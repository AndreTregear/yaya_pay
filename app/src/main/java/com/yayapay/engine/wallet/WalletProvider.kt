package com.yayapay.engine.wallet

import com.yayapay.engine.data.model.Currency
import com.yayapay.engine.data.model.WalletType

interface WalletProvider {
    val walletType: WalletType
    val currency: Currency
    val packageNames: Set<String>
    val maxAmount: Long

    val dedupHashWindowMs: Long get() = 60_000L
    val dedupSenderAmountWindowMs: Long get() = 120_000L

    fun parseNotification(text: String): ParsedNotification?
    fun generatePaymentLink(amountSmallestUnit: Long, recipientId: String): String?
    fun generateQrData(amountSmallestUnit: Long, recipientId: String): String?
}
