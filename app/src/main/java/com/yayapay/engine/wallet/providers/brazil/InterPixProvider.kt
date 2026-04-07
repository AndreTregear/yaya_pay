package com.yayapay.engine.wallet.providers.brazil

import com.yayapay.engine.data.model.WalletType
import javax.inject.Inject

class InterPixProvider @Inject constructor() : PixProvider() {
    override val walletType = WalletType.PIX_INTER
    override val packageNames = setOf("br.com.intermedium")
}
