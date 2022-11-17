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

package com.nunchuk.android.core.domain

import com.nunchuk.android.nativelib.NunchukNativeSdk
import com.nunchuk.android.utils.CrashlyticsReporter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

interface CleanUpCryptoAssetsUseCase {
    fun execute(): Flow<Unit>
}

internal class CleanUpCryptoAssetsUseCaseImpl @Inject constructor(
    private val nativeSdk: NunchukNativeSdk
) : CleanUpCryptoAssetsUseCase {

    override fun execute() = flow {
        try {
            nativeSdk.getWallets().forEach { nativeSdk.deleteWallet(it.id) }
            nativeSdk.getMasterSigners().forEach { nativeSdk.deleteMasterSigner(it.id) }
            nativeSdk.getRemoteSigners().forEach { nativeSdk.deleteRemoteSigner(it.masterSignerId, it.masterFingerprint) }
        } catch (t: Throwable) {
            CrashlyticsReporter.recordException(t)
        }
        emit(Unit)
    }

}
