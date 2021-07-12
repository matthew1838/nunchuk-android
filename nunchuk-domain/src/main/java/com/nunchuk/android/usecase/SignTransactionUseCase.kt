package com.nunchuk.android.usecase

import com.nunchuk.android.model.Device
import com.nunchuk.android.model.Result
import com.nunchuk.android.model.Transaction
import com.nunchuk.android.nativelib.NunchukNativeSdk
import javax.inject.Inject

interface SignTransactionUseCase {
    suspend fun execute(walletId: String, txId: String, device: Device): Result<Transaction>
}

internal class SignTransactionUseCaseImpl @Inject constructor(
    private val nativeSdk: NunchukNativeSdk
) : BaseUseCase(), SignTransactionUseCase {

    override suspend fun execute(walletId: String, txId: String, device: Device) = exe {
        nativeSdk.signTransaction(
            walletId = walletId,
            txId = txId,
            device = device
        )
    }

}