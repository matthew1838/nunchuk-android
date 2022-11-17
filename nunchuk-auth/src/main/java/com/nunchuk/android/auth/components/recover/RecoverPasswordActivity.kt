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

package com.nunchuk.android.auth.components.recover

import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import com.nunchuk.android.auth.R
import com.nunchuk.android.auth.components.recover.RecoverPasswordEvent.*
import com.nunchuk.android.auth.databinding.ActivityRecoverPasswordBinding
import com.nunchuk.android.core.base.BaseActivity
import com.nunchuk.android.core.util.orUnknownError
import com.nunchuk.android.core.util.showToast
import com.nunchuk.android.widget.util.setTransparentStatusBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecoverPasswordActivity : BaseActivity<ActivityRecoverPasswordBinding>() {

    private val args: RecoverPasswordArgs by lazy { RecoverPasswordArgs.deserializeFrom(intent) }

    private val viewModel: RecoverPasswordViewModel by viewModels()

    override fun initializeBinding() = ActivityRecoverPasswordBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTransparentStatusBar(false)

        viewModel.initData(args.email)

        setupViews()

        observeEvent()
    }

    private fun observeEvent() {
        viewModel.event.observe(this, ::handleEvent)
    }

    private fun handleEvent(event: RecoverPasswordEvent) {
        when (event) {
            is OldPasswordRequiredEvent -> binding.oldPassword.setError(getString(R.string.nc_text_required))
            is OldPasswordValidEvent -> binding.oldPassword.hideError()
            is NewPasswordRequiredEvent -> binding.newPassword.setError(getString(R.string.nc_text_required))
            is NewPasswordValidEvent -> binding.newPassword.hideError()
            is ConfirmPasswordRequiredEvent -> binding.confirmPassword.setError(getString(R.string.nc_text_required))
            is ConfirmPasswordValidEvent -> binding.confirmPassword.hideError()
            is ConfirmPasswordNotMatchedEvent -> binding.confirmPassword.setError(getString(R.string.nc_text_password_does_not_match))
            is RecoverPasswordErrorEvent -> showChangePasswordError(event.errorMessage.orUnknownError())
            is RecoverPasswordSuccessEvent -> openLoginScreen()
            is LoadingEvent -> showLoading()
        }
    }

    private fun setupViews() {
        binding.oldPassword.makeMaskedInput()
        binding.newPassword.makeMaskedInput()
        binding.confirmPassword.makeMaskedInput()
        binding.recoverPassword.setOnClickListener { onChangePasswordClicked() }
        binding.signIn.setOnClickListener { openLoginScreen() }
    }

    private fun openLoginScreen() {
        hideLoading()
        finish()
        navigator.openSignInScreen(this)
    }

    private fun showChangePasswordError(errorMessage: String) {
        hideLoading()
        showToast(errorMessage)
    }

    private fun onChangePasswordClicked() {
        viewModel.handleChangePassword(
            oldPassword = binding.oldPassword.getEditText(),
            newPassword = binding.newPassword.getEditText(),
            confirmPassword = binding.confirmPassword.getEditText()
        )
    }

    companion object {
        fun start(activityContext: Context, email: String) {
            activityContext.startActivity(RecoverPasswordArgs(email).buildIntent(activityContext))
        }
    }

}

