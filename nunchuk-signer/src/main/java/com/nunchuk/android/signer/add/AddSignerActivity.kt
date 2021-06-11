package com.nunchuk.android.signer.add

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.nunchuk.android.arch.vm.NunchukFactory
import com.nunchuk.android.core.base.BaseActivity
import com.nunchuk.android.qr.QRCodeParser
import com.nunchuk.android.qr.startQRCodeScan
import com.nunchuk.android.signer.R
import com.nunchuk.android.signer.add.AddSignerEvent.*
import com.nunchuk.android.signer.databinding.ActivityAddSignerBinding
import com.nunchuk.android.widget.NCToastMessage
import com.nunchuk.android.widget.util.addTextChangedCallback
import com.nunchuk.android.widget.util.heightExtended
import com.nunchuk.android.widget.util.setLightStatusBar
import com.nunchuk.android.widget.util.setMaxLength
import javax.inject.Inject

class AddSignerActivity : BaseActivity() {

    @Inject
    lateinit var factory: NunchukFactory

    private val viewModel: AddSignerViewModel by lazy {
        ViewModelProviders.of(this, factory).get(AddSignerViewModel::class.java)
    }

    private lateinit var binding: ActivityAddSignerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setLightStatusBar()

        binding = ActivityAddSignerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()

        observeEvent()
    }

    private fun observeEvent() {
        viewModel.event.observe(this) {
            when (it) {
                is AddSignerSuccessEvent -> openSignerInfo(it.id, it.name)
                InvalidSignerSpecEvent -> binding.signerSpec.setError(getString(R.string.nc_error_invalid_signer_spec))
                is AddSignerErrorEvent -> NCToastMessage(this).showWarning(it.message)
                SignerNameRequiredEvent -> binding.signerName.setError(getString(R.string.nc_text_required))
            }
        }
    }

    private fun openSignerInfo(id: String, name: String) {
        finish()
        navigator.openSignerInfoScreen(this, id = id, name = name, justAdded = true)
    }

    private fun setupViews() {
        binding.signerName.setMaxLength(MAX_LENGTH)
        updateCounter(0)
        binding.signerName.addTextChangedCallback {
            updateCounter(it.length)
        }

        binding.addSignerViaQR.setOnClickListener { startQRCodeScan() }
        binding.signerSpec.heightExtended(resources.getDimensionPixelSize(R.dimen.nc_height_180))
        binding.addSigner.setOnClickListener {
            viewModel.handleAddSigner(binding.signerName.getEditText(), binding.signerSpec.getEditText())
        }
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        QRCodeParser.parse(requestCode, resultCode, data)?.apply {
            binding.signerSpec.getEditTextView().setText(this)
            viewModel.handleAddCoboSigner(binding.signerName.getEditText(), this)
        }
    }

    private fun updateCounter(length: Int) {
        val counterValue = "$length/$MAX_LENGTH"
        binding.signerNameCounter.text = counterValue
    }

    companion object {
        private const val MAX_LENGTH = 20
        fun start(activityContext: Context) {
            activityContext.startActivity(Intent(activityContext, AddSignerActivity::class.java))
        }
    }

}