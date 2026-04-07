package com.yayapay.engine.wallet.providers.brazil

import com.yayapay.engine.data.model.Currency
import com.yayapay.engine.data.model.WalletType
import com.yayapay.engine.wallet.ParsedNotification
import com.yayapay.engine.wallet.WalletProvider

abstract class PixProvider : WalletProvider {

    override val currency = Currency.BRL
    override val maxAmount = 999_999_99L // R$ 999,999.99

    companion object {
        // BRL uses comma as decimal: R$ 50,00 or R$1.500,00
        private const val AMOUNT_BRL = """R\$\s*([0-9.]+,[0-9]{2})"""

        // "Voce recebeu R$ 50,00 de NOME via Pix"
        private val PATTERN_VOCE_RECEBEU = Regex(
            """[Vv]oc[eê]\s+recebeu\s+$AMOUNT_BRL\s+de\s+(.+?)(?:\s+via\s+[Pp]ix)?$""",
            RegexOption.IGNORE_CASE
        )
        // "Pix recebido: R$ 150,00 de FULANO"
        private val PATTERN_PIX_RECEBIDO = Regex(
            """[Pp]ix\s+recebido:?\s+$AMOUNT_BRL\s+de\s+(.+)""",
            RegexOption.IGNORE_CASE
        )
        // "Transferencia Pix de R$ 25,00 recebida de NOME"
        private val PATTERN_TRANSFERENCIA = Regex(
            """[Tt]ransfer[eê]ncia\s+[Pp]ix\s+de\s+$AMOUNT_BRL\s+recebida\s+de\s+(.+)""",
            RegexOption.IGNORE_CASE
        )
        // "Recebeu um Pix de R$ 100,00 de NOME"
        private val PATTERN_RECEBEU_PIX = Regex(
            """[Rr]ecebeu\s+(?:um\s+)?[Pp]ix\s+de\s+$AMOUNT_BRL\s+de\s+(.+)""",
            RegexOption.IGNORE_CASE
        )
        // Generic: "R$ 50,00 recebido" (no sender)
        private val PATTERN_GENERIC = Regex(
            """$AMOUNT_BRL\s+recebido""",
            RegexOption.IGNORE_CASE
        )

        private val SWAPPED_PATTERNS = listOf(
            PATTERN_VOCE_RECEBEU,
            PATTERN_PIX_RECEBIDO,
            PATTERN_TRANSFERENCIA,
            PATTERN_RECEBEU_PIX
        )
    }

    override fun parseNotification(text: String): ParsedNotification? {
        val trimmed = text.trim()

        for (pattern in SWAPPED_PATTERNS) {
            pattern.find(trimmed)?.let { match ->
                val amount = parseBrlAmount(match.groupValues[1]) ?: return@let
                val name = match.groupValues[2].trim()
                if (amount <= 0) return@let
                return ParsedNotification(
                    name.ifBlank { "PIX" },
                    currency.toSmallestUnit(amount),
                    trimmed
                )
            }
        }

        PATTERN_GENERIC.find(trimmed)?.let { match ->
            val amount = parseBrlAmount(match.groupValues[1]) ?: return@let
            if (amount <= 0) return@let
            return ParsedNotification("PIX", currency.toSmallestUnit(amount), trimmed)
        }

        return null
    }

    private fun parseBrlAmount(raw: String): Double? =
        raw.replace(".", "").replace(",", ".").toDoubleOrNull()

    override fun generatePaymentLink(amountSmallestUnit: Long, recipientId: String): String? {
        // PIX copia-e-cola is the same as the QR data
        return generateQrData(amountSmallestUnit, recipientId)
    }

    override fun generateQrData(amountSmallestUnit: Long, recipientId: String): String? {
        // Simplified BR Code (PIX) — real production would need full EMV TLV encoding
        // This generates a basic PIX static QR payload
        val amount = "%.2f".format(currency.toDisplayAmount(amountSmallestUnit))
        return buildString {
            append("00020126") // Payload format indicator + merchant account
            val merchantInfo = "0014BR.GOV.BCB.PIX01${"%02d".format(recipientId.length)}$recipientId"
            append("%02d".format(merchantInfo.length))
            append(merchantInfo)
            append("52040000") // Merchant category
            append("5303986") // Currency: BRL
            append("54${"%02d".format(amount.length)}$amount")
            append("5802BR") // Country
            append("6304") // CRC placeholder
        }
    }
}
