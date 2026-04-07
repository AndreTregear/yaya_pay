package com.yayapay.engine.engine

import android.util.Log
import com.yayapay.engine.data.local.db.PaymentIntentDao
import com.yayapay.engine.data.local.db.WalletNotificationDao
import com.yayapay.engine.data.model.Currency
import com.yayapay.engine.data.model.PaymentIntentStatus
import com.yayapay.engine.data.model.WalletType
import com.yayapay.engine.data.model.WebhookEventType
import com.yayapay.engine.webhook.WebhookDispatcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentMatchingEngine @Inject constructor(
    private val intentDao: PaymentIntentDao,
    private val notificationDao: WalletNotificationDao,
    private val webhookDispatcher: WebhookDispatcher
) {
    companion object {
        private const val TAG = "PaymentMatchingEngine"
    }

    /**
     * Attempts to match a captured wallet notification against pending payment intents.
     *
     * Strategy (from yape-matcher.ts):
     * - Filter intents by: walletType + amount + status in (CREATED, PENDING) + not expired
     * - Single match -> auto-confirm
     * - Zero matches -> notification stays unmatched
     * - Multiple matches -> ambiguous, leave as pending
     */
    suspend fun attemptMatch(
        notificationId: Long,
        walletType: WalletType,
        amountSmallestUnit: Long,
        currency: Currency
    ) {
        val now = System.currentTimeMillis()
        val candidates = intentDao.findMatchingIntents(
            walletType = walletType,
            amount = amountSmallestUnit,
            now = now
        )

        when (candidates.size) {
            1 -> {
                val intent = candidates[0]
                val notif = notificationDao.getById(notificationId)

                intentDao.confirmIntent(
                    id = intent.id,
                    notificationId = notificationId,
                    senderName = notif?.senderName,
                    succeededAt = now
                )
                notificationDao.markMatched(notificationId, intent.id)

                Log.i(TAG, "Payment matched: intent=${intent.id} notification=$notificationId")

                webhookDispatcher.dispatch(
                    WebhookEventType.PAYMENT_INTENT_SUCCEEDED,
                    intent.copy(
                        status = PaymentIntentStatus.SUCCEEDED,
                        matchedNotificationId = notificationId,
                        senderName = notif?.senderName,
                        succeededAt = now,
                        updatedAt = now
                    )
                )
            }
            0 -> {
                Log.d(TAG, "No matching intent for $walletType amount=$amountSmallestUnit")
            }
            else -> {
                Log.w(TAG, "Ambiguous: ${candidates.size} intents match $walletType amount=$amountSmallestUnit")
            }
        }
    }
}
