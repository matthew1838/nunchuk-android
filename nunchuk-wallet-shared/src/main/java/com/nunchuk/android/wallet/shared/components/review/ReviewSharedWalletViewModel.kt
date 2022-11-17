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

package com.nunchuk.android.wallet.shared.components.review

import androidx.lifecycle.viewModelScope
import com.nunchuk.android.arch.vm.NunchukViewModel
import com.nunchuk.android.core.matrix.SessionHolder
import com.nunchuk.android.core.util.orUnknownError
import com.nunchuk.android.model.SingleSigner
import com.nunchuk.android.type.AddressType
import com.nunchuk.android.type.WalletType
import com.nunchuk.android.usecase.InitWalletUseCase
import com.nunchuk.android.utils.onException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ReviewSharedWalletViewModel @Inject constructor(
    private val initWalletUseCase: InitWalletUseCase,
    private val sessionHolder: SessionHolder
) : NunchukViewModel<ReviewSharedWalletState, ReviewSharedWalletEvent>() {

    override val initialState = ReviewSharedWalletState()

    fun init() {
        updateState { initialState }
    }

    fun handleContinueEvent(
        walletName: String,
        walletType: WalletType,
        addressType: AddressType,
        totalSigns: Int,
        requireSigns: Int,
        signers: List<SingleSigner>
    ) {
        initWallet(
            roomId = sessionHolder.getActiveRoomId(),
            walletName = walletName,
            walletType = walletType,
            addressType = addressType,
            totalSigns = totalSigns,
            requireSigns = requireSigns,
            signers = signers
        )
    }

    private fun initWallet(
        roomId: String,
        walletName: String,
        walletType: WalletType,
        addressType: AddressType,
        totalSigns: Int,
        requireSigns: Int,
        signers : List<SingleSigner>
    ) {
        viewModelScope.launch {
            initWalletUseCase.execute(
                roomId = roomId,
                name = walletName,
                totalSigns = totalSigns,
                requireSigns = requireSigns,
                addressType = addressType,
                isEscrow = walletType == WalletType.ESCROW,
                des = "",
                signers = signers
            )
                .flowOn(Dispatchers.IO)
                .onException { event(ReviewSharedWalletEvent.InitWalletErrorEvent(it.message.orUnknownError())) }
                .flowOn(Dispatchers.Main)
                .collect {
                    event(ReviewSharedWalletEvent.InitWalletCompletedEvent)
                }
        }
    }

}