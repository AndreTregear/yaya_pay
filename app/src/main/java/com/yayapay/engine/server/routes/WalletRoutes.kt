package com.yayapay.engine.server.routes

import android.content.pm.PackageManager
import com.yayapay.engine.server.dto.WalletResponse
import com.yayapay.engine.wallet.WalletRegistry
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.walletRoutes(
    walletRegistry: WalletRegistry,
    packageManager: PackageManager?
) {
    get("/wallets") {
        val installed = packageManager?.let { pm ->
            walletRegistry.getInstalledWallets(pm).map { it.walletType }.toSet()
        } ?: emptySet()

        val wallets = walletRegistry.getAllProviders().map { provider ->
            WalletResponse(
                type = provider.walletType.name,
                displayName = provider.walletType.displayName,
                country = provider.walletType.country,
                currency = provider.currency.code,
                installed = provider.walletType in installed
            )
        }
        call.respond(wallets)
    }
}
