package com.nunchuk.android.messages.usecase.message

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.matrix.android.sdk.api.session.room.Room
import javax.inject.Inject

interface LeaveRoomUseCase {
    fun execute(room: Room, reason: String? = ""): Flow<Unit>
}

internal class LeaveRoomUseCaseImpl @Inject constructor(
) : LeaveRoomUseCase {

    override fun execute(room: Room, reason: String?) = flow { emit(room.leave(reason)) }

}