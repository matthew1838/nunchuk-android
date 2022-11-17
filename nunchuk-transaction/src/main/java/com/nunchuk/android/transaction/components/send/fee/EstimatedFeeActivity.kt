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

import android.app.Activity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.nunchuk.android.core.base.BaseActivity
import com.nunchuk.android.core.nfc.SweepType
import com.nunchuk.android.core.util.getBTCAmount
import com.nunchuk.android.core.util.getUSDAmount
import com.nunchuk.android.core.util.pureBTC
import com.nunchuk.android.core.util.toAmount
import com.nunchuk.android.model.EstimateFeeRates
import com.nunchuk.android.model.SatsCardSlot
import com.nunchuk.android.transaction.R
import com.nunchuk.android.transaction.components.send.fee.EstimatedFeeEvent.EstimatedFeeCompletedEvent
import com.nunchuk.android.transaction.components.send.fee.EstimatedFeeEvent.EstimatedFeeErrorEvent
import com.nunchuk.android.transaction.components.utils.toTitle
import com.nunchuk.android.transaction.databinding.ActivityTransactionEstimateFeeBinding
import com.nunchuk.android.utils.safeManualFee
import com.nunchuk.android.utils.textChanges
import com.nunchuk.android.widget.NCToastMessage
import com.nunchuk.android.widget.util.setLightStatusBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class EstimatedFeeActivity : BaseActivity<ActivityTransactionEstimateFeeBinding>() {

    private val args: EstimatedFeeArgs by lazy { EstimatedFeeArgs.deserializeFrom(intent) }

    private val viewModel: EstimatedFeeViewModel by viewModels()

    override fun initializeBinding() = ActivityTransactionEstimateFeeBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setLightStatusBar()

        viewModel.init(
            walletId = args.walletId,
            address = args.address,
            sendAmount = args.outputAmount,
            slots = args.slots
        )
        setupViews()
        observeEvent()
    }

    private fun observeEvent() {
        viewModel.event.observe(this, ::handleEvent)
        viewModel.state.observe(this, ::handleState)
    }

    @OptIn(FlowPreview::class)
    private fun setupViews() {
        binding.toolbarTitle.text = args.sweepType.toTitle(this)
        val subtractFeeFromAmount = args.subtractFeeFromAmount
        binding.subtractFeeCheckBox.isChecked = subtractFeeFromAmount
        binding.subtractFeeCheckBox.isEnabled = !subtractFeeFromAmount
        viewModel.handleSubtractFeeSwitch(subtractFeeFromAmount)

        binding.subtractFeeCheckBox.setOnCheckedChangeListener { _, isChecked -> viewModel.handleSubtractFeeSwitch(isChecked) }
        binding.manualFeeCheckBox.setOnCheckedChangeListener { _, isChecked -> handleManualFeeSwitch(isChecked) }
        binding.feeRateInput.textChanges()
            .onEach { binding.btnContinue.tag = true }
            .debounce(500)
            .onEach { viewModel.updateFeeRate(it.safeManualFee()) }
            .onEach { binding.btnContinue.tag = false }
            .launchIn(lifecycleScope)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.btnContinue.setOnClickListener {
            val isCalculatingFee = it.tag
            if (isCalculatingFee is Boolean && isCalculatingFee) {
                NCToastMessage(this).showWarning(getString(R.string.nc_wait_to_estimate_fee))
                return@setOnClickListener
            }
            if (binding.manualFeeCheckBox.isChecked.not() || viewModel.validateFeeRate(binding.feeRateInput.text.safeManualFee())) {
                viewModel.handleContinueEvent()
            }
        }

        bindSubtotal(args.outputAmount)
    }

    private fun handleManualFeeSwitch(isChecked: Boolean) {
        viewModel.handleManualFeeSwitch(isChecked)
        binding.feeRateInput.setText("${viewModel.defaultRate / 1000}")
    }

    private fun bindSubtotal(subtotal: Double) {
        binding.totalAmountBTC.text = subtotal.getBTCAmount()
        binding.totalAmountUSD.text = subtotal.getUSDAmount()
    }

    private fun handleState(state: EstimatedFeeState) {
        binding.estimatedFeeBTC.text = state.estimatedFee.getBTCAmount()
        binding.estimatedFeeUSD.text = state.estimatedFee.getUSDAmount()

        if (state.subtractFeeFromAmount) {
            bindSubtotal(args.outputAmount)
        } else {
            bindSubtotal((args.outputAmount + state.estimatedFee.pureBTC()).coerceAtMost(args.availableAmount))
        }

        binding.manualFeeDetails.isVisible = state.manualFeeDetails
        bindEstimateFeeRates(state.estimateFeeRates)
    }

    private fun bindEstimateFeeRates(estimateFeeRates: EstimateFeeRates) {
        binding.priorityRateValue.text = estimateFeeRates.priorityRate.toFeeRate()
        binding.standardRateValue.text = estimateFeeRates.standardRate.toFeeRate()
        binding.economicalRateValue.text = estimateFeeRates.economicRate.toFeeRate()
    }

    private fun handleEvent(event: EstimatedFeeEvent) {
        when (event) {
            is EstimatedFeeErrorEvent -> onEstimatedFeeError(event)
            is EstimatedFeeCompletedEvent -> openTransactionConfirmScreen(
                estimatedFee = event.estimatedFee,
                subtractFeeFromAmount = event.subtractFeeFromAmount,
                manualFeeRate = event.manualFeeRate
            )
            is EstimatedFeeEvent.Loading -> showOrHideLoading(event.isLoading)
            is EstimatedFeeEvent.InvalidManualFee -> NCToastMessage(this).showError(getString(R.string.nc_input_fee_invalid_error))
            is EstimatedFeeEvent.GetFeeRateSuccess -> {}
        }
    }

    private fun onEstimatedFeeError(event: EstimatedFeeErrorEvent) {
        NCToastMessage(this).showError(event.message)
    }

    private fun openTransactionConfirmScreen(estimatedFee: Double, subtractFeeFromAmount: Boolean, manualFeeRate: Int) {
        navigator.openTransactionConfirmScreen(
            activityContext = this,
            walletId = args.walletId,
            outputAmount = args.outputAmount,
            availableAmount = args.availableAmount,
            address = args.address,
            privateNote = args.privateNote,
            estimatedFee = estimatedFee,
            subtractFeeFromAmount = subtractFeeFromAmount,
            manualFeeRate = manualFeeRate,
            sweepType = args.sweepType,
            slots = args.slots
        )
    }

    companion object {

        fun start(
            activityContext: Activity,
            walletId: String,
            outputAmount: Double,
            availableAmount: Double,
            address: String,
            privateNote: String,
            subtractFeeFromAmount: Boolean = false,
            sweepType: SweepType = SweepType.NONE,
            slots: List<SatsCardSlot>
        ) {
            activityContext.startActivity(
                EstimatedFeeArgs(
                    walletId = walletId,
                    outputAmount = outputAmount,
                    availableAmount = availableAmount,
                    address = address,
                    privateNote = privateNote,
                    subtractFeeFromAmount = subtractFeeFromAmount,
                    sweepType = sweepType,
                    slots = slots
                ).buildIntent(activityContext)
            )
        }

    }

}

internal fun Int.toFeeRate() = (this / 1000).toString() + " sat/vB"
internal fun Int.toFeeRateInBtc() = (this / 1000).toAmount().getBTCAmount() + "/vB"
