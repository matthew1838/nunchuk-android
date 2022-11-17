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

package com.nunchuk.android.core.nfc

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nunchuk.android.exception.NCNativeException
import com.nunchuk.android.model.MasterSigner
import com.nunchuk.android.model.Result
import com.nunchuk.android.model.TapProtocolException
import com.nunchuk.android.usecase.GetMasterSignerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class NfcViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getMasterSignerUseCase: GetMasterSignerUseCase
) : ViewModel() {
    val masterSignerId = savedStateHandle.get<String>(EXTRA_MASTER_SIGNER_ID).orEmpty()

    init {
        if (masterSignerId.isNotEmpty()) {
            viewModelScope.launch {
                val result = getMasterSignerUseCase.execute(masterSignerId)
                if (result is Result.Success) {
                    masterSigner = result.data
                }
            }
        }
    }

    private val _nfcScanInfo = MutableStateFlow<NfcScanInfo?>(null)
    private val _event = MutableStateFlow<NfcState?>(null)

    var inputCvc: String? = null
        private set

    var masterSigner: MasterSigner? = null
        private set

    val nfcScanInfo = _nfcScanInfo.filterIsInstance<NfcScanInfo>()
    val event = _event.filterIsInstance<NfcState>()

    fun updateInputCvc(cvc: String) {
        inputCvc = cvc
    }

    fun updateMasterSigner(masterSigner: MasterSigner) {
        this.masterSigner = masterSigner
        savedStateHandle[EXTRA_MASTER_SIGNER_ID] = masterSigner.id
    }

    fun updateNfcScanInfo(intent: Intent) {
        val tag: Tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG) as? Tag ?: return
        val requestCode = intent.getIntExtra(BaseNfcActivity.EXTRA_REQUEST_NFC_CODE, 0)
        Timber.d("requestCode: $requestCode")
        if (requestCode == 0) return
        val records = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            .orEmpty()
            .filterIsInstance<NdefMessage>().map { rawMessage ->
                rawMessage.records.toList()
            }.flatten()
        _nfcScanInfo.value = NfcScanInfo(requestCode, tag, records)
    }

    fun clearScanInfo() {
        _nfcScanInfo.value = null
    }

    fun clearEvent() {
        _event.value = null
    }

    fun handleNfcError(e: Throwable?): Boolean {
        if (e is NCNativeException) {
            if (e.message.contains(TapProtocolException.BAD_AUTH.toString())) {
                _event.value = NfcState.WrongCvc
                return true
            } else if (e.message.contains(TapProtocolException.RATE_LIMIT.toString())) {
                _event.value = NfcState.LimitCvcInput
                return true
            }
        }
        return false
    }

    override fun onCleared() {
        super.onCleared()
        closeNfc()
    }

    private fun closeNfc() {
        runCatching {
            _nfcScanInfo.value?.let {
                IsoDep.get(it.tag)?.close()
            }
        }
    }

    companion object {
        const val EXTRA_MASTER_SIGNER_ID = "EXTRA_MASTER_SIGNER_ID"
    }
}

sealed class NfcState {
    object WrongCvc : NfcState()
    object LimitCvcInput : NfcState()
}