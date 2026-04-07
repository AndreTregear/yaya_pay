package com.yayapay.engine.wallet.providers.brazil

import com.yayapay.engine.data.model.WalletType
import javax.inject.Inject

class ItauPixProvider @Inject constructor() : PixProvider() {
    override val walletType = WalletType.PIX_ITAU
    override val packageNames = setOf("com.itau")
}
