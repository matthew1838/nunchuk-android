/**************************************************************************
 * This file is part of the Nunchuk software (https://nunchuk.io/)        *							          *
 * Copyright (C) 2022 Nunchuk								              *
 *                                                                        *
 * This program is free software; you can redistribute it and/or          *
 * modify it under the terms of the GNU General Public License            *
 * as published by the Free Software Foundation; either version 3         *
 * of the License, or (at your option) any later version.                 *
 *                                                                        *
 * This program is distributed in the hope that it will be useful,        *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of         *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          *
 * GNU General Public License for more details.                           *
 *                                                                        *
 * You should have received a copy of the GNU General Public License      *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.  *
 *                                                                        *
 **************************************************************************/

package com.nunchuk.android.main

import androidx.lifecycle.viewModelScope
import com.nunchuk.android.arch.vm.NunchukViewModel
import com.nunchuk.android.core.data.model.SyncStateMatrixResponse
import com.nunchuk.android.core.domain.LoginWithMatrixUseCase
import com.nunchuk.android.core.matrix.SessionHolder
import com.nunchuk.android.core.matrix.SyncStateHolder
import com.nunchuk.android.core.matrix.SyncStateMatrixUseCase
import com.nunchuk.android.core.profile.GetUserProfileUseCase
import com.nunchuk.android.core.util.orFalse
import com.nunchuk.android.log.fileLog
import com.nunchuk.android.messages.usecase.message.CreateRoomWithTagUseCase
import com.nunchuk.android.messages.util.STATE_NUNCHUK_SYNC
import com.nunchuk.android.utils.onException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
internal class SyncRoomViewModel @Inject constructor(
    private val createRoomWithTagUseCase: CreateRoomWithTagUseCase,
    private val syncStateMatrixUseCase: SyncStateMatrixUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val loginWithMatrixUseCase: LoginWithMatrixUseCase,
    private val syncStateHolder: SyncStateHolder,
    private val sessionHolder: SessionHolder
) : NunchukViewModel<Unit, SyncRoomEvent>() {

    override val initialState = Unit

    fun createRoomWithTagSync() {
        viewModelScope.launch {
            syncStateHolder.lockStateSyncRoom.withLock {
                createRoomWithTagUseCase.execute(
                    STATE_NUNCHUK_SYNC,
                    listOf(sessionHolder.getSafeActiveSession()?.sessionParams?.userId.orEmpty()),
                    STATE_NUNCHUK_SYNC
                )
                    .flowOn(IO)
                    .onException { }
                    .flowOn(Main)
                    .collect {
                        event(SyncRoomEvent.CreateSyncRoomSucceedEvent(it.roomId))
                    }
            }
        }
    }

    fun findSyncRoom() {
        viewModelScope.launch {
            syncStateHolder.lockStateSyncRoom.withLock {
                syncStateMatrixUseCase.execute()
                    .flowOn(IO)
                    .onException { }
                    .flowOn(Main)
                    .collect {
                        handleSyncStateMatrix(it)?.let { syncRoomId ->
                            Timber.d("Have sync room: $syncRoomId")
                            event(SyncRoomEvent.FindSyncRoomSuccessEvent(syncRoomId))
                        } ?: run {
                            Timber.d("Don't have sync room")
                            event(SyncRoomEvent.FindSyncRoomFailedEvent(0))
                        }
                    }
            }
        }
    }

    private fun handleSyncStateMatrix(response: SyncStateMatrixResponse): String? {
        val mapSyncRooms = response.rooms?.join?.filter {
            it.value.accountData?.events?.any { event ->
                event.type == EVENT_TYPE_TAG_ROOM && event.content?.tags?.get(EVENT_TYPE_SYNC) != null
            }.orFalse()
        }

        // in the worst case, maybe we will have more than one sync room.
        if ((mapSyncRooms?.size ?: 0) > 1) {
            return mapSyncRooms?.filter {
                it.value.timeline?.events?.any { event ->
                    event.type == EVENT_TYPE_SYNC || event.type == EVENT_TYPE_SYNC_ERROR
                }.orFalse()
            }?.map {
                it.key
            }?.firstOrNull()
        }
        return mapSyncRooms?.map { it.key }?.firstOrNull()
    }

    fun setupMatrix(token: String, encryptedDeviceId: String) {
        fileLog("Start setup matrix")
        viewModelScope.launch {
            getUserProfileUseCase.execute()
                .flowOn(IO)
                .onException {  }
                .flatMapConcat {
                    loginWithMatrix(
                        userName = it,
                        password = token,
                        encryptedDeviceId = encryptedDeviceId
                    ).onStart {
                        fileLog("start login matrix")
                    }.onCompletion {
                        fileLog("end login matrix")
                    }
                }
                .collect {
                    event(SyncRoomEvent.LoginMatrixSucceedEvent(it))
                }
        }
    }

    private fun loginWithMatrix(
        userName: String,
        password: String,
        encryptedDeviceId: String
    ) = loginWithMatrixUseCase.execute(
        userName = userName,
        password = password,
        encryptedDeviceId = encryptedDeviceId
    ).onException {}

    companion object {
        private const val TAG = "SyncRoomViewModel"
        private const val EVENT_TYPE_SYNC = "io.nunchuk.sync"
        private const val EVENT_TYPE_SYNC_ERROR = "io.nunchuk.error"
        private const val EVENT_TYPE_TAG_ROOM = "m.tag"
    }
}