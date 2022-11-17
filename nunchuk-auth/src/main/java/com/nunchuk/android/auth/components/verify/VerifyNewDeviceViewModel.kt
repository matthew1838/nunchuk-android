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

package com.nunchuk.android.auth.components.verify

import androidx.lifecycle.viewModelScope
import com.nunchuk.android.arch.vm.NunchukViewModel
import com.nunchuk.android.auth.components.verify.VerifyNewDeviceEvent.*
import com.nunchuk.android.auth.domain.VerifyNewDeviceUseCase
import com.nunchuk.android.core.account.AccountManager
import com.nunchuk.android.core.guestmode.SignInMode
import com.nunchuk.android.core.guestmode.SignInModeHolder
import com.nunchuk.android.core.persistence.NCSharePreferences
import com.nunchuk.android.core.util.orUnknownError
import com.nunchuk.android.share.InitNunchukUseCase
import com.nunchuk.android.utils.onException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class VerifyNewDeviceViewModel @Inject constructor(
    private val verifyNewDeviceUseCase: VerifyNewDeviceUseCase,
    private val initNunchukUseCase: InitNunchukUseCase,
    private val accountManager: AccountManager,
    private val signInModeHolder: SignInModeHolder
) : NunchukViewModel<Unit, VerifyNewDeviceEvent>() {

    override val initialState = Unit

    private var token: String? = null
    private var encryptedDeviceId: String? = null

    fun handleVerifyNewDevice(
        email: String,
        loginHalfToken: String,
        pin: String,
        deviceId: String,
        staySignedIn: Boolean
    ) {
        viewModelScope.launch {
            verifyNewDeviceUseCase.execute(
                email = email,
                loginHalfToken = loginHalfToken,
                pin = pin,
                deviceId = deviceId,
                staySignedIn = staySignedIn
            ).flowOn(Dispatchers.IO)
                .onStart { event(ProcessingEvent) }
                .onException { event(SignInErrorEvent(message = it.message.orUnknownError())) }
                .flatMapConcat {
                    token = it.first
                    encryptedDeviceId = it.second
                    initNunchuk()
                }
                .flowOn(Dispatchers.Main)
                .collect {
                    signInModeHolder.setCurrentMode(SignInMode.EMAIL)
                    event(SignInSuccessEvent(token = token.orEmpty(), encryptedDeviceId = encryptedDeviceId.orEmpty()))
                }
        }
    }

    private fun initNunchuk(): Flow<Unit> {
        val account = accountManager.getAccount()
        return initNunchukUseCase.execute(accountId = account.email)
            .flowOn(Dispatchers.IO)
            .onException { event(SignInErrorEvent(message = it.message.orUnknownError())) }
    }

}