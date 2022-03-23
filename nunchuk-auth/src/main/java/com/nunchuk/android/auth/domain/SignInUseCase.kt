package com.nunchuk.android.auth.domain

import com.nunchuk.android.auth.api.UserTokenResponse
import com.nunchuk.android.auth.data.AuthRepository
import com.nunchuk.android.core.account.AccountManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface SignInUseCase {
    fun execute(
        email: String,
        password: String,
        staySignedIn: Boolean = true
    ): Flow<Pair<String, String>>
}

internal class SignInUseCaseImpl @Inject constructor(
    private val authRepository: AuthRepository,
    private val accountManager: AccountManager
) : SignInUseCase {

    override fun execute(email: String, password: String, staySignedIn: Boolean) = authRepository.login(
        email = email,
        password = password
    ).map {
        storeAccount(email, it, staySignedIn)
    }

    private fun storeAccount(email: String, response: UserTokenResponse, staySignedIn: Boolean): Pair<String, String> {
        val account = accountManager.getAccount()
        accountManager.storeAccount(
            account.copy(
                email = email,
                token = response.tokenId,
                activated = true,
                staySignedIn = staySignedIn,
                deviceId = response.deviceId
            )
        )
        return response.tokenId to response.deviceId
    }

}