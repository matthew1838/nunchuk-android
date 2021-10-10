package com.nunchuk.android.usecase.room.transaction

import com.nunchuk.android.model.Device
import com.nunchuk.android.model.NunchukMatrixEvent
import com.nunchuk.android.nativelib.NunchukNativeSdk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

interface SignRoomTransactionUseCase {
    fun execute(initEventId: String, device: Device): Flow<NunchukMatrixEvent>
}

internal class SignRoomTransactionUseCaseImpl @Inject constructor(
    private val nativeSdk: NunchukNativeSdk
) : SignRoomTransactionUseCase {

    override fun execute(initEventId: String, device: Device) = flow {
        emit(nativeSdk.signRoomTransaction(initEventId, device))
    }

}