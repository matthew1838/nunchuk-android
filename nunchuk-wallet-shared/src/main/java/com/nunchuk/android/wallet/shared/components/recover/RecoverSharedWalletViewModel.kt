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

import androidx.lifecycle.viewModelScope
import com.nunchuk.android.arch.vm.NunchukViewModel
import com.nunchuk.android.core.domain.ParseWalletDescriptorUseCase
import com.nunchuk.android.core.util.orUnknownError
import com.nunchuk.android.utils.onException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class RecoverSharedWalletViewModel @Inject constructor(
    private val parseWalletDescriptorUseCase: ParseWalletDescriptorUseCase
) : NunchukViewModel<RecoverSharedWalletState, RecoverSharedWalletEvent>() {
    val walletName: String?
        get() = state.value?.walletName

    override val initialState = RecoverSharedWalletState()

    fun updateWalletName(walletName: String) {
        updateState { copy(walletName = walletName) }
    }

    fun handleContinueEvent() {
        val currentState = getState()
        if (currentState.walletName.isNotEmpty()) {
            event(RecoverSharedWalletEvent.WalletSetupDoneEvent(walletName = currentState.walletName))
        } else {
            event(RecoverSharedWalletEvent.WalletNameRequiredEvent)
        }
    }

    fun parseWalletDescriptor(content: String) {
        viewModelScope.launch {
            parseWalletDescriptorUseCase.execute(content)
                .flowOn(Dispatchers.IO)
                .onException { setEvent(RecoverSharedWalletEvent.ShowError(it.message.orUnknownError())) }
                .flowOn(Dispatchers.Main)
                .collect {
                    event(RecoverSharedWalletEvent.RecoverSharedWalletSuccess(it))
                }
        }
    }

}