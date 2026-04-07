package com.yayapay.engine.server.routes

import com.yayapay.engine.data.model.PaymentIntentEntity
import com.yayapay.engine.data.model.PaymentIntentStatus
import com.yayapay.engine.data.model.WalletType
import com.yayapay.engine.data.model.WebhookEventType
import com.yayapay.engine.engine.PaymentIntentRepository
import com.yayapay.engine.server.dto.CreatePaymentIntentRequest
import com.yayapay.engine.server.dto.PaginatedResponse
import com.yayapay.engine.server.dto.errorResponse
import com.yayapay.engine.server.dto.toResponse
import com.yayapay.engine.util.IdGenerator
import com.yayapay.engine.wallet.WalletRegistry
import com.yayapay.engine.webhook.WebhookDispatcher
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

fun Route.paymentIntentRoutes(
    intentRepo: PaymentIntentRepository,
    walletRegistry: WalletRegistry,
    webhookDispatcher: WebhookDispatcher,
    idGenerator: IdGenerator,
    defaultExpirationMinutes: Int = 30
) {
    post("/payment_intents") {
        val req = call.receive<CreatePaymentIntentRequest>()

        if (req.amount <= 0) {
            call.respond(HttpStatusCode.BadRequest, errorResponse("Amount must be positive"))
            return@post
        }

        val walletType = try {
            WalletType.valueOf(req.walletType)
        } catch (_: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, errorResponse("Unsupported wallet type: ${req.walletType}"))
            return@post
        }

        val provider = walletRegistry.getByType(walletType)
        if (provider == null) {
            call.respond(HttpStatusCode.BadRequest, errorResponse("Wallet not registered: ${req.walletType}"))
            return@post
        }

        // Idempotency check
        if (req.idempotencyKey != null) {
            val existing = intentRepo.getByIdempotencyKey(req.idempotencyKey)
            if (existing != null) {
                call.respond(HttpStatusCode.OK, existing.toResponse())
                return@post
            }
        }

        val expirationMinutes = req.expirationMinutes ?: defaultExpirationMinutes
        val recipientId = req.recipientId ?: ""
        val paymentLink = provider.generatePaymentLink(req.amount, recipientId)
        val qrData = provider.generateQrData(req.amount, recipientId)

        val entity = PaymentIntentEntity(
            id = idGenerator.paymentIntentId(),
            amount = req.amount,
            currency = provider.currency,
            walletType = walletType,
            description = req.description,
            metadata = req.metadata,
            paymentLink = paymentLink,
            qrData = qrData,
            recipientIdentifier = req.recipientId,
            idempotencyKey = req.idempotencyKey,
            clientReferenceId = req.clientReferenceId,
            expiresAt = System.currentTimeMillis() + expirationMinutes * 60_000L
        )

        intentRepo.create(entity)

        webhookDispatcher.dispatch(WebhookEventType.PAYMENT_INTENT_CREATED, entity)

        call.respond(HttpStatusCode.Created, entity.toResponse())
    }

    get("/payment_intents/{id}") {
        val id = call.parameters["id"] ?: run {
            call.respond(HttpStatusCode.BadRequest, errorResponse("Missing id"))
            return@get
        }
        val intent = intentRepo.getById(id)
        if (intent == null) {
            call.respond(HttpStatusCode.NotFound, errorResponse("Payment intent not found"))
            return@get
        }
        call.respond(intent.toResponse())
    }

    post("/payment_intents/{id}/cancel") {
        val id = call.parameters["id"] ?: run {
            call.respond(HttpStatusCode.BadRequest, errorResponse("Missing id"))
            return@post
        }
        val canceled = intentRepo.cancel(id)
        if (canceled == null) {
            call.respond(HttpStatusCode.BadRequest, errorResponse("Cannot cancel this payment intent"))
            return@post
        }
        webhookDispatcher.dispatch(WebhookEventType.PAYMENT_INTENT_CANCELED, canceled)
        call.respond(canceled.toResponse())
    }

    get("/payment_intents") {
        val status = call.parameters["status"]?.let {
            try { PaymentIntentStatus.valueOf(it.uppercase()) } catch (_: Exception) { null }
        }
        val wallet = call.parameters["wallet"]?.let {
            try { WalletType.valueOf(it) } catch (_: Exception) { null }
        }
        val limit = (call.parameters["limit"]?.toIntOrNull() ?: 25).coerceIn(1, 100)
        val offset = (call.parameters["offset"]?.toIntOrNull() ?: 0).coerceAtLeast(0)

        val intents = intentRepo.list(status, wallet, limit, offset)
        call.respond(PaginatedResponse(
            data = intents.map { it.toResponse() },
            hasMore = intents.size == limit
        ))
    }
}
