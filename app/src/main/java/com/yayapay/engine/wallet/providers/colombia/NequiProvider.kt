package com.yayapay.engine.wallet.providers.colombia

import com.yayapay.engine.data.model.Currency
import com.yayapay.engine.data.model.WalletType
import com.yayapay.engine.wallet.ParsedNotification
import com.yayapay.engine.wallet.WalletProvider
import javax.inject.Inject

class NequiProvider @Inject constructor() : WalletProvider {

    override val walletType = WalletType.NEQUI
    override val currency = Currency.COP
    override val packageNames = setOf("com.nequi.MobileApp")
    override val maxAmount = 999_999_999_00L

    companion object {
        // COP uses dots as thousands separator: $50.000 or $150,000
        private const val AMOUNT_COP = """\$\s*([0-9.,]+)"""

        // "Te enviaron $50.000 desde Nequi"
        private val PATTERN_TE_ENVIARON = Regex(
            """[Tt]e\s+enviaron\s+$AMOUNT_COP""",
            RegexOption.IGNORE_CASE
        )
        // "Recibiste $150.000 de Juan"
        private val PATTERN_RECIBISTE = Regex(
            """[Rr]ecibiste\s+$AMOUNT_COP\s+de\s+(.+)""",
            RegexOption.IGNORE_CASE
        )
        // "Juan te envio $50.000 por Nequi"
        private val PATTERN_TE_ENVIO = Regex(
            """(.+?)\s+te\s+envio\s+$AMOUNT_COP""",
            RegexOption.IGNORE_CASE
        )
        // "Transferencia recibida por $50.000 de Juan"
        private val PATTERN_TRANSFERENCIA = Regex(
            """[Tt]ransferencia\s+recibida\s+(?:por\s+)?$AMOUNT_COP\s+de\s+(.+)""",
            RegexOption.IGNORE_CASE
        )
    }

    override fun parseNotification(text: String): ParsedNotification? {
        val trimmed = text.trim()

        // Patterns with amount only (no sender name available)
        PATTERN_TE_ENVIARON.find(trimmed)?.let { match ->
            val amount = parseCopAmount(match.groupValues[1]) ?: return@let
            if (amount <= 0) return@let
            return ParsedNotification("Nequi", currency.toSmallestUnit(amount), trimmed)
        }

        // Patterns with amount then name (swapped)
        for (pattern in listOf(PATTERN_RECIBISTE, PATTERN_TRANSFERENCIA)) {
            pattern.find(trimmed)?.let { match ->
                val amount = parseCopAmount(match.groupValues[1]) ?: return@let
                val name = match.groupValues[2].trim()
                if (name.isBlank() || amount <= 0) return@let
                return ParsedNotification(name, currency.toSmallestUnit(amount), trimmed)
            }
        }

        // Normal: name then amount
        PATTERN_TE_ENVIO.find(trimmed)?.let { match ->
            val name = match.groupValues[1].trim()
            val amount = parseCopAmount(match.groupValues[2]) ?: return@let
            if (name.isBlank() || amount <= 0) return@let
            return ParsedNotification(name, currency.toSmallestUnit(amount), trimmed)
        }

        return null
    }

    private fun parseCopAmount(raw: String): Double? =
        raw.replace(".", "").replace(",", ".").toDoubleOrNull()

    override fun generatePaymentLink(amountSmallestUnit: Long, recipientId: String): String? {
        val pesos = currency.toDisplayAmount(amountSmallestUnit).toLong()
        return "nequi://recharge?phone=$recipientId&value=$pesos"
    }

    override fun generateQrData(amountSmallestUnit: Long, recipientId: String): String? = null
}
