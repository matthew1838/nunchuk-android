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

package com.nunchuk.android.wallet.shared.components.assign

import android.nfc.tech.IsoDep
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.nunchuk.android.GetDefaultSignerFromMasterSignerUseCase
import com.nunchuk.android.arch.vm.NunchukViewModel
import com.nunchuk.android.core.domain.CacheDefaultTapsignerMasterSignerXPubUseCase
import com.nunchuk.android.core.domain.HasSignerUseCase
import com.nunchuk.android.core.domain.SendErrorEventUseCase
import com.nunchuk.android.core.mapper.MasterSignerMapper
import com.nunchuk.android.core.matrix.SessionHolder
import com.nunchuk.android.core.signer.SignerModel
import com.nunchuk.android.core.signer.toModel
import com.nunchuk.android.core.util.orUnknownError
import com.nunchuk.android.core.util.readableMessage
import com.nunchuk.android.model.SingleSigner
import com.nunchuk.android.type.AddressType
import com.nunchuk.android.type.SignerType
import com.nunchuk.android.type.WalletType
import com.nunchuk.android.usecase.GetCompoundSignersUseCase
import com.nunchuk.android.usecase.GetSignerFromMasterSignerUseCase
import com.nunchuk.android.usecase.GetUnusedSignerFromMasterSignerUseCase
import com.nunchuk.android.usecase.JoinWalletUseCase
import com.nunchuk.android.utils.onException
import com.nunchuk.android.wallet.shared.components.assign.AssignSignerEvent.ShowError
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

