package com.nunchuk.android.transaction.components.export

import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.nunchuk.android.core.base.BaseActivity
import com.nunchuk.android.core.share.IntentSharingController
import com.nunchuk.android.share.model.TransactionOption
import com.nunchuk.android.transaction.components.export.ExportTransactionEvent.*
import com.nunchuk.android.transaction.databinding.ActivityExportTransactionBinding
import com.nunchuk.android.widget.NCToastMessage
import com.nunchuk.android.widget.util.setLightStatusBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ExportTransactionActivity : BaseActivity<ActivityExportTransactionBinding>() {

    private val controller: IntentSharingController by lazy { IntentSharingController.from(this) }

    private val args: ExportTransactionArgs by lazy { ExportTransactionArgs.deserializeFrom(intent) }

    private lateinit var bitmaps: List<Bitmap>

    private var index = 0

    private val viewModel: ExportTransactionViewModel by viewModels()

    override fun initializeBinding() = ActivityExportTransactionBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setLightStatusBar()
        setupViews()
        observeEvent()
        viewModel.init(walletId = args.walletId, txId = args.txId, transactionOption = args.transactionOption)
    }

    private fun bindQrCodes() {
        calculateIndex()
        binding.qrCode.setImageBitmap(bitmaps[index])
    }

    private fun calculateIndex() {
        index++
        if (index >= bitmaps.size) {
            index = 0
        }
    }

    private fun observeEvent() {
        viewModel.event.observe(this, ::handleEvent)
        viewModel.state.observe(this, ::handleState)
    }

    private fun setupViews() {
        binding.btnExportAsFile.setOnClickListener {
            viewModel.exportTransactionToFile()
        }
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun handleState(state: ExportTransactionState) {
        if (state.qrCodeBitmap.isNotEmpty()) {
            bitmaps = state.qrCodeBitmap
            lifecycleScope.launch {
                repeat(Int.MAX_VALUE) {
                    bindQrCodes()
                    delay(500L)
                }
            }
        }
    }

    private fun handleEvent(event: ExportTransactionEvent) {
        when (event) {
            is ExportTransactionError -> {
                hideLoading()
                NCToastMessage(this).showError(event.message)
            }
            is ExportToFileSuccess -> {
                hideLoading()
                shareTransactionFile(event.filePath)
            }
            LoadingEvent -> showLoading()
        }
    }

    private fun shareTransactionFile(filePath: String) {
        controller.shareFile(filePath)
    }

    companion object {

        fun start(activityContext: Activity, walletId: String, txId: String, transactionOption: TransactionOption) {
            activityContext.startActivity(
                ExportTransactionArgs(
                    walletId = walletId,
                    txId = txId,
                    transactionOption = transactionOption
                ).buildIntent(activityContext)
            )
        }
    }
}

