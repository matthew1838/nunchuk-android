/**************************************************************************
 * This file is part of the Nunchuk software (https://nunchuk.io/)        *
 * Copyright (C) 2022, 2023 Nunchuk                                       *
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

package com.nunchuk.android

import com.nunchuk.android.domain.di.IoDispatcher
import com.nunchuk.android.model.Amount
import com.nunchuk.android.model.Transaction
import com.nunchuk.android.nativelib.NunchukNativeSdk
import com.nunchuk.android.repository.PremiumWalletRepository
import com.nunchuk.android.usecase.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class ReplaceTransactionUseCase @Inject constructor(
    @IoDispatcher dispatcher: CoroutineDispatcher,
    private val nativeSdk: NunchukNativeSdk,
    private val repository: PremiumWalletRepository,
) : UseCase<ReplaceTransactionUseCase.Data, Transaction>(dispatcher) {

    override suspend fun execute(parameters: Data): Transaction {
        val transaction = nativeSdk.replaceTransaction(parameters.walletId, parameters.txId, Amount(value = parameters.newFee.toLong()))
        if (parameters.isAssistedWallet) {
            try {
                repository.createServerTransaction(
                    parameters.groupId,
                    parameters.walletId,
                    transaction.psbt,
                    transaction.memo,
                )
            } catch (e: Exception) {
                throw e
            }
        }
        return transaction
    }

    data class Data(val groupId: String?, val walletId: String, val txId: String, val newFee: Int, val isAssistedWallet: Boolean)
}