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

package com.nunchuk.android.usecase

import com.nunchuk.android.model.Amount
import com.nunchuk.android.model.Result
import com.nunchuk.android.model.Transaction
import com.nunchuk.android.model.UnspentOutput
import com.nunchuk.android.nativelib.NunchukNativeSdk
import javax.inject.Inject

interface CreateTransactionUseCase {
    suspend fun execute(
        walletId: String,
        outputs: Map<String, Amount>,
        memo: String = "",
        inputs: List<UnspentOutput> = emptyList(),
        feeRate: Amount = Amount(-1),
        subtractFeeFromAmount: Boolean = false
    ): Result<Transaction>
}

internal class CreateTransactionUseCaseImpl @Inject constructor(
    private val nativeSdk: NunchukNativeSdk
) : BaseUseCase(), CreateTransactionUseCase {
    override suspend fun execute(
        walletId: String,
        outputs: Map<String, Amount>,
        memo: String,
        inputs: List<UnspentOutput>,
        feeRate: Amount,
        subtractFeeFromAmount: Boolean
    ) = exe {
        nativeSdk.createTransaction(
            walletId = walletId,
            outputs = outputs,
            memo = memo,
            inputs = inputs,
            feeRate = feeRate,
            subtractFeeFromAmount = subtractFeeFromAmount
        )
    }

}