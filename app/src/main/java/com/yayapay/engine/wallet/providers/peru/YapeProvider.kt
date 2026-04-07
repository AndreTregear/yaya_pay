package com.yayapay.engine.wallet.providers.peru

import com.yayapay.engine.data.model.Currency
import com.yayapay.engine.data.model.WalletType
import com.yayapay.engine.wallet.ParsedNotification
import com.yayapay.engine.wallet.WalletProvider
import javax.inject.Inject

class YapeProvider @Inject constructor() : WalletProvider {

    override val walletType = WalletType.YAPE
    override val currency = Currency.PEN
    override val packageNames = setOf("com.bcp.innovacxion.yapeapp")
    override val maxAmount = 500_000_00L // S/ 500,000 in centimos

    companion object {
        private const val AMOUNT = """S/\.?\s*([0-9,]+\.?[0-9]*)"""

        // "Juan te envio S/ 25.50"
        private val PATTERN_TE_ENVIO = Regex("""(.+?)\s+te\s+envio\s+$AMOUNT""", RegexOption.IGNORE_CASE)
        // "Recibiste de Juan por S/ 25.50"
        private val PATTERN_RECIBISTE = Regex("""Recibiste\s+de\s+(.+?)\s+por\s+$AMOUNT""", RegexOption.IGNORE_CASE)
        // "Juan te han enviado S/ 25.50"
        private val PATTERN_TE_HAN_ENVIADO = Regex("""(.+?)\s+te\s+ha[ns]?\s+enviado\s+$AMOUNT""", RegexOption.IGNORE_CASE)
        // "Has recibido S/ 25.50 de Juan" (swapped: amount first, name second)
        private val PATTERN_HAS_RECIBIDO = Regex("""Has\s+recibido\s+$AMOUNT\s+de\s+(.+)""", RegexOption.IGNORE_CASE)
        // "Juan te yapeo S/ 25.50"
        private val PATTERN_TE_YAPEO = Regex("""(.+?)\s+te\s+yapeo\s+$AMOUNT""", RegexOption.IGNORE_CASE)

        private val NORMAL_PATTERNS = listOf(PATTERN_TE_ENVIO, PATTERN_RECIBISTE, PATTERN_TE_HAN_ENVIADO, PATTERN_TE_YAPEO)
        private val SWAPPED_PATTERNS = listOf(PATTERN_HAS_RECIBIDO)
    }

    override fun parseNotification(text: String): ParsedNotification? {
        val trimmed = text.trim()

        for (pattern in NORMAL_PATTERNS) {
            pattern.find(trimmed)?.let { match ->
                return extractNormal(match, trimmed)
            }
        }

        for (pattern in SWAPPED_PATTERNS) {
            pattern.find(trimmed)?.let { match ->
                return extractSwapped(match, trimmed)
            }
        }

        return null
    }

    private fun extractNormal(match: MatchResult, rawText: String): ParsedNotification? {
        val name = match.groupValues[1].trim()
        val amount = parseAmount(match.groupValues[2]) ?: return null
        if (name.isBlank() || amount <= 0) return null
        return ParsedNotification(name, currency.toSmallestUnit(amount), rawText)
    }

    private fun extractSwapped(match: MatchResult, rawText: String): ParsedNotification? {
        val amount = parseAmount(match.groupValues[1]) ?: return null
        val name = match.groupValues[2].trim()
        if (name.isBlank() || amount <= 0) return null
        return ParsedNotification(name, currency.toSmallestUnit(amount), rawText)
    }

    private fun parseAmount(raw: String): Double? =
        raw.replace(",", "").toDoubleOrNull()

    override fun generatePaymentLink(amountSmallestUnit: Long, recipientId: String): String? {
        val soles = currency.toDisplayAmount(amountSmallestUnit)
        return "yape://pay?phone=$recipientId&amount=${"%.2f".format(soles)}"
    }

    override fun generateQrData(amountSmallestUnit: Long, recipientId: String): String? {
        val soles = currency.toDisplayAmount(amountSmallestUnit)
        return "yape:$recipientId:${"%.2f".format(soles)}"
    }
}
