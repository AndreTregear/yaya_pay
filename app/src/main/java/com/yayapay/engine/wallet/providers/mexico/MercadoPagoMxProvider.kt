package com.yayapay.engine.wallet.providers.mexico

import com.yayapay.engine.data.model.Currency
import com.yayapay.engine.data.model.WalletType
import com.yayapay.engine.wallet.ParsedNotification
import com.yayapay.engine.wallet.WalletProvider
import javax.inject.Inject

class MercadoPagoMxProvider @Inject constructor() : WalletProvider {

    override val walletType = WalletType.MERCADOPAGO_MX
    override val currency = Currency.MXN
    override val packageNames = setOf("com.mercadopago.wallet")
    override val maxAmount = 99_999_999_00L

    companion object {
        private const val AMOUNT_MXN = """\$\s*([0-9,]+\.?[0-9]*)"""

        // "Recibiste $500.00 de Juan"
        private val PATTERN_RECIBISTE = Regex(
            """[Rr]ecibiste\s+$AMOUNT_MXN\s+de\s+(.+)""",
            RegexOption.IGNORE_CASE
        )
        // "Te enviaron $500.00"
        private val PATTERN_TE_ENVIARON = Regex(
            """[Tt]e\s+enviaron\s+$AMOUNT_MXN""",
            RegexOption.IGNORE_CASE
        )
        // "Juan te envio $500.00"
        private val PATTERN_TE_ENVIO = Regex(
            """(.+?)\s+te\s+envio\s+$AMOUNT_MXN""",
            RegexOption.IGNORE_CASE
        )
        // "Cobro recibido por $500.00"
        private val PATTERN_COBRO = Regex(
            """[Cc]obro\s+recibido\s+(?:por\s+)?$AMOUNT_MXN""",
            RegexOption.IGNORE_CASE
        )
    }

    override fun parseNotification(text: String): ParsedNotification? {
        val trimmed = text.trim()

        PATTERN_RECIBISTE.find(trimmed)?.let { match ->
            val amount = parseMxnAmount(match.groupValues[1]) ?: return@let
            val name = match.groupValues[2].trim()
            if (amount <= 0) return@let
            return ParsedNotification(name.ifBlank { "Mercado Pago" }, currency.toSmallestUnit(amount), trimmed)
        }

        PATTERN_TE_ENVIO.find(trimmed)?.let { match ->
            val name = match.groupValues[1].trim()
            val amount = parseMxnAmount(match.groupValues[2]) ?: return@let
            if (amount <= 0) return@let
            return ParsedNotification(name.ifBlank { "Mercado Pago" }, currency.toSmallestUnit(amount), trimmed)
        }

        for (pattern in listOf(PATTERN_TE_ENVIARON, PATTERN_COBRO)) {
            pattern.find(trimmed)?.let { match ->
                val amount = parseMxnAmount(match.groupValues[1]) ?: return@let
                if (amount <= 0) return@let
                return ParsedNotification("Mercado Pago", currency.toSmallestUnit(amount), trimmed)
            }
        }

        return null
    }

    private fun parseMxnAmount(raw: String): Double? =
        raw.replace(",", "").toDoubleOrNull()

    override fun generatePaymentLink(amountSmallestUnit: Long, recipientId: String): String? {
        val amount = currency.toDisplayAmount(amountSmallestUnit)
        return "mercadopago://collect?amount=${"%.2f".format(amount)}&reason=payment"
    }

    override fun generateQrData(amountSmallestUnit: Long, recipientId: String): String? = null
}
