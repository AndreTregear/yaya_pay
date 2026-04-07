package com.yayapay.engine.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payment_intents",
    indices = [
        Index(value = ["status", "walletType"]),
        Index(value = ["amount", "currency", "walletType", "status"]),
        Index(value = ["expiresAt"]),
        Index(value = ["createdAt"]),
        Index(value = ["idempotencyKey"], unique = true)
    ]
)
data class PaymentIntentEntity(
    @PrimaryKey
    val id: String,
    val amount: Long,
    val currency: Currency,
    val walletType: WalletType,
    val description: String? = null,
    val metadata: String? = null,
    val status: PaymentIntentStatus = PaymentIntentStatus.CREATED,
    val paymentLink: String? = null,
    val qrData: String? = null,
    val recipientIdentifier: String? = null,
    val matchedNotificationId: Long? = null,
    val senderName: String? = null,
    val idempotencyKey: String? = null,
    val clientReferenceId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long,
    val succeededAt: Long? = null,
    val canceledAt: Long? = null
)
