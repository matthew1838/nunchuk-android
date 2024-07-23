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

package com.nunchuk.android.core.nfc

import android.net.Uri
import android.nfc.tech.NfcA
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.viewbinding.ViewBinding
import com.nunchuk.android.core.R
import com.nunchuk.android.core.domain.data.PortalAction
import com.nunchuk.android.core.domain.data.UpdateFirmware
import com.nunchuk.android.core.util.flowObserver
import com.nunchuk.android.widget.NCInputDialog
import com.nunchuk.android.widget.NUMBER_TYPE
import kotlinx.coroutines.flow.filter

abstract class BasePortalActivity<Binding : ViewBinding> : BaseNfcActivity<Binding>(), NfcActionListener {
    private val portalViewModel: PortalDeviceViewModel by viewModels()

    private val pickFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            portalViewModel.setPendingAction(UpdateFirmware(uri))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        flowObserver(portalViewModel.state) { state ->
            state.event ?: return@flowObserver
            when(state.event) {
                PortalDeviceEvent.RequestScan -> startNfcFlowIfNeeded()
                PortalDeviceEvent.IncorrectPin -> showInputCvcDialog(getString(R.string.nc_incorrect_cvc_please_try_again))
                PortalDeviceEvent.AskPin -> showInputCvcDialog()
                else -> onHandledPortalAction(state.event)
            }
            portalViewModel.markEventHandled()
        }

        flowObserver(nfcViewModel.nfcScanInfo.filter { it.requestCode == REQUEST_PORTAL }) {
            NfcA.get(it.tag)?.let { newTag -> portalViewModel.newTag(newTag) }
            nfcViewModel.clearScanInfo()
        }
    }

    abstract fun onHandledPortalAction(event : PortalDeviceEvent)

    fun handlePortalAction(action: PortalAction) {
        portalViewModel.setPendingAction(action)
    }

    fun selectFirmwareFile() {
        pickFile.launch("*/*")
    }

    private fun showInputCvcDialog(errorMessage: String? = null, descMessage: String? = null) {
        NCInputDialog(this)
            .showDialog(
                title = getString(R.string.nc_enter_pin),
                onConfirmed = { cvc ->
                    portalViewModel.updatePin(cvc)
                    startNfcFlowIfNeeded()
                },
                isMaskedInput = true,
                errorMessage = errorMessage,
                descMessage = descMessage,
                inputType = NUMBER_TYPE
            ).show()
    }

    private fun startNfcFlowIfNeeded() {
        if (portalViewModel.isConnectedToSdk) return
        startNfcFlow(REQUEST_PORTAL)
    }
}