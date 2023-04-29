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

package com.nunchuk.android.wallet.shared.components.recover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nunchuk.android.core.domain.ParseWalletDescriptorUseCase
import com.nunchuk.android.core.util.orUnknownError
import com.nunchuk.android.utils.onException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class RecoverSharedWalletViewModel @Inject constructor(
    private val parseWalletDescriptorUseCase: ParseWalletDescriptorUseCase
) : ViewModel() {
    val walletName: String
        get() = state.value.walletName

    private val _state = MutableStateFlow(RecoverSharedWalletState())
    val state = _state.asStateFlow()

    private val _event = MutableSharedFlow<RecoverSharedWalletEvent>()
    val event = _event.asSharedFlow()


    fun updateWalletName(walletName: String) {
        _state.update { it.copy(walletName = walletName) }
    }

    fun handleContinueEvent() = viewModelScope.launch {
        val currentState = _state.value
        if (currentState.walletName.isNotEmpty()) {
            _event.emit(RecoverSharedWalletEvent.WalletSetupDoneEvent(walletName = currentState.walletName))
        } else {
            _event.emit(RecoverSharedWalletEvent.WalletNameRequiredEvent)
        }
    }

    fun parseWalletDescriptor(content: String) {
        viewModelScope.launch {
            parseWalletDescriptorUseCase.execute(content)
                .flowOn(Dispatchers.IO)
                .onException { _event.emit(RecoverSharedWalletEvent.ShowError(it.message.orUnknownError())) }
                .flowOn(Dispatchers.Main)
                .collect {
                    _event.emit(RecoverSharedWalletEvent.RecoverSharedWalletSuccess(it))
                }
        }
    }
}