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

package com.nunchuk.android.main.components.tabs.wallet

import com.nunchuk.android.model.*
import com.nunchuk.android.type.Chain
import com.nunchuk.android.type.ConnectionStatus

internal data class WalletsState(
    val wallets: List<WalletExtended> = emptyList(),
    val signers: List<SingleSigner> = emptyList(),
    val masterSigners: List<MasterSigner> = emptyList(),
    val connectionStatus: ConnectionStatus? = null,
    val chain: Chain = Chain.MAIN
)

internal sealed class WalletsEvent {
    data class Loading(val loading: Boolean) : WalletsEvent()
    data class ShowErrorEvent(val e: Throwable?) : WalletsEvent()
    object AddWalletEvent : WalletsEvent()
    object ShowSignerIntroEvent : WalletsEvent()
    object WalletEmptySignerEvent : WalletsEvent()
    class NeedSetupSatsCard(val status: SatsCardStatus) : WalletsEvent()
    class NfcLoading(val loading: Boolean) : WalletsEvent()
    class GoToSatsCardScreen(val status: SatsCardStatus) : WalletsEvent()
    class GetTapSignerStatusSuccess(val status: TapSignerStatus) : WalletsEvent()
    class SatsCardUsedUp(val numberOfSlot: Int) : WalletsEvent()
}