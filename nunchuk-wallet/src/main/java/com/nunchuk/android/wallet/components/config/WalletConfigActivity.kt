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

package com.nunchuk.android.wallet.components.config

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.nunchuk.android.core.manager.ActivityManager
import com.nunchuk.android.core.share.IntentSharingController
import com.nunchuk.android.core.sheet.BottomSheetOption
import com.nunchuk.android.core.sheet.SheetOption
import com.nunchuk.android.core.sheet.SheetOptionType
import com.nunchuk.android.model.WalletExtended
import com.nunchuk.android.share.wallet.bindWalletConfiguration
import com.nunchuk.android.type.WalletType
import com.nunchuk.android.wallet.R
import com.nunchuk.android.wallet.components.base.BaseWalletConfigActivity
import com.nunchuk.android.wallet.components.config.WalletConfigEvent.UpdateNameErrorEvent
import com.nunchuk.android.wallet.components.config.WalletConfigEvent.UpdateNameSuccessEvent
import com.nunchuk.android.wallet.components.upload.UploadConfigurationEvent
import com.nunchuk.android.wallet.databinding.ActivityWalletConfigBinding
import com.nunchuk.android.wallet.util.toReadableString
import com.nunchuk.android.widget.NCDeleteConfirmationDialog
import com.nunchuk.android.widget.NCToastMessage
import com.nunchuk.android.widget.NCWarningDialog
import com.nunchuk.android.widget.util.setLightStatusBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WalletConfigActivity : BaseWalletConfigActivity<ActivityWalletConfigBinding>() {

    private val viewModel: WalletConfigViewModel by viewModels()

    private val controller: IntentSharingController by lazy(LazyThreadSafetyMode.NONE) {
        IntentSharingController.from(
            this
        )
    }

    private val args: WalletConfigArgs by lazy { WalletConfigArgs.deserializeFrom(intent) }

    override fun initializeBinding() = ActivityWalletConfigBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setLightStatusBar()
        setupViews()
        observeEvent()
        viewModel.init(args.walletId)
        sharedViewModel.init(args.walletId)
    }

    override fun onOptionClicked(option: SheetOption) {
        super.onOptionClicked(option)
        when (option.type) {
            SheetOptionType.TYPE_EXPORT_AS_QR -> showSubOptionsExportQr()
            SheetOptionType.TYPE_DELETE_WALLET -> handleDeleteWallet()
            SheetOptionType.TYPE_EXPORT_TO_COLD_CARD -> showExportColdcardOptions()
        }
    }

    private fun handleDeleteWallet() {
        if (viewModel.isSharedWallet()) {
            NCWarningDialog(this).showDialog(
                message = getString(R.string.nc_delete_collaborative_wallet),
                onYesClick = { viewModel.handleDeleteWallet() }
            )
        } else {
            NCDeleteConfirmationDialog(this).showDialog(
                message = getString(R.string.nc_are_you_sure_to_delete_wallet),
                onConfirmed = {
                    if (it.trim() == CONFIRMATION_TEXT) {
                        viewModel.handleDeleteWallet()
                    } else {
                        NCToastMessage(this).showWarning(getString(R.string.nc_incorrect))
                    }
                }
            )
        }
    }

    private fun observeEvent() {
        viewModel.state.observe(this, ::handleState)
        viewModel.event.observe(this, ::handleEvent)
    }

    override fun handleSharedEvent(event: UploadConfigurationEvent) {
        super.handleSharedEvent(event)
        if (event is UploadConfigurationEvent.ExportColdcardSuccess
            && event.filePath.isNullOrEmpty().not()
        ) {
            shareConfigurationFile(event.filePath.orEmpty())
        }
    }

    private fun handleEvent(event: WalletConfigEvent) {
        when (event) {
            UpdateNameSuccessEvent -> showEditWalletSuccess()
            is UpdateNameErrorEvent -> NCToastMessage(this).showWarning(event.message)
            WalletConfigEvent.DeleteWalletSuccess -> walletDeleted()
            is WalletConfigEvent.WalletDetailsError -> onGetWalletError(event)
        }
    }

    private fun onGetWalletError(event: WalletConfigEvent.WalletDetailsError) {
        NCToastMessage(this).showError(event.message)
    }

    private fun shareConfigurationFile(filePath: String) {
        controller.shareFile(filePath)
    }

    private fun walletDeleted() {
        NCToastMessage(this).showMessage(getString(R.string.nc_wallet_delete_wallet_success))
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(EXTRA_WALLET_ACTION, WalletConfigAction.DELETE)
        })
        ActivityManager.popUntilRoot()
    }

    private fun handleState(walletExtended: WalletExtended) {
        val wallet = walletExtended.wallet
        binding.walletName.text = wallet.name

        binding.configuration.bindWalletConfiguration(wallet)

        binding.walletType.text =
            (if (wallet.escrow) WalletType.ESCROW else WalletType.MULTI_SIG).toReadableString(this)
        binding.addressType.text = wallet.addressType.toReadableString(this)
        binding.shareIcon.isVisible = walletExtended.isShared
        SignersViewBinder(binding.signersContainer, viewModel.mapSigners(wallet.signers)).bindItems()
    }

    private fun setupViews() {
        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.menu_more) {
                showMoreOptions()
            }
            false
        }
        binding.walletName.setOnClickListener { onEditClicked() }
        binding.btnDone.setOnClickListener {
            finish()
        }
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun showMoreOptions() {
        val options = listOf(
            SheetOption(
                SheetOptionType.TYPE_EXPORT_AS_QR,
                R.drawable.ic_qr,
                R.string.nc_show_as_qr_code
            ),
            SheetOption(
                SheetOptionType.TYPE_EXPORT_TO_COLD_CARD,
                R.drawable.ic_export,
                R.string.nc_wallet_export_coldcard
            ),
            SheetOption(
                SheetOptionType.TYPE_DELETE_WALLET,
                R.drawable.ic_delete_red,
                R.string.nc_wallet_delete_wallet,
                isDeleted = true
            ),
        )
        val bottomSheet = BottomSheetOption.newInstance(options)
        bottomSheet.show(supportFragmentManager, "BottomSheetOption")
    }

    private fun onEditClicked() {
        val bottomSheet = WalletUpdateBottomSheet.show(
            fragmentManager = supportFragmentManager,
            walletName = binding.walletName.text.toString()
        )

        bottomSheet.setListener(viewModel::handleEditCompleteEvent)
    }

    private fun showEditWalletSuccess() {
        binding.root.post {
            NCToastMessage(this).show(R.string.nc_text_change_wallet_success)
            setResult(Activity.RESULT_OK, Intent().apply {
                putExtra(EXTRA_WALLET_ACTION, WalletConfigAction.UPDATE_NAME)
            })
        }
    }

    companion object {
        private const val CONFIRMATION_TEXT = "DELETE"
        const val EXTRA_WALLET_ACTION = "action"

        fun start(activityContext: Context, walletId: String) {
            activityContext.startActivity(
                WalletConfigArgs(walletId = walletId).buildIntent(
                    activityContext
                )
            )
        }

        fun start(
            launcher: ActivityResultLauncher<Intent>,
            activityContext: Context,
            walletId: String
        ) {
            launcher.launch(WalletConfigArgs(walletId = walletId).buildIntent(activityContext))
        }
    }

}

enum class WalletConfigAction {
    DELETE, UPDATE_NAME
}