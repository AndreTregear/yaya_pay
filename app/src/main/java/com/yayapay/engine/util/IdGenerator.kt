package com.yayapay.engine.util

import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IdGenerator @Inject constructor() {

    private val random = SecureRandom()
    private val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

    private fun generate(length: Int): String =
        (1..length).map { chars[random.nextInt(chars.length)] }.joinToString("")

    fun paymentIntentId(): String = "pi_${generate(24)}"
    fun webhookEndpointId(): String = "we_${generate(24)}"
    fun webhookDeliveryId(): String = "wdel_${generate(24)}"
    fun apiKeyId(): String = "ak_${generate(24)}"
    fun eventId(): String = "evt_${generate(24)}"

    fun apiKeySecret(): String = "sk_live_${generate(32)}"
    fun webhookSecret(): String = "whsec_${generate(32)}"
}
