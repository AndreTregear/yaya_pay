package com.yayapay.engine.server.dto

import com.yayapay.engine.data.model.PaymentIntentEntity
import com.yayapay.engine.data.model.WalletType
import com.yayapay.engine.data.model.WebhookEndpointEntity
import kotlinx.serialization.Serializable

// --- Payment Intents ---

@Serializable
data class CreatePaymentIntentRequest(
    val amount: Long,
    val walletType: String,
    val description: String? = null,
    val metadata: String? = null,
    val recipientId: String? = null,
    val expirationMinutes: Int? = null,
    val idempotencyKey: String? = null,
    val clientReferenceId: String? = null
)

@Serializable
data class PaymentIntentResponse(
    val id: String,
    val `object`: String = "payment_intent",
    val amount: Long,
    val currency: String,
    val wallet: String,
    val status: String,
    val description: String? = null,
    val metadata: String? = null,
    val paymentLink: String? = null,
    val qrData: String? = null,
    val senderName: String? = null,
    val clientReferenceId: String? = null,
    val createdAt: Long,
    val expiresAt: Long,
    val succeededAt: Long? = null,
    val canceledAt: Long? = null,
    val livemode: Boolean = true
)

fun PaymentIntentEntity.toResponse() = PaymentIntentResponse(
    id = id,
    amount = amount,
    currency = currency.code,
    wallet = walletType.name,
    status = status.name.lowercase(),
    description = description,
    metadata = metadata,
    paymentLink = paymentLink,
    qrData = qrData,
    senderName = senderName,
    clientReferenceId = clientReferenceId,
    createdAt = createdAt,
    expiresAt = expiresAt,
    succeededAt = succeededAt,
    canceledAt = canceledAt
)

// --- Webhooks ---

@Serializable
data class CreateWebhookRequest(
    val url: String,
    val enabledEvents: List<String>,
    val description: String? = null
)

@Serializable
data class WebhookEndpointResponse(
    val id: String,
    val `object`: String = "webhook_endpoint",
    val url: String,
    val secret: String? = null,
    val enabledEvents: List<String>,
    val description: String? = null,
    val active: Boolean,
    val createdAt: Long
)

fun WebhookEndpointEntity.toResponse(includeSecret: Boolean = false) = WebhookEndpointResponse(
    id = id,
    url = url,
    secret = if (includeSecret) secret else null,
    enabledEvents = enabledEvents.split(",").filter { it.isNotBlank() },
    description = description,
    active = active,
    createdAt = createdAt
)

// --- Wallets ---

@Serializable
data class WalletResponse(
    val type: String,
    val displayName: String,
    val country: String,
    val currency: String,
    val installed: Boolean
)

// --- Health ---

@Serializable
data class HealthResponse(
    val status: String = "ok",
    val version: String,
    val uptime: Long,
    val serverPort: Int,
    val activeWallets: Int,
    val localIp: String?
)

// --- Generic ---

@Serializable
data class ErrorResponse(
    val error: ErrorDetail
)

@Serializable
data class ErrorDetail(
    val type: String = "invalid_request_error",
    val message: String
)

fun errorResponse(message: String, type: String = "invalid_request_error") =
    ErrorResponse(ErrorDetail(type = type, message = message))

@Serializable
data class PaginatedResponse<T>(
    val `object`: String = "list",
    val data: List<T>,
    val hasMore: Boolean = false
)

// --- Webhook Event Payload ---

@Serializable
data class WebhookEvent(
    val id: String,
    val `object`: String = "event",
    val type: String,
    val createdAt: Long,
    val data: PaymentIntentResponse
)
