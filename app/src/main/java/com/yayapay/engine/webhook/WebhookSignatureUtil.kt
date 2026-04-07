package com.yayapay.engine.webhook

import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class WebhookSignatureUtil @Inject constructor() {

    fun sign(payload: String, secret: String): String {
        val timestamp = System.currentTimeMillis() / 1000
        val signedPayload = "$timestamp.$payload"
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(), "HmacSHA256"))
        val hash = mac.doFinal(signedPayload.toByteArray()).joinToString("") { "%02x".format(it) }
        return "t=$timestamp,v1=$hash"
    }

    fun verify(payload: String, signature: String, secret: String, toleranceSeconds: Long = 300): Boolean {
        val parts = signature.split(",").associate {
            val (k, v) = it.split("=", limit = 2)
            k to v
        }
        val timestamp = parts["t"]?.toLongOrNull() ?: return false
        val v1 = parts["v1"] ?: return false

        if (abs(System.currentTimeMillis() / 1000 - timestamp) > toleranceSeconds) return false

        val signedPayload = "$timestamp.$payload"
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(), "HmacSHA256"))
        val expected = mac.doFinal(signedPayload.toByteArray()).joinToString("") { "%02x".format(it) }
        return MessageDigest.isEqual(v1.toByteArray(), expected.toByteArray())
    }
}
