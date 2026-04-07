package com.yayapay.engine.wallet.providers.colombia

import com.yayapay.engine.data.model.Currency
import com.yayapay.engine.data.model.WalletType
import com.yayapay.engine.wallet.ParsedNotification
import com.yayapay.engine.wallet.WalletProvider
import javax.inject.Inject

class DaviplataProvider @Inject constructor() : WalletProvider {

    override val walletType = WalletType.DAVIPLATA
    override val currency = Currency.COP
    override val packageNames = setOf("com.davivienda.daviplataapp")
    override val maxAmount = 999_999_999_00L

    companion object {
        private const val AMOUNT_COP = """\$\s*([0-9.,]+)"""

        // "Recibiste $50.000 en tu DaviPlata"
        private val PATTERN_RECIBISTE = Regex(
            """[Rr]ecibiste\s+$AMOUNT_COP\s+(?:en\s+tu\s+)?[Dd]avi[Pp]lata""",
            RegexOption.IGNORE_CASE
        )
        // "Te enviaron $50.000 a tu DaviPlata de Juan"
        private val PATTERN_TE_ENVIARON = Regex(
            """[Tt]e\s+enviaron\s+$AMOUNT_COP.*?de\s+(.+)""",
            RegexOption.IGNORE_CASE
        )
        // "Transferencia recibida $50.000 de Juan"
        private val PATTERN_TRANSFERENCIA = Regex(
            """[Tt]ransferencia\s+recibida\s+$AMOUNT_COP\s+de\s+(.+)""",
            RegexOption.IGNORE_CASE
        )
    }

    override fun parseNotification(text: String): ParsedNotification? {
        val trimmed = text.trim()

        PATTERN_RECIBISTE.find(trimmed)?.let { match ->
            val amount = parseCopAmount(match.groupValues[1]) ?: return@let
            if (amount <= 0) return@let
            return ParsedNotification("Daviplata", currency.toSmallestUnit(amount), trimmed)
        }

        for (pattern in listOf(PATTERN_TE_ENVIARON, PATTERN_TRANSFERENCIA)) {
            pattern.find(trimmed)?.let { match ->
                val amount = parseCopAmount(match.groupValues[1]) ?: return@let
                val name = match.groupValues[2].trim()
                if (amount <= 0) return@let
                return ParsedNotification(
                    name.ifBlank { "Daviplata" },
                    currency.toSmallestUnit(amount),
                    trimmed
                )
            }
        }

        return null
    }

    private fun parseCopAmount(raw: String): Double? =
        raw.replace(".", "").replace(",", ".").toDoubleOrNull()

    override fun generatePaymentLink(amountSmallestUnit: Long, recipientId: String): String? = null
    override fun generateQrData(amountSmallestUnit: Long, recipientId: String): String? = null
}
