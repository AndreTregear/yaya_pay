package com.yayapay.engine.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "wallet_notifications",
    indices = [
        Index(value = ["notificationHash"]),
        Index(value = ["senderName", "amount", "capturedAt"]),
        Index(value = ["walletType", "amount", "matched"]),
        Index(value = ["capturedAt"])
    ]
)
data class WalletNotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val walletType: WalletType,
    val packageName: String,
    val senderName: String,
    val amount: Long,
    val currency: Currency,
    val rawNotification: String,
    val notificationHash: String,
    val matched: Boolean = false,
    val matchedIntentId: String? = null,
    val capturedAt: Long = System.currentTimeMillis()
)
