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

package com.nunchuk.android.transaction.components.send.confirmation

import com.nunchuk.android.model.Amount
import com.nunchuk.android.model.Transaction

sealed class TransactionConfirmEvent {
    object LoadingEvent : TransactionConfirmEvent()
    data class CreateTxSuccessEvent(val txId: String) : TransactionConfirmEvent()
    data class CreateTxErrorEvent(val message: String) : TransactionConfirmEvent()
    data class UpdateChangeAddress(val address: String, val amount: Amount) : TransactionConfirmEvent()
    data class InitRoomTransactionError(val message: String) : TransactionConfirmEvent()
    data class InitRoomTransactionSuccess(val roomId: String) : TransactionConfirmEvent()
}