internal class AssignSignerViewModel @AssistedInject constructor(
    @Assisted private val args: AssignSignerArgs,
    @Assisted private val savedStateHandle: SavedStateHandle,
    private val getCompoundSignersUseCase: GetCompoundSignersUseCase,
    private val getUnusedSignerUseCase: GetUnusedSignerFromMasterSignerUseCase,
    private val joinWalletUseCase: JoinWalletUseCase,
    private val sendErrorEventUseCase: SendErrorEventUseCase,
    private val hasSignerUseCase: HasSignerUseCase,
    private val sessionHolder: SessionHolder,
    private val masterSignerMapper: MasterSignerMapper,
    private val getSignerFromMasterSignerUseCase: GetSignerFromMasterSignerUseCase,
    private val cacheDefaultTapsignerMasterSignerXPubUseCase: CacheDefaultTapsignerMasterSignerXPubUseCase,
    private val getDefaultSignerFromMasterSignerUseCase: GetDefaultSignerFromMasterSignerUseCase
) : NunchukViewModel<AssignSignerState, AssignSignerEvent>() {

    override val initialState = AssignSignerState()

    fun init() {
        updateState { initialState }
    }

    fun filterSigners(signer: SingleSigner) {
        hasSignerUseCase.execute(signer).flowOn(Dispatchers.IO).onException { }.onEach {
            if (it) {
                updateState {
                    val newList = filterRecSigners.toMutableList()
                    newList.add(signer)
                    copy(
                        filterRecSigners = newList
                    )
                }
            }
        }.flowOn(Dispatchers.Main).launchIn(viewModelScope)
    }

    fun cacheTapSignerXpub(isoDep: IsoDep, cvc: String) {
        viewModelScope.launch {
            val signer: SignerModel = savedStateHandle[EXTRA_CURRENT_MASTER_SIGNER] ?: return@launch
            val result = cacheDefaultTapsignerMasterSignerXPubUseCase(
                CacheDefaultTapsignerMasterSignerXPubUseCase.Data(isoDep, cvc, signer.id)
            )
            if (result.isSuccess) {
                val signerResult = getDefaultSignerFromMasterSignerUseCase(
                    GetDefaultSignerFromMasterSignerUseCase.Params(
                        signer.id,
                        WalletType.MULTI_SIG,
                        args.addressType
                    )
                )
                if (signerResult.isSuccess) {
                    val newSigner = signerResult.getOrThrow()
                    handleSelected(newSigner.toModel(), true)
                    updateState {
                        copy(masterSignerMap = masterSignerMap.toMutableMap().apply {
                            set(signer.id, newSigner)
                        })
                    }
                } else {
                    handleSelected(signer, false)
                }
            } else {
                handleSelected(signer, false)
                setEvent(AssignSignerEvent.CacheTapSignerXpubError(result.exceptionOrNull()))
            }
        }
    }

    fun getSigners(walletType: WalletType, addressType: AddressType) {
        getCompoundSignersUseCase.execute().flatMapLatest { signerPair ->
            getUnusedSignerUseCase.execute(
                signerPair.first, walletType, addressType
            ).map { signers ->
                Triple(signerPair.first,
                    signerPair.second,
                    signers.associateBy { it.masterSignerId })
            }
        }.flowOn(Dispatchers.IO)
            .onException {
                updateState {
                    copy(
                        masterSigners = emptyList(), remoteSigners = emptyList()
                    )
                }
            }.onEach {
                updateState {
                    copy(
                        masterSigners = it.first,
                        remoteSigners = it.second,
                        masterSignerMap = it.third
                    )
                }
            }
            .flowOn(Dispatchers.Main)
            .launchIn(viewModelScope)
    }

    fun updateSelectedXfps(model: SignerModel, checked: Boolean) {
        if (model.type == SignerType.NFC && model.derivationPath.isEmpty()) {
            savedStateHandle[EXTRA_CURRENT_MASTER_SIGNER] = model
            setEvent(AssignSignerEvent.RequestCacheTapSignerXpub)
        } else {
            handleSelected(model, checked)
        }
    }

    fun getMasterSignerMap() = getState().masterSignerMap

    fun changeBip32Path(masterSignerId: String, newPath: String) {
        viewModelScope.launch {
            setEvent(AssignSignerEvent.Loading(true))
            val result = getSignerFromMasterSignerUseCase(
                GetSignerFromMasterSignerUseCase.Params(
                    masterSignerId, newPath
                )
            )
            setEvent(AssignSignerEvent.Loading(false))
            if (result.isSuccess) {
                val newMap = getState().masterSignerMap.toMutableMap().apply {
                    set(masterSignerId, result.getOrThrow())
                }
                updateState {
                    copy(masterSignerMap = newMap)
                }
                setEvent(AssignSignerEvent.ChangeBip32Success)
            } else {
                setEvent(ShowError(result.exceptionOrNull()?.message.orUnknownError()))
            }
        }
    }

    fun handleContinueEvent() {
        val state = getState()
        val unusedSignerSigners =
            state.selectedSigner.asSequence().map { state.masterSignerMap[it.id] }.filterNotNull()
                .toList()
        val remoteSigners = if (state.filterRecSigners.isNotEmpty()) state.filterRecSigners.filter {
            state.selectedSigner.contains(it.toModel())
        }
        else state.remoteSigners.filter { state.selectedSigner.contains(it.toModel()) }

        if (unusedSignerSigners.isEmpty() && remoteSigners.isEmpty()) {
            event(ShowError("No keys found"))
            return
        }

        viewModelScope.launch {
            if (sessionHolder.hasActiveRoom()) {
                sessionHolder.getActiveRoomId().let { roomId ->
                    joinWalletUseCase.execute(
                        roomId,
                        if (state.filterRecSigners.isNotEmpty()) remoteSigners else remoteSigners + unusedSignerSigners
                    ).flowOn(Dispatchers.IO)
                        .onStart { setEvent(AssignSignerEvent.Loading(true)) }
                        .onCompletion { setEvent(AssignSignerEvent.Loading(false)) }
                        .onException {
                            event(ShowError(it.readableMessage()))
                            sendErrorEvent(roomId, it, sendErrorEventUseCase::execute)
                        }.onEach { event(AssignSignerEvent.AssignSignerCompletedEvent(roomId)) }
                        .flowOn(Dispatchers.Main).launchIn(viewModelScope)
                }
            }
        }
    }

    fun mapSigners(): List<SignerModel> {
        val state = getState()
        val masterSignerModels = state.masterSigners.map { signer ->
            masterSignerMapper(
                signer,
                state.masterSignerMap[signer.id]?.derivationPath.orEmpty()
            )
        }
        val signers = if (args.signers.isNotEmpty()) {
            masterSignerModels + state.filterRecSigners.map(SingleSigner::toModel)
        } else {
            masterSignerModels + state.remoteSigners.map(SingleSigner::toModel)
        }
        return signers
    }

    private fun handleSelected(model: SignerModel, checked: Boolean) {
        val newSet = getState().selectedSigner.toMutableSet().apply {
            if (checked) add(model) else remove(model)
        }
        updateState {
            copy(
                selectedSigner = newSet
            )
        }
        val state = getState()
        val currentNum = state.totalRequireSigns
        if (currentNum == 0 || state.totalRequireSigns > state.selectedSigner.size) {
            updateState { copy(totalRequireSigns = state.selectedSigner.size) }
        }
    }

    fun toggleShowPath() {
        updateState {
            copy(
                isShowPath = isShowPath.not()
            )
        }
    }

    fun isShowPath() = getState().isShowPath

    @AssistedFactory
    internal interface Factory {
        fun create(args: AssignSignerArgs, savedStateHandle: SavedStateHandle): AssignSignerViewModel
    }

    companion object {
        private const val EXTRA_CURRENT_MASTER_SIGNER = "_a"
    }
}