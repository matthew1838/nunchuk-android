package com.nunchuk.android.usecase

import com.nunchuk.android.model.RoomWallet
import com.nunchuk.android.model.Wallet
import com.nunchuk.android.model.WalletExtended
import com.nunchuk.android.nativelib.NunchukNativeSdk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

interface GetWalletsUseCase {
    fun execute(): Flow<List<WalletExtended>>
}

internal class GetWalletsUseCaseImpl @Inject constructor(
    private val nativeSdk: NunchukNativeSdk
) : GetWalletsUseCase {

    override fun execute() = flow {
        val wallets = nativeSdk.getWallets()
        val rWalletIds = nativeSdk.getAllRoomWalletIds()
        emit(wallets.map { WalletExtended(it, it.isShared(rWalletIds)) })
    }

}

interface GetWalletUseCase {
    fun execute(walletId: String): Flow<WalletExtended>
}

internal class GetWalletUseCaseImpl @Inject constructor(
    private val nativeSdk: NunchukNativeSdk
) : GetWalletUseCase {

    override fun execute(walletId: String) = flow {
        val wallet = nativeSdk.getWallet(walletId)
        val rWallets = nativeSdk.getAllRoomWallet()
        val rWalletIds = rWallets.map(RoomWallet::walletId)
        val roomWallet = rWallets.first { wallet.id == it.walletId }
        emit(WalletExtended(wallet, wallet.isShared(rWalletIds), roomWallet))
    }

}

internal fun NunchukNativeSdk.getAllRoomWalletIds() = try {
    getAllRoomWallets().map(RoomWallet::walletId)
} catch (t: Throwable) {
    emptyList()
}

internal fun NunchukNativeSdk.getAllRoomWallet() = try {
    getAllRoomWallets()
} catch (t: Throwable) {
    emptyList()
}

private fun Wallet.isShared(rWalletIds: List<String>) = id in rWalletIds