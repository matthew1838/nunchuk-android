package com.nunchuk.android.main.components.tabs.services.inheritanceplanning.claimbufferperiod

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.navigation.fragment.navArgs
import com.nunchuk.android.compose.*
import com.nunchuk.android.core.manager.ActivityManager
import com.nunchuk.android.main.R
import com.nunchuk.android.model.BufferPeriodCountdown

class InheritanceClaimBufferPeriodFragment : Fragment() {
    private val args: InheritanceClaimBufferPeriodFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                InheritanceClaimScreen(args) {
                    requireActivity().finish()
                }
            }
        }
    }
}

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun InheritanceClaimScreen(
    args: InheritanceClaimBufferPeriodFragmentArgs,
    onGotItClick: () -> Unit = {}
) {
    InheritanceClaimBufferPeriodContent(
        countdown = args.countdownBufferPeriod,
        onGotItClick = onGotItClick
    )
}

@ExperimentalLifecycleComposeApi
@Composable
private fun InheritanceClaimBufferPeriodContent(
    countdown: BufferPeriodCountdown,
    onGotItClick: () -> Unit = {},
) {
    NunchukTheme {
        Scaffold { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .navigationBarsPadding()
            ) {
                NcImageAppBar(
                    backgroundRes = R.drawable.bg_buffer_period_illustration,
                    title = "",
                )
                Text(
                    modifier = Modifier.padding(top = 24.dp, start = 16.dp, end = 16.dp),
                    text = stringResource(R.string.nc_buffer_period_has_started),
                    style = NunchukTheme.typography.heading
                )
                Text(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                    text = stringResource(R.string.nc_buffer_period_has_started_desc),
                    style = NunchukTheme.typography.body
                )
                NcHighlightText(
                    modifier = Modifier.padding(16.dp),
                    text = stringResource(R.string.nc_check_back_in, countdown.remainingDisplayName),
                    style = NunchukTheme.typography.body
                )
                Spacer(modifier = Modifier.weight(1.0f))
                NcPrimaryDarkButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    onClick = onGotItClick,
                ) {
                    Text(text = stringResource(id = R.string.nc_text_got_it))
                }
            }
        }
    }
}

@OptIn(ExperimentalLifecycleComposeApi::class)
@Preview
@Composable
private fun InheritanceClaimBufferPeriodScreenPreview() {
    InheritanceClaimBufferPeriodContent(countdown = BufferPeriodCountdown(0, "0", 0, 0, "0"))
}