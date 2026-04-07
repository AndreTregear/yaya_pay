package com.yayapay.engine.wallet.providers.chile

import com.yayapay.engine.data.model.Currency
import com.yayapay.engine.data.model.WalletType
import com.yayapay.engine.wallet.ParsedNotification
import com.yayapay.engine.wallet.WalletProvider
import javax.inject.Inject

class MercadoPagoClProvider @Inject constructor() : WalletProvider {

    override val walletType = WalletType.MERCADOPAGO_CL
    override val currency = Currency.CLP
    override val packageNames = setOf("com.mercadopago.wallet")
    override val maxAmount = 999_999_999L // CLP has no subdivisions

    companion object {
        // CLP: $50.000 (dots as thousands separator, no decimals)
        private const val AMOUNT_CLP = """\$\s*([0-9.]+)"""

        private val PATTERN_RECIBISTE = Regex(
            """[Rr]ecibiste\s+$AMOUNT_CLP\s+de\s+(.+)""",
            RegexOption.IGNORE_CASE
        )
        private val PATTERN_TE_ENVIARON = Regex(
            """[Tt]e\s+enviaron\s+$AMOUNT_CLP""",
            RegexOption.IGNORE_CASE
        )
        private val PATTERN_COBRO = Regex(
            """[Cc]obro\s+recibido\s+(?:por\s+)?$AMOUNT_CLP""",
            RegexOption.IGNORE_CASE
        )
    }

    override fun parseNotification(text: String): ParsedNotification? {
        val trimmed = text.trim()

        PATTERN_RECIBISTE.find(trimmed)?.let { match ->
            val amount = parseClpAmount(match.groupValues[1]) ?: return@let
            val name = match.groupValues[2].trim()
            if (amount <= 0) return@let
            return ParsedNotification(name.ifBlank { "Mercado Pago" }, currency.toSmallestUnit(amount.toDouble()), trimmed)
        }

        for (pattern in listOf(PATTERN_TE_ENVIARON, PATTERN_COBRO)) {
            pattern.find(trimmed)?.let { match ->
                val amount = parseClpAmount(match.groupValues[1]) ?: return@let
                if (amount <= 0) return@let
                return ParsedNotification("Mercado Pago", currency.toSmallestUnit(amount.toDouble()), trimmed)
            }
        }

        return null
    }

    private fun parseClpAmount(raw: String): Long? =
        raw.replace(".", "").toLongOrNull()

    override fun generatePaymentLink(amountSmallestUnit: Long, recipientId: String): String? {
        return "mercadopago://collect?amount=$amountSmallestUnit&reason=payment"
    }

    override fun generateQrData(amountSmallestUnit: Long, recipientId: String): String? = null
}
