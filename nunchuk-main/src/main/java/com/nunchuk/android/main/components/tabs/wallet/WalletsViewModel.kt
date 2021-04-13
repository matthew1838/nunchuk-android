package com.nunchuk.android.main.components.tabs.wallet

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.nunchuk.android.arch.vm.NunchukViewModel
import com.nunchuk.android.main.components.tabs.wallet.WalletsEvent.*
import com.nunchuk.android.model.Result
import com.nunchuk.android.model.Result.Success
import com.nunchuk.android.usecase.GetRemoteSignersUseCase
import com.nunchuk.android.usecase.GetWalletsUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class WalletsViewModel @Inject constructor(
    private val getRemoteSignersUseCase: GetRemoteSignersUseCase,
    private val getWalletsUseCase: GetWalletsUseCase
) : NunchukViewModel<WalletsState, WalletsEvent>() {

    override val initialState = WalletsState()

    fun retrieveData() {
        getSigners()
        getWallets()
    }

    private fun getSigners() {
        viewModelScope.launch {
            when (val result = getRemoteSignersUseCase.execute()) {
                is Success -> {
                    updateState { copy(signers = result.data) }
                }
                is Result.Error -> {
                    updateState { copy(signers = emptyList()) }
                    Log.e(TAG, "get signers error: ${result.exception.message}")
                }
            }
        }
    }

    private fun getWallets() {
        viewModelScope.launch {
            when (val result = getWalletsUseCase.execute()) {
                is Success -> {
                    updateState { copy(wallets = result.data) }
                }
                is Result.Error -> {
                    updateState { copy(wallets = emptyList()) }
                    Log.e(TAG, "get wallets error: ${result.exception.message}")
                }
            }
        }
    }

    fun handleAddSignerOrWallet() {
        if (hasSigner()) {
            handleAddWallet()
        } else {
            handleAddSigner()
        }
    }

    fun handleAddSigner() {
        if (hasSigner()) {
            event(AddSignerEvent)
        } else {
            event(ShowSignerIntroEvent)
        }
    }

    fun handleAddWallet() {
        if (hasSigner()) {
            event(AddWalletEvent)
        } else {
            event(ShowErrorEvent("You need to add signer before creating wallet"))
        }
    }

    private fun hasSigner() = getState().signers.isNotEmpty()

    companion object {
        private const val TAG = "WalletsViewModel"
    }
}