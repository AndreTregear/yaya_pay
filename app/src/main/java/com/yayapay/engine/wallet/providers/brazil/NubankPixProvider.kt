package com.yayapay.engine.wallet.providers.brazil

import com.yayapay.engine.data.model.WalletType
import javax.inject.Inject

class NubankPixProvider @Inject constructor() : PixProvider() {
    override val walletType = WalletType.PIX_NUBANK
    override val packageNames = setOf("com.nu.production")
}
