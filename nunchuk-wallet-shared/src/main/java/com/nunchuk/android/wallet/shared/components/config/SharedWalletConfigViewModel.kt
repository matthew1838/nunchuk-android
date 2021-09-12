package com.nunchuk.android.wallet.shared.components.config

import androidx.lifecycle.viewModelScope
import com.nunchuk.android.arch.vm.NunchukViewModel
import com.nunchuk.android.core.util.orUnknownError
import com.nunchuk.android.model.Wallet
import com.nunchuk.android.usecase.GetWalletUseCase
import com.nunchuk.android.usecase.UpdateWalletUseCase
import com.nunchuk.android.wallet.shared.components.config.SharedWalletConfigEvent.UpdateNameErrorEvent
import com.nunchuk.android.wallet.shared.components.config.SharedWalletConfigEvent.UpdateNameSuccessEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class SharedWalletConfigViewModel @Inject constructor(
    private val getWalletUseCase: GetWalletUseCase,
    private val updateWalletUseCase: UpdateWalletUseCase
) : NunchukViewModel<Wallet, SharedWalletConfigEvent>() {

    override val initialState = Wallet()

    lateinit var walletId: String

    fun init(walletId: String) {
        this.walletId = walletId
        getWalletDetails()
    }

    private fun getWalletDetails() {
        viewModelScope.launch {
            getWalletUseCase.execute(walletId)
                .flowOn(Dispatchers.IO)
                .catch { event(UpdateNameErrorEvent(it.message.orUnknownError())) }
                .flowOn(Dispatchers.Main)
                .collect { updateState { it } }
        }
    }

    fun handleEditCompleteEvent(walletName: String) {
        viewModelScope.launch {
            updateWalletUseCase.execute(getState().copy(name = walletName))
                .flowOn(Dispatchers.IO)
                .catch { event(UpdateNameErrorEvent(it.message.orUnknownError())) }
                .flowOn(Dispatchers.Main)
                .collect {
                    updateState { copy(name = walletName) }
                    event(UpdateNameSuccessEvent)
                }
        }
    }

}