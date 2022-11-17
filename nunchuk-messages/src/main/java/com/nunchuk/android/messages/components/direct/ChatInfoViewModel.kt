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

package com.nunchuk.android.messages.components.direct

import androidx.lifecycle.viewModelScope
import com.nunchuk.android.arch.vm.NunchukViewModel
import com.nunchuk.android.core.account.AccountManager
import com.nunchuk.android.core.matrix.SessionHolder
import com.nunchuk.android.core.util.pureBTC
import com.nunchuk.android.messages.components.direct.ChatInfoEvent.CreateSharedWalletEvent
import com.nunchuk.android.messages.components.direct.ChatInfoEvent.CreateTransactionEvent
import com.nunchuk.android.messages.util.getRoomMemberList
import com.nunchuk.android.model.Contact
import com.nunchuk.android.model.RoomWallet
import com.nunchuk.android.model.Wallet
import com.nunchuk.android.share.GetContactsUseCase
import com.nunchuk.android.usecase.GetRoomWalletUseCase
import com.nunchuk.android.usecase.GetWalletUseCase
import com.nunchuk.android.utils.onException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.session.room.Room
import javax.inject.Inject

@HiltViewModel
class ChatInfoViewModel @Inject constructor(
    accountManager: AccountManager,
    private val getContactsUseCase: GetContactsUseCase,
    private val getRoomWalletUseCase: GetRoomWalletUseCase,
    private val getWalletUseCase: GetWalletUseCase,
    private val sessionHolder: SessionHolder
) : NunchukViewModel<ChatInfoState, ChatInfoEvent>() {

    private val currentId = accountManager.getAccount().chatId

    private lateinit var room: Room

    override val initialState = ChatInfoState()

    fun initialize(roomId: String) {
        sessionHolder.getSafeActiveSession()?.roomService()?.getRoom(roomId)?.let(::onRetrievedRoom) ?: event(ChatInfoEvent.RoomNotFoundEvent)
    }

    private fun onRetrievedRoom(room: Room) {
        this.room = room
        viewModelScope.launch {
            getContactsUseCase.execute()
                .catch {  }
                .collect(::onRetrievedContacts)
        }
        getRoomWallet()
    }

    private fun getRoomWallet() {
        getRoomWalletUseCase.execute(roomId = room.roomId)
            .onException { }
            .onEach { onGetRoomWallet(it) }
            .launchIn(viewModelScope)
    }

    private fun onGetRoomWallet(roomWallet: RoomWallet?) {
        updateState { copy(roomWallet = roomWallet) }
        roomWallet?.let { rWallet ->
            getWalletUseCase.execute(walletId = rWallet.walletId)
                .onException {}
                .onEach { onGetWallet(it.wallet) }
                .launchIn(viewModelScope)
        }
    }

    private fun onGetWallet(wallet: Wallet) {
        updateState { copy(wallet = wallet) }
    }

    fun createWalletOrTransaction() {
        val wallet = getState().wallet
        if (wallet == null) {
            event(CreateSharedWalletEvent)
        } else if (wallet.balance.value > 0L) {
            event(CreateTransactionEvent(roomId = room.roomId, walletId = wallet.id, availableAmount = wallet.balance.pureBTC()))
        }
    }

    private fun onRetrievedContacts(contacts: List<Contact>) {
        val directUserId = room.roomSummary()?.directUserId
        if (directUserId != null) {
            getContact(contacts, directUserId)
        } else {
            val roomMemberList = room.getRoomMemberList()
            roomMemberList.firstOrNull { it.userId != currentId }?.let {
                getContact(contacts, it.userId)
            }
        }
    }

    private fun getContact(contacts: List<Contact>, directUserId: String?) {
        contacts.firstOrNull { it.chatId == directUserId }?.let { contact ->
            updateState { copy(contact = contact) }
        }
    }

}