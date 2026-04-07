package com.yayapay.engine.wallet

import android.content.pm.PackageManager
import com.yayapay.engine.data.model.WalletType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRegistry @Inject constructor(
    private val providers: Set<@JvmSuppressWildcards WalletProvider>
) {
    private val byType: Map<WalletType, WalletProvider> =
        providers.associateBy { it.walletType }

    private val byPackage: Map<String, List<WalletProvider>> =
        providers.flatMap { provider -> provider.packageNames.map { pkg -> pkg to provider } }
            .groupBy({ it.first }, { it.second })

    fun getByPackage(packageName: String): List<WalletProvider> =
        byPackage[packageName] ?: emptyList()

    fun getByType(walletType: WalletType): WalletProvider? =
        byType[walletType]

    fun getAllRegisteredPackages(): Set<String> = byPackage.keys

    fun getAllProviders(): Collection<WalletProvider> = byType.values

    fun getInstalledWallets(packageManager: PackageManager): List<WalletProvider> =
        providers.filter { provider ->
            provider.packageNames.any { pkg ->
                try {
                    packageManager.getPackageInfo(pkg, 0)
                    true
                } catch (_: PackageManager.NameNotFoundException) {
                    false
                }
            }
        }
}
