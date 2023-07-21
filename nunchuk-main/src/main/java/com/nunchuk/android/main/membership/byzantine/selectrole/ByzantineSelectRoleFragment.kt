package com.nunchuk.android.main.membership.byzantine.selectrole

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.RadioButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.nunchuk.android.compose.NcPrimaryDarkButton
import com.nunchuk.android.compose.NcTopAppBar
import com.nunchuk.android.compose.NunchukTheme
import com.nunchuk.android.main.R
import com.nunchuk.android.model.byzantine.AssistedWalletRole
import com.nunchuk.android.share.membership.MembershipFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ByzantineSelectRoleFragment : MembershipFragment() {

    private val viewModel: ByzantineSelectRoleViewModel by viewModels()
    private val args: ByzantineSelectRoleFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                SelectRoleScreen(viewModel) {
                    setFragmentResult(
                        REQUEST_KEY,
                        bundleOf(EXTRA_ROLE to viewModel.getSelectedRole())
                    )
                    findNavController().popBackStack()
                }
            }
        }
    }


    companion object {
        const val REQUEST_KEY = "ByzantineSelectRoleFragment"
        const val EXTRA_ROLE = "EXTRA_ROLE"
    }
}

@Composable
private fun SelectRoleScreen(
    viewModel: ByzantineSelectRoleViewModel = viewModel(),
    onContinueClicked: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SelectRoleContent(
        selectedRole = state.selectedRole,
        options = state.roles,
        onOptionClick = viewModel::onOptionClick,
        onContinueClicked = onContinueClicked
    )
}

@Composable
private fun SelectRoleContent(
    options: List<AdvisorPlanRoleOption> = emptyList(),
    selectedRole: String = AssistedWalletRole.NONE.name,
    onOptionClick: (String) -> Unit = {},
    onContinueClicked: () -> Unit = {}
) = NunchukTheme {
    Scaffold(
        modifier = Modifier
            .navigationBarsPadding()
            .statusBarsPadding(),
        bottomBar = {
            NcPrimaryDarkButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = selectedRole != AssistedWalletRole.NONE.name,
                onClick = onContinueClicked
            ) {
                Text(text = stringResource(id = R.string.nc_text_continue))
            }
        }, topBar = {
            NcTopAppBar(title = "", elevation = 0.dp, isBack = false, actions = {
                Spacer(modifier = Modifier.size(LocalViewConfiguration.current.minimumTouchTargetSize))
            })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                item {
                    Text(
                        modifier = Modifier.padding(top = 0.dp, start = 16.dp, end = 16.dp),
                        text = stringResource(R.string.nc_select_a_role),
                        style = NunchukTheme.typography.heading
                    )
                    options.forEach { item ->
                        val title = when (item.role) {
                            AssistedWalletRole.ADMIN.name -> stringResource(id = R.string.nc_keyholder_admin)
                            AssistedWalletRole.KEYHOLDER.name -> stringResource(id = R.string.nc_keyholder)
                            AssistedWalletRole.OBSERVER.name -> stringResource(id = R.string.nc_observer)
                            else -> ""
                        }
                        OptionItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                            isSelected = selectedRole == item.role,
                            desc = item.desc,
                            title = title
                        ) {
                            onOptionClick(item.role)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun OptionItem(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    title: String,
    desc: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier, onClick = onClick,
        border = BorderStroke(
            width = 2.dp,
            color = if (isSelected) colorResource(id = R.color.nc_primary_color) else Color(
                0xFFDEDEDE
            )
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = isSelected, onClick = onClick)
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(text = title, style = NunchukTheme.typography.title)
                Text(text = desc, style = NunchukTheme.typography.body)
            }
        }
    }
}

@Preview
@Composable
private fun SelectRoleScreenPreview() {
    SelectRoleContent()
}