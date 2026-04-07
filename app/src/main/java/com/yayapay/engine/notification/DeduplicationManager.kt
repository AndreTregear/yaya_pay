package com.yayapay.engine.notification

import com.yayapay.engine.data.local.db.WalletNotificationDao
import com.yayapay.engine.data.model.WalletType
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeduplicationManager @Inject constructor(
    private val walletNotificationDao: WalletNotificationDao
) {
    suspend fun isDuplicate(rawText: String, windowMs: Long): Boolean {
        val hash = computeHash(rawText)
        val since = System.currentTimeMillis() - windowMs
        return walletNotificationDao.countByHashSince(hash, since) > 0
    }

    suspend fun isDuplicatePayment(
        senderName: String,
        amount: Long,
        walletType: WalletType,
        windowMs: Long
    ): Boolean {
        val since = System.currentTimeMillis() - windowMs
        return walletNotificationDao.countBySenderAmountWalletSince(senderName, amount, walletType, since) > 0
    }

    fun computeHash(rawText: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(rawText.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
