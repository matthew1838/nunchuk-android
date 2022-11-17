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

package com.nunchuk.android.transaction.components.send.fee

import androidx.lifecycle.viewModelScope
import com.nunchuk.android.arch.vm.NunchukViewModel
import com.nunchuk.android.core.util.orUnknownError
import com.nunchuk.android.core.util.pureBTC
import com.nunchuk.android.core.util.toAmount
import com.nunchuk.android.model.EstimateFeeRates
import com.nunchuk.android.model.Result.Error
import com.nunchuk.android.model.Result.Success
import com.nunchuk.android.model.SatsCardSlot
import com.nunchuk.android.model.defaultRate
import com.nunchuk.android.transaction.components.send.confirmation.toManualFeeRate
import com.nunchuk.android.transaction.components.send.fee.EstimatedFeeEvent.EstimatedFeeCompletedEvent
import com.nunchuk.android.transaction.components.send.fee.EstimatedFeeEvent.EstimatedFeeErrorEvent
import com.nunchuk.android.usecase.DraftSatsCardTransactionUseCase
import com.nunchuk.android.usecase.DraftTransactionUseCase
import com.nunchuk.android.usecase.EstimateFeeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EstimatedFeeViewModel @Inject constructor(
    private val estimateFeeUseCase: EstimateFeeUseCase,
    private val draftTransactionUseCase: DraftTransactionUseCase,
    private val draftSatsCardTransactionUseCase: DraftSatsCardTransactionUseCase
) : NunchukViewModel<EstimatedFeeState, EstimatedFeeEvent>() {

    private var walletId: String = ""
    private var address: String = ""
    private var sendAmount: Double = 0.0
    private var draftTranJob: Job? = null
    private var slots = mutableListOf<SatsCardSlot>()

    override val initialState = EstimatedFeeState()

    fun init(walletId: String, address: String, sendAmount: Double, slots: List<SatsCardSlot>) {
        this.walletId = walletId
        this.address = address
        this.sendAmount = sendAmount
        this.slots.apply {
            clear()
            addAll(slots)
        }
        getEstimateFeeRates()
    }

    fun getEstimateFeeRates() {
        viewModelScope.launch {
            val result = estimateFeeUseCase(Unit)
            if (result.isSuccess) {
                setEvent(EstimatedFeeEvent.GetFeeRateSuccess(result.getOrThrow()))
                updateState { copy(estimateFeeRates = result.getOrThrow(), manualFeeRate = result.getOrThrow().defaultRate) }
            } else {
                setEvent(EstimatedFeeErrorEvent(result.exceptionOrNull()?.message.orUnknownError()))
                updateState { copy(estimateFeeRates = EstimateFeeRates()) }
            }
            if (walletId.isNotEmpty()) {
                draftTransaction()
            }
        }
    }

    private fun draftTransaction() {
        draftTranJob?.cancel()
        draftTranJob = viewModelScope.launch {
            if (slots.isEmpty()) {
                draftNormalTransaction()
            } else {
                draftSatsCardTransaction()
            }
        }
    }

    private suspend fun draftNormalTransaction() {
        val state = getState()
        setEvent(EstimatedFeeEvent.Loading(true))
        when (val result = draftTransactionUseCase.execute(
            walletId = walletId,
            outputs = mapOf(address to sendAmount.toAmount()),
            subtractFeeFromAmount = state.subtractFeeFromAmount,
            feeRate = state.manualFeeRate.toManualFeeRate()
        )) {
            is Success -> updateState { copy(estimatedFee = result.data.fee) }
            is Error -> {
                if (result.exception !is CancellationException) {
                    setEvent(EstimatedFeeErrorEvent(result.exception.message.orEmpty()))
                }
            }
        }
        setEvent(EstimatedFeeEvent.Loading(false))
    }

    private suspend fun draftSatsCardTransaction() {
        setEvent(EstimatedFeeEvent.Loading(true))
        val result = draftSatsCardTransactionUseCase(
            DraftSatsCardTransactionUseCase.Data(
                address,
                slots,
                getState().manualFeeRate
            )
        )
        setEvent(EstimatedFeeEvent.Loading(false))
        if (result.isSuccess) {
            updateState { copy(estimatedFee = result.getOrThrow().fee) }
        } else {
            if (result.exceptionOrNull() !is CancellationException) {
                setEvent(EstimatedFeeErrorEvent(result.exceptionOrNull()?.message.orUnknownError()))
            }
        }
    }

    fun handleSubtractFeeSwitch(checked: Boolean) {
        updateState { copy(subtractFeeFromAmount = checked) }
        draftTransaction()
    }

    fun handleManualFeeSwitch(checked: Boolean) {
        updateState { copy(manualFeeDetails = checked) }
        updateFeeRate(defaultRate)
    }

    fun handleContinueEvent() {
        getState().apply {
            event(
                EstimatedFeeCompletedEvent(
                    estimatedFee = estimatedFee.pureBTC(),
                    subtractFeeFromAmount = subtractFeeFromAmount,
                    manualFeeRate = manualFeeRate
                )
            )
        }
    }

    fun updateFeeRate(feeRate: Int) {
        val newFeeRate = feeRate.coerceAtLeast(getState().estimateFeeRates.minimumFee)
        if (newFeeRate != getState().manualFeeRate) {
            updateState { copy(manualFeeRate = newFeeRate) }
            draftTransaction()
        }
    }

    fun validateFeeRate(feeRate: Int): Boolean {
        if (feeRate < getState().estimateFeeRates.minimumFee) {
            setEvent(EstimatedFeeEvent.InvalidManualFee)
            return false
        }
        return true
    }

    val defaultRate: Int
        get() = getState().estimateFeeRates.defaultRate
}