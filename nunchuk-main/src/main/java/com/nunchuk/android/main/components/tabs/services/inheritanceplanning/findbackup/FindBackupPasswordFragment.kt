/**************************************************************************
 * This file is part of the Nunchuk software (https://nunchuk.io/)        *
 * Copyright (C) 2022, 2023 Nunchuk                                       *
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

package com.nunchuk.android.main.components.tabs.services.inheritanceplanning.findbackup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.nunchuk.android.compose.NcHighlightText
import com.nunchuk.android.compose.NcImageAppBar
import com.nunchuk.android.compose.NcPrimaryDarkButton
import com.nunchuk.android.compose.NunchukTheme
import com.nunchuk.android.main.R
import com.nunchuk.android.main.components.tabs.services.inheritanceplanning.InheritanceKeyType
import com.nunchuk.android.main.components.tabs.services.inheritanceplanning.InheritancePlanningViewModel
import com.nunchuk.android.model.byzantine.GroupWalletType
import com.nunchuk.android.share.membership.MembershipFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FindBackupPasswordFragment : MembershipFragment() {
    private val viewModel: FindBackupPasswordViewModel by viewModels()
    private val inheritanceViewModel: InheritancePlanningViewModel by activityViewModels()
    private val args by navArgs<FindBackupPasswordFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                val remainTime by viewModel.remainTime.collectAsStateWithLifecycle()
                val uiState by inheritanceViewModel.state.collectAsStateWithLifecycle()
                FindBackupPasswordContent(remainTime = remainTime,
                    inheritanceKeyType = uiState.keyTypes[args.stepNumber - 1],
                    numOfKeys = uiState.keyTypes.size,
                    stepNumber = args.stepNumber,
                    groupWalletType = inheritanceViewModel.getGroupWalletType()) {
                    if (uiState.keyTypes.size == 2 && args.stepNumber == 1) {
                        findNavController().navigate(FindBackupPasswordFragmentDirections.actionFindBackupPasswordFragmentSelf(2))
                    } else {
                        if (inheritanceViewModel.getGroupWalletType() == GroupWalletType.THREE_OF_FIVE_INHERITANCE) {
                            findNavController().navigate(FindBackupPasswordFragmentDirections.actionFindBackupPasswordFragmentToInheritanceActivationDateFragment())
                        } else {
                            findNavController().navigate(
                                FindBackupPasswordFragmentDirections.actionFindBackupPasswordFragmentToInheritanceKeyTipFragment()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FindBackupPasswordContent(
    remainTime: Int = 0,
    groupWalletType: GroupWalletType? = null,
    inheritanceKeyType: InheritanceKeyType = InheritanceKeyType.TAPSIGNER,
    stepNumber: Int = 1,
    numOfKeys: Int = 1,
    onContinueClicked: () -> Unit = {},
) {
    val isTwoOfFiveInheritance = groupWalletType == GroupWalletType.THREE_OF_FIVE_INHERITANCE
    val desc = when {
        numOfKeys == 1 && inheritanceKeyType == InheritanceKeyType.TAPSIGNER -> stringResource(id = R.string.nc_find_backup_password_desc)
        numOfKeys == 1 -> stringResource(id = R.string.nc_record_your_backup_password_desc)
        inheritanceKeyType == InheritanceKeyType.TAPSIGNER && stepNumber == 1 -> stringResource(id = R.string.nc_find_backup_password_desc_1)
        inheritanceKeyType == InheritanceKeyType.TAPSIGNER -> stringResource(id = R.string.nc_find_backup_password_desc_2)
        stepNumber == 1 -> stringResource(id = R.string.nc_record_your_backup_password_desc_1)
        else -> stringResource(id = R.string.nc_find_backup_password_desc_2)
    }
    NunchukTheme {
        Scaffold { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
            ) {
                NcImageAppBar(
                    backgroundRes = if (inheritanceKeyType == InheritanceKeyType.TAPSIGNER) R.drawable.nc_bg_tap_signer_explain else R.drawable.bg_backup_coldcard_illustration,
                    title = stringResource(
                        id = R.string.nc_estimate_remain_time,
                        remainTime
                    ),
                )
                if (numOfKeys > 1) {
                    Text(
                        modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
                        text = "Inheritance Key $stepNumber/$numOfKeys",
                        style = NunchukTheme.typography.title.copy(color = colorResource(id = R.color.nc_beeswax_dark))
                    )
                }
                Text(
                    modifier = Modifier.padding(top = if (numOfKeys > 1) 4.dp else 16.dp, start = 16.dp, end = 16.dp),
                    text = if (inheritanceKeyType == InheritanceKeyType.TAPSIGNER) stringResource(id = R.string.nc_find_backup_passwords) else stringResource(R.string.nc_record_your_backup_password),
                    style = NunchukTheme.typography.heading
                )
                NcHighlightText(
                    modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
                    text = desc,
                    style = NunchukTheme.typography.body
                )
                Spacer(modifier = Modifier.weight(1.0f))
                NcPrimaryDarkButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    onClick = onContinueClicked,
                ) {
                    Text(text = stringResource(id = R.string.nc_text_continue))
                }
            }
        }
    }
}

@Preview
@Composable
private fun FindBackupPasswordScreenPreview() {
    FindBackupPasswordContent(

    )
}