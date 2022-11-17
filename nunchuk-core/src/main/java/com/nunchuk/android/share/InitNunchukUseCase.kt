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

package com.nunchuk.android.share

import com.nunchuk.android.core.domain.GetAppSettingUseCase
import com.nunchuk.android.core.matrix.SessionHolder
import com.nunchuk.android.core.util.BLOCKCHAIN_STATUS
import com.nunchuk.android.core.util.toMatrixContent
import com.nunchuk.android.log.fileLog
import com.nunchuk.android.model.*
import com.nunchuk.android.nativelib.NunchukNativeSdk
import com.nunchuk.android.type.ConnectionStatus
import com.nunchuk.android.utils.DeviceManager
import com.nunchuk.android.utils.trySafe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

interface InitNunchukUseCase {
    fun execute(passphrase: String = "", accountId: String): Flow<Unit>
}

internal class InitNunchukUseCaseImpl @Inject constructor(
    private val getAppSettingUseCase: GetAppSettingUseCase,
    private val nativeSdk: NunchukNativeSdk,
    private val deviceManager: DeviceManager,
    private val sessionHolder: SessionHolder
) : InitNunchukUseCase {

    override fun execute(
        passphrase: String,
        accountId: String
    ) = getAppSettingUseCase.execute().flatMapConcat {
        initNunchuk(appSettings = it, passphrase = passphrase, accountId = accountId, deviceId = deviceManager.getDeviceId())
    }

    private fun initNunchuk(appSettings: AppSettings, passphrase: String, accountId: String, deviceId: String) = flow {
        initReceiver()
        fileLog(message = "start nativeSdk initNunchuk")
        emit(nativeSdk.run {
            initNunchuk(appSettings = appSettings, passphrase = passphrase, accountId = accountId, deviceId = deviceId)
        })
        fileLog("end nativeSdk initNunchuk")
    }.flowOn(Dispatchers.IO)

    private fun initReceiver() {
        SendEventHelper.executor = object : SendEventExecutor {
            override fun execute(roomId: String, type: String, content: String, ignoreError: Boolean): String {
                if (sessionHolder.hasActiveSession()) {
                    sessionHolder.getSafeActiveSession()?.roomService()?.getRoom(roomId)?.apply {
                        trySafe { sendService().sendEvent(eventType = type, content = content.toMatrixContent()) }
                    }
                }
                return ""
            }
        }

        ConnectionStatusHelper.executor = object : ConnectionStatusExecutor {
            override fun execute(connectionStatus: ConnectionStatus, percent: Int) {
                BLOCKCHAIN_STATUS = connectionStatus
            }
        }
    }
}
