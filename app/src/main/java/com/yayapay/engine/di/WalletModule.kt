package com.yayapay.engine.di

import com.yayapay.engine.wallet.WalletProvider
import com.yayapay.engine.wallet.providers.argentina.MercadoPagoArProvider
import com.yayapay.engine.wallet.providers.brazil.BradescoPixProvider
import com.yayapay.engine.wallet.providers.brazil.InterPixProvider
import com.yayapay.engine.wallet.providers.brazil.ItauPixProvider
import com.yayapay.engine.wallet.providers.brazil.NubankPixProvider
import com.yayapay.engine.wallet.providers.chile.MercadoPagoClProvider
import com.yayapay.engine.wallet.providers.colombia.DaviplataProvider
import com.yayapay.engine.wallet.providers.colombia.NequiProvider
import com.yayapay.engine.wallet.providers.mexico.MercadoPagoMxProvider
import com.yayapay.engine.wallet.providers.peru.PlinProvider
import com.yayapay.engine.wallet.providers.peru.YapeProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class WalletModule {

    @Binds @IntoSet abstract fun yape(impl: YapeProvider): WalletProvider
    @Binds @IntoSet abstract fun plin(impl: PlinProvider): WalletProvider
    @Binds @IntoSet abstract fun nequi(impl: NequiProvider): WalletProvider
    @Binds @IntoSet abstract fun daviplata(impl: DaviplataProvider): WalletProvider
    @Binds @IntoSet abstract fun mercadoPagoMx(impl: MercadoPagoMxProvider): WalletProvider
    @Binds @IntoSet abstract fun nubankPix(impl: NubankPixProvider): WalletProvider
    @Binds @IntoSet abstract fun itauPix(impl: ItauPixProvider): WalletProvider
    @Binds @IntoSet abstract fun bradescoPix(impl: BradescoPixProvider): WalletProvider
    @Binds @IntoSet abstract fun interPix(impl: InterPixProvider): WalletProvider
    @Binds @IntoSet abstract fun mercadoPagoAr(impl: MercadoPagoArProvider): WalletProvider
    @Binds @IntoSet abstract fun mercadoPagoCl(impl: MercadoPagoClProvider): WalletProvider
}
