package com.nunchuk.android.auth.components.signin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import androidx.lifecycle.ViewModelProviders
import com.nunchuk.android.arch.BaseActivity
import com.nunchuk.android.arch.vm.ViewModelFactory
import com.nunchuk.android.auth.R
import com.nunchuk.android.auth.components.signin.SignInEvent.*
import com.nunchuk.android.auth.databinding.ActivitySigninBinding
import com.nunchuk.android.auth.util.orUnknownError
import com.nunchuk.android.auth.util.setUnderlineText
import com.nunchuk.android.core.util.showToast
import com.nunchuk.android.nav.NunchukNavigator
import com.nunchuk.android.widget.util.SimpleTextWatcher
import com.nunchuk.android.widget.util.setTransparentStatusBar
import javax.inject.Inject

class SignInActivity : BaseActivity() {

    @Inject
    lateinit var factory: ViewModelFactory

    @Inject
    lateinit var navigator: NunchukNavigator

    private val viewModel: SignInViewModel by lazy {
        ViewModelProviders.of(this, factory).get(SignInViewModel::class.java)
    }

    private lateinit var binding: ActivitySigninBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTransparentStatusBar(false)

        binding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()

        observeEvent()
    }

    private fun observeEvent() {
        viewModel.event.observe(this) {
            when (it) {
                is EmailRequiredEvent -> binding.email.setError(getString(R.string.nc_text_required))
                is EmailInvalidEvent -> binding.email.setError(getString(R.string.nc_text_email_invalid))
                is EmailValidEvent -> binding.email.hideError()
                is PasswordRequiredEvent -> binding.password.setError(getString(R.string.nc_text_required))
                is PasswordValidEvent -> binding.password.hideError()
                is SignInErrorEvent -> showToast(it.message.orUnknownError())
                is SignInSuccessEvent -> openMainScreen()
            }
        }
    }

    private fun openMainScreen() {
        finish()
        navigator.openMainScreen(this)
    }

    private fun setupViews() {
        binding.forgotPassword.setUnderlineText(getString(R.string.nc_text_forgot_password))

        binding.password.getEditTextView().transformationMethod = PasswordTransformationMethod.getInstance()

        binding.staySignIn.setOnCheckedChangeListener { _, checked -> viewModel.storeStaySignedIn(checked) }
        binding.signUp.setOnClickListener { onSignUpClick() }
        binding.signIn.setOnClickListener { onSignInClick() }
        binding.forgotPassword.setOnClickListener { onForgotPasswordClick() }
    }

    private fun onSignUpClick() {
        finish()
        navigator.openSignUpScreen(this)
    }

    private fun onForgotPasswordClick() {
        finish()
        navigator.openForgotPasswordScreen(this)
    }

    private fun onSignInClick() {
        viewModel.handleSignIn(
            email = binding.email.getEditText(),
            password = binding.password.getEditText(),
        )
    }

    companion object {
        fun start(activityContext: Context) {
            activityContext.startActivity(Intent(activityContext, SignInActivity::class.java))
        }
    }

}