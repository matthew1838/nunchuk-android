package com.nunchuk.android.nav

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher

interface AuthNavigator {
    fun openSignInScreen(activityContext: Context, isNeedNewTask: Boolean = true, isAccountDeleted: Boolean = false)

    fun openSignUpScreen(activityContext: Context)

    fun openIntroScreen(activityContext: Context)

    fun openChangePasswordScreen(activityContext: Context)

    fun openRecoverPasswordScreen(activityContext: Context, email: String)

    fun openForgotPasswordScreen(activityContext: Context)

    fun openVerifyNewDeviceScreen(
        launcher: ActivityResultLauncher<Intent>,
        activityContext: Context, email: String, loginHalfToken: String, deviceId: String, staySignedIn: Boolean
    )
}