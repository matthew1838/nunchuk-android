package com.nunchuk.android.main.membership.byzantine.select

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.nunchuk.android.compose.NcHintMessage
import com.nunchuk.android.compose.NcPrimaryDarkButton
import com.nunchuk.android.compose.NcRadioOption
import com.nunchuk.android.compose.NcTag
import com.nunchuk.android.compose.NcTopAppBar
import com.nunchuk.android.compose.NunchukTheme
import com.nunchuk.android.core.util.ClickAbleText
import com.nunchuk.android.main.R
import com.nunchuk.android.main.membership.byzantine.ByzantineMemberFlow
import com.nunchuk.android.share.membership.MembershipFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectWalletSetupFragment : MembershipFragment() {

    private val args: SelectWalletSetupFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                SelectWalletSetupScreen(
                    onContinueClicked = {
                        findNavController().navigate(
                            SelectWalletSetupFragmentDirections.actionSelectWalletSetupFragmentToByzantineInviteMembersFragment(
                                setupPreference = it,
                                groupType = args.groupType,
                                members = emptyArray(),
                                flow = ByzantineMemberFlow.SETUP
                            )
                        )
                    },
                    onMoreClicked = ::handleShowMore,
                )
            }
        }
    }
}

@Composable
private fun SelectWalletSetupScreen(
    onContinueClicked: (String) -> Unit = {},
    onMoreClicked: () -> Unit = {},
) {
    SelectWalletSetupContent(onContinueClicked = onContinueClicked, onMoreClicked = onMoreClicked)
}

@Composable
private fun SelectWalletSetupContent(
    onContinueClicked: (String) -> Unit = {},
    onMoreClicked: () -> Unit = {},
) {
    var isSinglePersonSetup by remember { mutableStateOf(true) }
    NunchukTheme {
        Scaffold(modifier = Modifier
            .navigationBarsPadding()
            .statusBarsPadding(),
            bottomBar = {
                NcPrimaryDarkButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    onClick = {
                        if (isSinglePersonSetup) {
                            onContinueClicked("SINGLE_PERSON")
                        } else {
                            onContinueClicked("DISTRIBUTED")
                        }
                    }
                ) {
                    Text(text = stringResource(id = R.string.nc_text_continue))
                }
            }, topBar = {
                NcTopAppBar(title = "", elevation = 0.dp, actions = {
                    IconButton(onClick = onMoreClicked) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_more),
                            contentDescription = "More icon"
                        )
                    }
                })
            }) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = stringResource(R.string.nc_main_wallet_setup_preference),
                    style = NunchukTheme.typography.heading
                )
                Text(
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
                    text = stringResource(R.string.nc_wallet_setup_preference_desc),
                    style = NunchukTheme.typography.body
                )

                NcRadioOption(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                        .fillMaxWidth(),
                    isSelected = isSinglePersonSetup.not(),
                    onClick = {
                        isSinglePersonSetup = false
                    }
                ) {
                    NcTag(
                        modifier = Modifier.padding(bottom = 4.dp),
                        label = stringResource(id = R.string.nc_advanced)
                    )
                    Text(
                        text = stringResource(R.string.nc_main_distributed_setup_desc),
                        style = NunchukTheme.typography.title
                    )
                }

                NcRadioOption(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    isSelected = isSinglePersonSetup,
                    onClick = {
                        isSinglePersonSetup = true
                    }
                ) {
                    Text(
                        text = stringResource(R.string.nc_main_option_single_person_setup_desc),
                        style = NunchukTheme.typography.title
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                NcHintMessage(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    messages = listOf(ClickAbleText(content = stringResource(R.string.nc_main_wallet_setup_preference_hint)))
                )
            }
        }
    }
}

@Preview
@Composable
private fun SelectGroupScreenPreview() {
    SelectWalletSetupContent(

    )
}