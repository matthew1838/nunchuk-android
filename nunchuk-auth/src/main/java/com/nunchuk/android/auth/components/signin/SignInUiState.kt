package com.nunchuk.android.auth.components.signin

import com.nunchuk.android.core.account.SignInType
import com.nunchuk.android.model.PrimaryKey

data class SignInUiState(
    val email: String = "",
    val type: SignInType = SignInType.EMAIL,
    val isSubscriberUser: Boolean = false,
    val accounts: List<PrimaryKey> = emptyList(),
)