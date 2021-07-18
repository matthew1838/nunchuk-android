package com.nunchuk.android.wallet.confirm

import androidx.lifecycle.viewModelScope
import com.nunchuk.android.arch.vm.NunchukViewModel
import com.nunchuk.android.core.util.orUnknownError
import com.nunchuk.android.model.MasterSigner
import com.nunchuk.android.model.Result
import com.nunchuk.android.model.Result.Error
import com.nunchuk.android.model.Result.Success
import com.nunchuk.android.model.SingleSigner
import com.nunchuk.android.type.AddressType
import com.nunchuk.android.type.WalletType
import com.nunchuk.android.type.WalletType.ESCROW
import com.nunchuk.android.usecase.CreateWalletUseCase
import com.nunchuk.android.usecase.DraftWalletUseCase
import com.nunchuk.android.usecase.GetUnusedSignerFromMasterSignerUseCase
import com.nunchuk.android.wallet.confirm.WalletConfirmEvent.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

internal class WalletConfirmViewModel @Inject constructor(
    private val getUnusedSignerFromMasterSignerUseCase: GetUnusedSignerFromMasterSignerUseCase,
    private val draftWalletUseCase: DraftWalletUseCase,
    private val createWalletUseCase: CreateWalletUseCase
) : NunchukViewModel<Unit, WalletConfirmEvent>() {

    override val initialState = Unit
    private var descriptor = ""

    fun handleContinueEvent(
        walletName: String,
        walletType: WalletType,
        addressType: AddressType,
        totalRequireSigns: Int,
        masterSigners: List<MasterSigner>,
        remoteSigners: List<SingleSigner>
    ) {
        event(SetLoadingEvent(true))
        viewModelScope.launch {
            val unusedSignerSigners = ArrayList<SingleSigner>()
            masterSigners.forEach {
                val result: Result<SingleSigner> = getUnusedSignerFromMasterSignerUseCase.execute(it.id, walletType, addressType)
                if (result is Success) {
                    unusedSignerSigners.add(result.data)
                }
            }
            draftWallet(walletName, totalRequireSigns, addressType, walletType, unusedSignerSigners + remoteSigners)
        }
    }

    private suspend fun draftWallet(
        walletName: String,
        totalRequireSigns: Int,
        addressType: AddressType,
        walletType: WalletType,
        signers: List<SingleSigner>
    ) {
        val result = draftWalletUseCase.execute(
            name = walletName,
            totalRequireSigns = totalRequireSigns,
            signers = signers,
            addressType = addressType,
            isEscrow = walletType == ESCROW
        )
        when (result) {
            is Success -> {
                descriptor = result.data
                createWallet(walletName, totalRequireSigns, signers, addressType, walletType)
            }
            is Error -> {
                event(CreateWalletErrorEvent(result.exception.message.orUnknownError()))
                event(SetLoadingEvent(false))
            }
        }
    }

    private suspend fun createWallet(
        walletName: String,
        totalRequireSigns: Int,
        signers: List<SingleSigner>,
        addressType: AddressType,
        walletType: WalletType
    ) {
        val result = createWalletUseCase.execute(
            name = walletName,
            totalRequireSigns = totalRequireSigns,
            signers = signers,
            addressType = addressType,
            isEscrow = walletType == ESCROW
        )
        when (result) {
            is Success -> event(CreateWalletSuccessEvent(result.data.id, descriptor))
            is Error -> {
                event(CreateWalletErrorEvent(result.exception.message.orUnknownError()))
                event(SetLoadingEvent(false))
            }
        }
    }

}

internal fun String.isWalletExisted() = this.toLowerCase(Locale.getDefault()).startsWith(WALLET_EXISTED)

internal const val WALLET_EXISTED = "wallet existed"