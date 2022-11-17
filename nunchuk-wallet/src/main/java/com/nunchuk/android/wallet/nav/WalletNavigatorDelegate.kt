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

package com.nunchuk.android.wallet.nav

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.nunchuk.android.core.qr.DynamicQRCodeActivity
import com.nunchuk.android.model.RecoverWalletData
import com.nunchuk.android.model.RoomWalletData
import com.nunchuk.android.model.SingleSigner
import com.nunchuk.android.nav.WalletNavigator
import com.nunchuk.android.type.AddressType
import com.nunchuk.android.type.WalletType
import com.nunchuk.android.wallet.components.backup.BackupWalletActivity
import com.nunchuk.android.wallet.components.config.WalletConfigActivity
import com.nunchuk.android.wallet.components.configure.ConfigureWalletActivity
import com.nunchuk.android.wallet.components.details.WalletDetailsActivity
import com.nunchuk.android.wallet.components.intro.WalletEmptySignerActivity
import com.nunchuk.android.wallet.components.review.ReviewWalletActivity
import com.nunchuk.android.wallet.components.upload.UploadConfigurationActivity
import com.nunchuk.android.wallet.personal.components.TaprootWarningActivity
import com.nunchuk.android.wallet.personal.components.WalletIntermediaryActivity
import com.nunchuk.android.wallet.personal.components.add.AddWalletActivity
import com.nunchuk.android.wallet.personal.components.recover.AddRecoverWalletActivity
import com.nunchuk.android.wallet.personal.components.recover.RecoverWalletQrCodeActivity
import com.nunchuk.android.wallet.shared.components.assign.AssignSignerSharedWalletActivity
import com.nunchuk.android.wallet.shared.components.config.SharedWalletConfigActivity
import com.nunchuk.android.wallet.shared.components.configure.ConfigureSharedWalletActivity
import com.nunchuk.android.wallet.shared.components.create.CreateSharedWalletActivity
import com.nunchuk.android.wallet.shared.components.recover.AddRecoverSharedWalletActivity
import com.nunchuk.android.wallet.shared.components.recover.RecoverSharedWalletActivity
import com.nunchuk.android.wallet.shared.components.review.ReviewSharedWalletActivity

interface WalletNavigatorDelegate : WalletNavigator {

    override fun openAddWalletScreen(activityContext: Context) {
        AddWalletActivity.start(activityContext)
    }

    override fun openWalletIntermediaryScreen(activityContext: Context, hasSigner: Boolean) {
        WalletIntermediaryActivity.start(activityContext, hasSigner)
    }

    override fun openAddRecoverWalletScreen(activityContext: Context, data: RecoverWalletData) {
        AddRecoverWalletActivity.start(activityContext, data)
    }

    override fun openRecoverWalletQRCodeScreen(activityContext: Context) {
        RecoverWalletQrCodeActivity.start(activityContext)
    }

    override fun openConfigureWalletScreen(
        activityContext: Context,
        walletName: String,
        walletType: WalletType,
        addressType: AddressType
    ) {
        ConfigureWalletActivity.start(activityContext, walletName, walletType, addressType)
    }

    override fun openReviewWalletScreen(
        activityContext: Context,
        walletName: String,
        walletType: WalletType,
        addressType: AddressType,
        totalRequireSigns: Int,
        masterSigners: List<SingleSigner>,
        remoteSigners: List<SingleSigner>
    ) {
        ReviewWalletActivity.start(
            activityContext = activityContext,
            walletName = walletName,
            walletType = walletType,
            addressType = addressType,
            totalRequireSigns = totalRequireSigns,
            masterSigners = masterSigners,
            remoteSigners = remoteSigners
        )
    }

    override fun openBackupWalletScreen(activityContext: Context, walletId: String, numberOfSignKey: Int, isQuickWallet: Boolean) {
        BackupWalletActivity.start(
            activityContext = activityContext,
            walletId = walletId,
            totalRequireSigns = numberOfSignKey,
            isQuickWallet = isQuickWallet
        )
    }

    override fun openUploadConfigurationScreen(activityContext: Context, walletId: String) {
        UploadConfigurationActivity.start(activityContext, walletId)
    }

    override fun openWalletConfigScreen(activityContext: Context, walletId: String) {
        WalletConfigActivity.start(activityContext, walletId)
    }

    override fun openWalletConfigScreen(launcher: ActivityResultLauncher<Intent>, activityContext: Context, walletId: String) {
        WalletConfigActivity.start(launcher, activityContext, walletId)
    }

    override fun openDynamicQRScreen(activityContext: Context, values: List<String>) {
        DynamicQRCodeActivity.start(activityContext, values)
    }

    override fun openWalletDetailsScreen(activityContext: Context, walletId: String) {
        WalletDetailsActivity.start(activityContext, walletId)
    }

    override fun openWalletEmptySignerScreen(activityContext: Context) {
        WalletEmptySignerActivity.start(activityContext)
    }

    override fun openCreateSharedWalletScreen(activityContext: Context) {
        CreateSharedWalletActivity.start(activityContext)
    }

    override fun openConfigureSharedWalletScreen(
        activityContext: Context,
        walletName: String,
        walletType: WalletType,
        addressType: AddressType
    ) {
        ConfigureSharedWalletActivity.start(activityContext, walletName, walletType, addressType)
    }

    override fun openReviewSharedWalletScreen(
        activityContext: Context,
        walletName: String,
        walletType: WalletType,
        addressType: AddressType,
        totalSigns: Int,
        requireSigns: Int,
        signers: List<SingleSigner>
    ) {
        ReviewSharedWalletActivity.start(activityContext, walletName, walletType, addressType, totalSigns, requireSigns, signers)
    }

    override fun openAssignSignerSharedWalletScreen(
        activityContext: Context,
        walletName: String,
        walletType: WalletType,
        addressType: AddressType,
        totalSigns: Int,
        requireSigns: Int,
        signers: List<SingleSigner>
    ) {
        AssignSignerSharedWalletActivity.start(activityContext, walletName, walletType, addressType, totalSigns, requireSigns, signers)
    }

    override fun openSharedWalletConfigScreen(
        activityContext: Context,
        roomWalletData: RoomWalletData
    ) {
        SharedWalletConfigActivity.start(activityContext, roomWalletData)
    }

    override fun openTaprootWarningScreen(
        activityContext: Context,
        walletName: String,
        walletType: WalletType,
        addressType: AddressType
    ) {
        TaprootWarningActivity.start(activityContext, walletName, walletType, addressType)
    }

    override fun openRecoverSharedWalletScreen(activityContext: Context) {
        RecoverSharedWalletActivity.start(activityContext)
    }

    override fun openAddRecoverSharedWalletScreen(activityContext: Context, data: String) {
        AddRecoverSharedWalletActivity.start(activityContext, data)
    }
}