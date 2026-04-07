package com.yayapay.engine.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "webhook_deliveries",
    indices = [
        Index(value = ["status"]),
        Index(value = ["webhookEndpointId"]),
        Index(value = ["nextRetryAt"])
    ]
)
data class WebhookDeliveryEntity(
    @PrimaryKey
    val id: String,
    val webhookEndpointId: String,
    val eventType: String,
    val payload: String,
    val status: DeliveryStatus = DeliveryStatus.PENDING,
    val httpStatusCode: Int? = null,
    val responseBody: String? = null,
    val attemptCount: Int = 0,
    val maxAttempts: Int = 5,
    val nextRetryAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val deliveredAt: Long? = null
)
