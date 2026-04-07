package com.yayapay.engine.notification

import android.app.Notification
import android.service.notification.StatusBarNotification
import android.util.Log
import com.yayapay.engine.data.local.db.WalletNotificationDao
import com.yayapay.engine.data.model.WalletNotificationEntity
import com.yayapay.engine.engine.PaymentMatchingEngine
import com.yayapay.engine.wallet.WalletRegistry
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRouter @Inject constructor(
    private val walletRegistry: WalletRegistry,
    private val deduplicationManager: DeduplicationManager,
    private val walletNotificationDao: WalletNotificationDao,
    private val paymentMatchingEngine: PaymentMatchingEngine
) {
    companion object {
        private const val TAG = "NotificationRouter"
    }

    suspend fun route(sbn: StatusBarNotification) {
        val providers = walletRegistry.getByPackage(sbn.packageName)
        if (providers.isEmpty()) return

        val extras = sbn.notification.extras ?: return
        val text = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: return

        for (provider in providers) {
            val parsed = provider.parseNotification(text) ?: continue
            if (parsed.amountSmallestUnit > provider.maxAmount) {
                Log.w(TAG, "Rejecting ${provider.walletType}: amount exceeds max")
                continue
            }

            if (deduplicationManager.isDuplicate(text, provider.dedupHashWindowMs)) {
                Log.d(TAG, "Duplicate notification (hash), skipping")
                continue
            }
            if (deduplicationManager.isDuplicatePayment(
                    parsed.senderName, parsed.amountSmallestUnit,
                    provider.walletType, provider.dedupSenderAmountWindowMs
                )) {
                Log.d(TAG, "Duplicate payment (sender+amount), skipping")
                continue
            }

            val hash = deduplicationManager.computeHash(text)
            val notification = WalletNotificationEntity(
                walletType = provider.walletType,
                packageName = sbn.packageName,
                senderName = parsed.senderName.take(200),
                amount = parsed.amountSmallestUnit,
                currency = provider.currency,
                rawNotification = parsed.rawText.take(500),
                notificationHash = hash
            )

            val notifId = walletNotificationDao.insert(notification)
            Log.d(TAG, "Notification saved: ${provider.walletType} id=$notifId")

            paymentMatchingEngine.attemptMatch(
                notifId, provider.walletType, parsed.amountSmallestUnit, provider.currency
            )
            return // First successful parse wins
        }
    }
}
