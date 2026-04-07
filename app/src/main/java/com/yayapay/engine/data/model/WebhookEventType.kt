package com.yayapay.engine.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class WebhookEventType(val value: String) {
    PAYMENT_INTENT_CREATED("payment_intent.created"),
    PAYMENT_INTENT_SUCCEEDED("payment_intent.succeeded"),
    PAYMENT_INTENT_FAILED("payment_intent.failed"),
    PAYMENT_INTENT_CANCELED("payment_intent.canceled"),
    PAYMENT_INTENT_EXPIRED("payment_intent.expired");

    companion object {
        fun fromValue(value: String): WebhookEventType? =
            entries.find { it.value == value }
    }
}
