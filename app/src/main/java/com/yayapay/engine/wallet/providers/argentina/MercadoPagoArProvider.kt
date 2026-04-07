package com.yayapay.engine.wallet.providers.argentina

import com.yayapay.engine.data.model.Currency
import com.yayapay.engine.data.model.WalletType
import com.yayapay.engine.wallet.ParsedNotification
import com.yayapay.engine.wallet.WalletProvider
import javax.inject.Inject

class MercadoPagoArProvider @Inject constructor() : WalletProvider {

    override val walletType = WalletType.MERCADOPAGO_AR
    override val currency = Currency.ARS
    override val packageNames = setOf("com.mercadopago.wallet")
    override val maxAmount = 99_999_999_00L

    companion object {
        // ARS uses dots for thousands, comma for decimals: $5.000,50
        private const val AMOUNT_ARS = """\$\s*([0-9.]+(?:,[0-9]+)?)"""

        // "Recibiste $5.000 de Juan"
        private val PATTERN_RECIBISTE = Regex(
            """[Rr]ecibiste\s+$AMOUNT_ARS\s+de\s+(.+)""",
            RegexOption.IGNORE_CASE
        )
        // "Te enviaron $1.500,50"
        private val PATTERN_TE_ENVIARON = Regex(
            """[Tt]e\s+enviaron\s+$AMOUNT_ARS""",
            RegexOption.IGNORE_CASE
        )
        // "Cobro recibido $5.000"
        private val PATTERN_COBRO = Regex(
            """[Cc]obro\s+recibido\s+(?:por\s+)?$AMOUNT_ARS""",
            RegexOption.IGNORE_CASE
        )
    }

    override fun parseNotification(text: String): ParsedNotification? {
        val trimmed = text.trim()

        PATTERN_RECIBISTE.find(trimmed)?.let { match ->
            val amount = parseArsAmount(match.groupValues[1]) ?: return@let
            val name = match.groupValues[2].trim()
            if (amount <= 0) return@let
            return ParsedNotification(name.ifBlank { "Mercado Pago" }, currency.toSmallestUnit(amount), trimmed)
        }

        for (pattern in listOf(PATTERN_TE_ENVIARON, PATTERN_COBRO)) {
            pattern.find(trimmed)?.let { match ->
                val amount = parseArsAmount(match.groupValues[1]) ?: return@let
                if (amount <= 0) return@let
                return ParsedNotification("Mercado Pago", currency.toSmallestUnit(amount), trimmed)
            }
        }

        return null
    }

    private fun parseArsAmount(raw: String): Double? =
        raw.replace(".", "").replace(",", ".").toDoubleOrNull()

    override fun generatePaymentLink(amountSmallestUnit: Long, recipientId: String): String? {
        val amount = currency.toDisplayAmount(amountSmallestUnit)
        return "mercadopago://collect?amount=${"%.2f".format(amount)}&reason=payment"
    }

    override fun generateQrData(amountSmallestUnit: Long, recipientId: String): String? = null
}
