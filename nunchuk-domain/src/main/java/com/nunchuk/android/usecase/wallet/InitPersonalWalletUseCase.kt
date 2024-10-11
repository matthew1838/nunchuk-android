package com.nunchuk.android.usecase.wallet

import com.nunchuk.android.domain.di.IoDispatcher
import com.nunchuk.android.model.WalletConfig
import com.nunchuk.android.repository.PremiumWalletRepository
import com.nunchuk.android.usecase.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class InitPersonalWalletUseCase @Inject constructor(
    private val repository: PremiumWalletRepository,
    @IoDispatcher dispatcher: CoroutineDispatcher
) : UseCase<InitPersonalWalletUseCase.Param, Unit>(dispatcher) {

    override suspend fun execute(parameters: Param) {
        repository.initPersonalWallet(parameters.walletConfig)
    }

    data class Param(val walletConfig: WalletConfig)
}