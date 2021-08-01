package com.nunchuk.android.signer.components.ss.passphrase

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import com.nunchuk.android.arch.vm.NunchukFactory
import com.nunchuk.android.core.base.BaseActivity
import com.nunchuk.android.signer.R
import com.nunchuk.android.signer.components.ss.passphrase.SetPassphraseEvent.*
import com.nunchuk.android.signer.databinding.ActivitySetPassphraseBinding
import com.nunchuk.android.widget.NCToastMessage
import com.nunchuk.android.widget.util.addTextChangedCallback
import com.nunchuk.android.widget.util.passwordEnabled
import com.nunchuk.android.widget.util.setLightStatusBar
import javax.inject.Inject

class SetPassphraseActivity : BaseActivity<ActivitySetPassphraseBinding>() {

    @Inject
    lateinit var factory: NunchukFactory

    private val viewModel: SetPassphraseViewModel by viewModels { factory }

    private val args: SetPassphraseActivityArgs by lazy { SetPassphraseActivityArgs.deserializeFrom(intent) }

    override fun initializeBinding() = ActivitySetPassphraseBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setLightStatusBar()
        viewModel.init(args.mnemonic, args.signerName)
        setupViews()
        observeEvent()
    }

    private fun observeEvent() {
        viewModel.event.observe(this, ::handleEvent)
        viewModel.state.observe(this, ::handleState)
    }

    private fun handleState(state: SetPassphraseState) {
        Log.d(TAG, "handleState($state)")
    }

    private fun handleEvent(event: SetPassphraseEvent) {
        when (event) {
            PassPhraseRequiredEvent -> binding.passphrase.setError(getString(R.string.nc_text_required))
            ConfirmPassPhraseRequiredEvent -> binding.confirmPassphrase.setError(getString(R.string.nc_text_required))
            ConfirmPassPhraseNotMatchedEvent -> binding.confirmPassphrase.setError(getString(R.string.nc_text_confirm_passphrase_not_matched))
            is CreateSoftwareSignerCompletedEvent -> openSignerInfoScreen(event.id, event.name, event.skipPassphrase)
            is CreateSoftwareSignerErrorEvent -> NCToastMessage(this).showError(event.message)
            PassPhraseValidEvent -> removeValidationError()
        }
    }

    private fun removeValidationError() {
        binding.passphrase.hideError()
        binding.confirmPassphrase.hideError()
    }

    private fun openSignerInfoScreen(id: String, name: String, skipPassphrase: Boolean) {
        navigator.openSignerInfoScreen(
            activityContext = this,
            id = id,
            name = name,
            justAdded = true,
            software = true,
            setPassphrase = !skipPassphrase
        )
    }

    private fun setupViews() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.passphrase.passwordEnabled()
        binding.passphrase.addTextChangedCallback(viewModel::updatePassphrase)

        binding.confirmPassphrase.passwordEnabled()
        binding.confirmPassphrase.addTextChangedCallback(viewModel::updateConfirmPassphrase)
        binding.btnNoPassphrase.setOnClickListener { viewModel.skipPassphraseEvent() }
        binding.btnSetPassphrase.setOnClickListener { viewModel.confirmPassphraseEvent() }
    }

    companion object {
        private const val TAG = "SetPassphraseActivity"

        fun start(activityContext: Context, mnemonic: String, signerName: String) {
            activityContext.startActivity(
                SetPassphraseActivityArgs(
                    mnemonic = mnemonic,
                    signerName = signerName
                ).buildIntent(activityContext)
            )
        }
    }

}