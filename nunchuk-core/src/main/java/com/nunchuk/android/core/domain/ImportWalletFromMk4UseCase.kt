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

import android.nfc.NdefRecord
import com.nunchuk.android.domain.di.IoDispatcher
import com.nunchuk.android.model.Wallet
import com.nunchuk.android.nativelib.NunchukNativeSdk
import com.nunchuk.android.usecase.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ImportWalletFromMk4UseCase @Inject constructor(
    @IoDispatcher dispatcher: CoroutineDispatcher,
    private val getAppSettingUseCase: GetAppSettingUseCase,
    private val nunchukNativeSdk: NunchukNativeSdk
) : UseCase<List<NdefRecord>, Wallet?>(dispatcher) {

    override suspend fun execute(parameters: List<NdefRecord>): Wallet? {
        val appSettings = getAppSettingUseCase.execute().first()
        return nunchukNativeSdk.importWalletFromMk4(appSettings.chain.ordinal, parameters.toTypedArray())
    }
}
