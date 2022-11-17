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

import com.nunchuk.android.core.constants.Constants.GLOBAL_SIGNET_EXPLORER
import com.nunchuk.android.core.constants.Constants.MAIN_NET_HOST
import com.nunchuk.android.core.constants.Constants.SIG_NET_HOST
import com.nunchuk.android.core.constants.Constants.TEST_NET_HOST
import com.nunchuk.android.model.AppSettings
import com.nunchuk.android.type.BackendType
import com.nunchuk.android.type.Chain
import com.nunchuk.android.usecase.GetOrCreateRootDirUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import javax.inject.Inject

interface InitAppSettingsUseCase {
    fun execute(): Flow<AppSettings>
}

internal class InitAppSettingsUseCaseImpl @Inject constructor(
    private val updateAppSettingUseCase: UpdateAppSettingUseCase,
    private val getOrCreateRootDirUseCase: GetOrCreateRootDirUseCase
) : InitAppSettingsUseCase {

    override fun execute() = getOrCreateRootDirUseCase.execute().flatMapConcat { path ->
        updateAppSettingUseCase.execute(
            AppSettings(
                chain = Chain.MAIN,
                hwiPath = "bin/hwi",
                testnetServers = listOf(TEST_NET_HOST),
                mainnetServers = listOf(MAIN_NET_HOST),
                signetServers = listOf(SIG_NET_HOST),
                backendType = BackendType.ELECTRUM,
                storagePath = path,
                signetExplorerHost = GLOBAL_SIGNET_EXPLORER
            )

        )
    }
}
