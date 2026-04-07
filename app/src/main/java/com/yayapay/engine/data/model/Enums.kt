package com.yayapay.engine.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class PaymentIntentStatus {
    CREATED,
    PENDING,
    SUCCEEDED,
    FAILED,
    CANCELED,
    EXPIRED
}

@Serializable
enum class WalletType(val displayName: String, val country: String) {
    YAPE("Yape", "PE"),
    PLIN("Plin", "PE"),
    NEQUI("Nequi", "CO"),
    DAVIPLATA("Daviplata", "CO"),
    MERCADOPAGO_MX("Mercado Pago", "MX"),
    CODI_DIMO("CoDi/DiMo", "MX"),
    PIX_NUBANK("PIX (Nubank)", "BR"),
    PIX_ITAU("PIX (Itau)", "BR"),
    PIX_BRADESCO("PIX (Bradesco)", "BR"),
    PIX_INTER("PIX (Inter)", "BR"),
    MERCADOPAGO_AR("Mercado Pago", "AR"),
    MERCADOPAGO_CL("Mercado Pago", "CL");

    val isPix: Boolean get() = name.startsWith("PIX_")
    val isMercadoPago: Boolean get() = name.startsWith("MERCADOPAGO_")
}

@Serializable
enum class Currency(val code: String, val symbol: String, val smallestUnitFactor: Int) {
    PEN("PEN", "S/", 100),
    COP("COP", "$", 100),
    MXN("MXN", "$", 100),
    BRL("BRL", "R$", 100),
    ARS("ARS", "$", 100),
    CLP("CLP", "$", 1);

    fun toSmallestUnit(displayAmount: Double): Long =
        (displayAmount * smallestUnitFactor).toLong()

    fun toDisplayAmount(smallestUnit: Long): Double =
        smallestUnit.toDouble() / smallestUnitFactor
}

enum class DeliveryStatus {
    PENDING,
    DELIVERED,
    FAILED,
    EXHAUSTED
}
