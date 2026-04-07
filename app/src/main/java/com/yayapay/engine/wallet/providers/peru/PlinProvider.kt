package com.yayapay.engine.wallet.providers.peru

import com.yayapay.engine.data.model.Currency
import com.yayapay.engine.data.model.WalletType
import com.yayapay.engine.wallet.ParsedNotification
import com.yayapay.engine.wallet.WalletProvider
import javax.inject.Inject

class PlinProvider @Inject constructor() : WalletProvider {

    override val walletType = WalletType.PLIN
    override val currency = Currency.PEN
    // Plin is embedded in multiple bank apps
    override val packageNames = setOf(
        "com.bcp.bcp21",              // BCP
        "com.interbank.mobilebanking", // Interbank
        "pe.com.scotiabank.blaze",     // Scotiabank
        "com.bbva.nxt_peru"            // BBVA
    )
    override val maxAmount = 500_000_00L

    companion object {
        private const val AMOUNT = """S/\.?\s*([0-9,]+\.?[0-9]*)"""

        // "Recibiste S/ 50.00 de Juan via PLIN"
        private val PATTERN_RECIBISTE_PLIN = Regex(
            """[Rr]ecibiste\s+$AMOUNT\s+de\s+(.+?)(?:\s+(?:via|por|mediante)\s+[Pp][Ll][Ii][Nn])?$""",
            RegexOption.IGNORE_CASE
        )
        // "Te enviaron S/ 50.00 por PLIN"
        private val PATTERN_TE_ENVIARON_PLIN = Regex(
            """(.+?)\s+te\s+envio\s+$AMOUNT\s+(?:por|via)\s+[Pp][Ll][Ii][Nn]""",
            RegexOption.IGNORE_CASE
        )
        // "PLIN recibido: S/ 50.00 de Juan"
        private val PATTERN_PLIN_RECIBIDO = Regex(
            """[Pp][Ll][Ii][Nn]\s+recibido:?\s+$AMOUNT\s+de\s+(.+)""",
            RegexOption.IGNORE_CASE
        )
        // Generic Plin with amount (sender+amount)
        private val PATTERN_GENERIC_PLIN = Regex(
            """(.+?)\s+te\s+(?:envio|hizo|transfiri[oó])\s+$AMOUNT""",
            RegexOption.IGNORE_CASE
        )
    }

    override fun parseNotification(text: String): ParsedNotification? {
        val trimmed = text.trim()
        // Only process if it looks like a Plin notification
        val looksLikePlin = trimmed.contains("PLIN", ignoreCase = true) ||
            trimmed.contains("plin", ignoreCase = true)

        // Swapped patterns (amount first, then name)
        for (pattern in listOf(PATTERN_RECIBISTE_PLIN, PATTERN_PLIN_RECIBIDO)) {
            pattern.find(trimmed)?.let { match ->
                val amount = parseAmount(match.groupValues[1]) ?: return@let
                val name = match.groupValues[2].trim()
                if (name.isBlank() || amount <= 0) return@let
                return ParsedNotification(name, currency.toSmallestUnit(amount), rawText = trimmed)
            }
        }

        // Normal patterns (name first, then amount) — only if it mentions Plin
        if (looksLikePlin) {
            for (pattern in listOf(PATTERN_TE_ENVIARON_PLIN, PATTERN_GENERIC_PLIN)) {
                pattern.find(trimmed)?.let { match ->
                    val name = match.groupValues[1].trim()
                    val amount = parseAmount(match.groupValues[2]) ?: return@let
                    if (name.isBlank() || amount <= 0) return@let
                    return ParsedNotification(name, currency.toSmallestUnit(amount), rawText = trimmed)
                }
            }
        }

        return null
    }

    private fun parseAmount(raw: String): Double? =
        raw.replace(",", "").toDoubleOrNull()

    override fun generatePaymentLink(amountSmallestUnit: Long, recipientId: String): String? = null

    override fun generateQrData(amountSmallestUnit: Long, recipientId: String): String? = null
}
