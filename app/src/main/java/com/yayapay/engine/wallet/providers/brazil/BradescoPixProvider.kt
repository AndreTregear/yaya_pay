package com.yayapay.engine.wallet.providers.brazil

import com.yayapay.engine.data.model.WalletType
import javax.inject.Inject

class BradescoPixProvider @Inject constructor() : PixProvider() {
    override val walletType = WalletType.PIX_BRADESCO
    override val packageNames = setOf("com.bradesco")
}
