package com.yayapay.engine.wallet

data class ParsedNotification(
    val senderName: String,
    val amountSmallestUnit: Long,
    val rawText: String
)
