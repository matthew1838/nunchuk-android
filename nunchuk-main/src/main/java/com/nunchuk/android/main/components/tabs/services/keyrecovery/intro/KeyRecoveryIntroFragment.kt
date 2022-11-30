package com.nunchuk.android.main.components.tabs.services.keyrecovery.intro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.nunchuk.android.compose.*
import com.nunchuk.android.core.signer.SignerModel
import com.nunchuk.android.core.util.ClickAbleText
import com.nunchuk.android.core.util.flowObserver
import com.nunchuk.android.core.util.showOrHideLoading
import com.nunchuk.android.main.R
import com.nunchuk.android.main.components.tabs.services.keyrecovery.backupdownload.BackupDownloadFragmentArgs
import com.nunchuk.android.utils.parcelable
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class KeyRecoveryIntroFragment : Fragment() {

    private val viewModel by viewModels<KeyRecoveryIntroViewModel>()
    private val args: KeyRecoveryIntroFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                KeyRecoveryIntroScreen(viewModel)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(RecoveryTapSignerListBottomSheetFragment.REQUEST_KEY) { _, bundle ->
            val signer = bundle.parcelable<SignerModel>(RecoveryTapSignerListBottomSheetFragment.EXTRA_SELECTED_SIGNER_ID) ?: return@setFragmentResultListener
            openNextScreen(signer)
        }
    }

    private fun openNextScreen(signer: SignerModel) {
//        findNavController().navigate(
//            KeyRecoveryIntroFragmentDirections.actionKeyRecoveryIntroFragmentToAnswerSecurityQuestionFragment(signer)
//        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        flowObserver(viewModel.event) { event ->
            when (event) {
                is KeyRecoveryIntroEvent.ContinueClick -> {

                }
                is KeyRecoveryIntroEvent.Loading -> {
                    showOrHideLoading(loading = event.isLoading)
                }
                is KeyRecoveryIntroEvent.GetTapSignerSuccess -> {
                    findNavController().navigate(
                        KeyRecoveryIntroFragmentDirections.actionKeyRecoveryIntroFragmentToRecoverTapSignerListBottomSheetFragment(
                            event.signers.toTypedArray(),
                            args.verifyToken
                        )
                    )
//                    findNavController().navigate(
//                        KeyRecoveryIntroFragmentDirections.actionKeyRecoveryIntroFragmentToAnswerSecurityQuestionFragment()
//                    )
                }
            }
        }
    }
}

@Composable
fun KeyRecoveryIntroScreen(
    viewModel: KeyRecoveryIntroViewModel = viewModel()
) {
    KeyRecoveryIntroScreenContent(onContinueClicked = {
        viewModel.getTapSignerList()
    })
}

@Composable
fun KeyRecoveryIntroScreenContent(
    onContinueClicked: () -> Unit = {}
) {
    NunchukTheme {
        Scaffold { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .statusBarsPadding()
            ) {
                NunchukTheme {
                    Scaffold { innerPadding ->
                        Column(
                            modifier = Modifier
                                .padding(innerPadding)
                                .statusBarsPadding()
                                .navigationBarsPadding()
                        ) {
                            NcImageAppBar(
                                backgroundRes = R.drawable.nc_bg_key_recovery
                            )
                            LazyColumn(modifier = Modifier.weight(1.0f)) {
                                item {
                                    Text(
                                        modifier = Modifier.padding(
                                            top = 16.dp,
                                            start = 16.dp,
                                            end = 16.dp
                                        ),
                                        text = stringResource(R.string.nc_key_recovery),
                                        style = NunchukTheme.typography.heading
                                    )
                                    Text(
                                        modifier = Modifier.padding(
                                            top = 16.dp,
                                            start = 16.dp,
                                            end = 16.dp
                                        ),
                                        text = stringResource(R.string.nc_key_recovery_intro_desc),
                                        style = NunchukTheme.typography.body
                                    )
                                    NCLabelWithIndex(
                                        modifier = Modifier.padding(
                                            start = 16.dp,
                                            end = 16.dp,
                                            top = 24.dp
                                        ),
                                        index = 1,
                                        label = stringResource(R.string.nc_key_recovery_intro_info_1)
                                    )
                                    NCLabelWithIndex(
                                        modifier = Modifier.padding(
                                            start = 16.dp,
                                            end = 16.dp,
                                            top = 24.dp,
                                            bottom = 24.dp
                                        ),
                                        index = 2,
                                        label = stringResource(R.string.nc_key_recovery_intro_info_2)
                                    )
                                    Spacer(modifier = Modifier.weight(1.0f))
                                    NcHintMessage(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        messages = listOf(ClickAbleText(content = stringResource(R.string.nc_key_recovery_intro_notice)))
                                    )
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
                }
            }
        }
    }
}

@Preview
@Composable
private fun KeyRecoveryIntroScreenPreview() {
    KeyRecoveryIntroScreenContent()
}