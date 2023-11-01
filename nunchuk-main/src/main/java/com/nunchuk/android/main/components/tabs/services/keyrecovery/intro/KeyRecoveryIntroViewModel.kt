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

package com.nunchuk.android.main.components.tabs.services.keyrecovery.intro

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nunchuk.android.core.domain.membership.CalculateRequiredSignaturesKeyRecoveryUseCase
import com.nunchuk.android.core.domain.membership.DownloadBackupKeyUseCase
import com.nunchuk.android.core.domain.membership.RecoverKeyUseCase
import com.nunchuk.android.core.domain.membership.RequestRecoverUseCase
import com.nunchuk.android.core.mapper.MasterSignerMapper
import com.nunchuk.android.core.network.NunchukApiException
import com.nunchuk.android.core.signer.SignerModel
import com.nunchuk.android.core.util.CardIdManager
import com.nunchuk.android.core.util.orUnknownError
import com.nunchuk.android.main.components.tabs.services.keyrecovery.securityquestionanswer.AnswerSecurityQuestionEvent
import com.nunchuk.android.usecase.GetGroupsUseCase
import com.nunchuk.android.usecase.GetMasterSignersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KeyRecoveryIntroViewModel @Inject constructor(
    private val getMasterSignersUseCase: GetMasterSignersUseCase,
    private val masterSignerMapper: MasterSignerMapper,
    private val cardIdManager: CardIdManager,
    private val downloadBackupKeyUseCase: DownloadBackupKeyUseCase,
    private val calculateRequiredSignaturesKeyRecoveryUseCase: CalculateRequiredSignaturesKeyRecoveryUseCase,
    private val getGroupsUseCase: GetGroupsUseCase,
    private val requestRecoverUseCase: RequestRecoverUseCase,
    private val recoverKeyUseCase: RecoverKeyUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val args =
        KeyRecoveryIntroFragmentArgs.fromSavedStateHandle(savedStateHandle)

    private val _state = MutableStateFlow(KeyRecoveryIntroState())

    private val _event = MutableSharedFlow<KeyRecoveryIntroEvent>()
    val event = _event.asSharedFlow()

    var isHasGroup = false

    init {
        viewModelScope.launch {
            getGroupsUseCase(Unit).distinctUntilChanged().collect {
                isHasGroup = it.getOrNull().isNullOrEmpty().not()
            }
        }
    }

    fun getTapSignerList() = viewModelScope.launch {
        _event.emit(KeyRecoveryIntroEvent.Loading(true))
        getMasterSignersUseCase.execute().collect { masterSigners ->
            _event.emit(KeyRecoveryIntroEvent.Loading(false))
            val signers = masterSigners
                .filter { it.device.isTapsigner }
                .map { signer ->
                    masterSignerMapper(signer)
                }.map {
                    it.copy(cardId = cardIdManager.getCardId(signerId = it.id))
                }.toList()
            _event.emit(KeyRecoveryIntroEvent.GetTapSignerSuccess(signers))
            _state.update {
                it.copy(
                    tapSigners = signers,
                )
            }
        }
    }

    fun setSelectedSigner(signer: SignerModel) = viewModelScope.launch {
        _state.update {
            it.copy(selectedSigner = signer)
        }
    }

    fun downloadBackupKey(questionId: String, answer: String) = viewModelScope.launch {
        val state = _state.value
        if (state.selectedSigner == null) {
            return@launch
        }
        _event.emit(KeyRecoveryIntroEvent.Loading(true))
        val result = downloadBackupKeyUseCase(
            DownloadBackupKeyUseCase.Param(
                id = state.selectedSigner.fingerPrint,
                questionId = questionId,
                answer = answer,
                verifyToken = args.verifyToken
            )
        )
        _event.emit(KeyRecoveryIntroEvent.Loading(false))
        if (result.isSuccess) {
            _event.emit(
                KeyRecoveryIntroEvent.DownloadBackupKeySuccess(result.getOrThrow())
            )
        } else {
            _event.emit(KeyRecoveryIntroEvent.Error(result.exceptionOrNull()?.message.orUnknownError()))
        }
    }

    fun calculateRequiredSignatures() = viewModelScope.launch {
        val state = _state.value
        if (state.selectedSigner == null) {
            return@launch
        }
        _event.emit(KeyRecoveryIntroEvent.Loading(true))
        val result = calculateRequiredSignaturesKeyRecoveryUseCase(
            CalculateRequiredSignaturesKeyRecoveryUseCase.Param(
                xfp = state.selectedSigner.fingerPrint
            )
        )
        _event.emit(KeyRecoveryIntroEvent.Loading(false))
        if (result.isSuccess) {
            _event.emit(
                KeyRecoveryIntroEvent.CalculateRequiredSignaturesSuccess(result.getOrThrow())
            )
        } else {
            _event.emit(KeyRecoveryIntroEvent.Error(result.exceptionOrNull()?.message.orUnknownError()))
        }
    }

    fun requestRecover(
        signatures: HashMap<String, String>,
        securityQuestionToken: String, confirmCodeToken: String, confirmCodeNonce: String) {
        viewModelScope.launch {
            val state = _state.value
            if (state.selectedSigner == null) {
                return@launch
            }
            _event.emit(KeyRecoveryIntroEvent.Loading(true))
            val result = requestRecoverUseCase(
                RequestRecoverUseCase.Param(
                    signatures = signatures,
                    verifyToken = args.verifyToken,
                    securityQuestionToken = securityQuestionToken,
                    confirmCodeToken = confirmCodeToken,
                    confirmCodeNonce = confirmCodeNonce,
                    xfp = state.selectedSigner.fingerPrint
                )
            )
            _event.emit(KeyRecoveryIntroEvent.Loading(false))
            if (result.isSuccess) {
                _event.emit(KeyRecoveryIntroEvent.RequestRecoverSuccess)
            } else {
                _event.emit(KeyRecoveryIntroEvent.Error(result.exceptionOrNull()?.message.orUnknownError()))
            }
        }
    }

    fun recoverKey() {
        viewModelScope.launch {
            val state = _state.value
            if (state.selectedSigner == null) {
                return@launch
            }
            _event.emit(KeyRecoveryIntroEvent.Loading(true))
            val result = recoverKeyUseCase(
                RecoverKeyUseCase.Param(
                    xfp = state.selectedSigner.fingerPrint
                )
            )
            _event.emit(KeyRecoveryIntroEvent.Loading(false))
            if (result.isSuccess) {
                _event.emit(KeyRecoveryIntroEvent.DownloadBackupKeySuccess(result.getOrThrow()))
            } else {
                _event.emit(KeyRecoveryIntroEvent.Error(result.exceptionOrNull()?.message.orUnknownError()))
            }
        }
    }
}