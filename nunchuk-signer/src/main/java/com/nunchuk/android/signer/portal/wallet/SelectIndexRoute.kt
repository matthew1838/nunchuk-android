package com.nunchuk.android.signer.portal.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.nunchuk.android.compose.NcHintMessage
import com.nunchuk.android.compose.NcImageAppBar
import com.nunchuk.android.compose.NcPrimaryDarkButton
import com.nunchuk.android.compose.NcScaffold
import com.nunchuk.android.compose.NcTextField
import com.nunchuk.android.compose.NunchukTheme
import com.nunchuk.android.core.util.ClickAbleText
import com.nunchuk.android.signer.R

const val selectIndexRoute = "select_index"

fun NavGraphBuilder.selectIndex(
    snackState: SnackbarHostState = SnackbarHostState(),
    onSelectIndex: (Int) -> Unit = { },
) {
    composable(selectIndexRoute) {
        SelectIndexScreen(
            onSelectIndex = onSelectIndex,
            snackState = snackState
        )
    }
}

fun NavController.navigateToSelectIndex(navOptions: NavOptions? = null) {
    navigate(selectIndexRoute, navOptions)
}

@Composable
fun SelectIndexScreen(
    modifier: Modifier = Modifier,
    snackState: SnackbarHostState = SnackbarHostState(),
    onSelectIndex: (Int) -> Unit = { },
) {
    var index by rememberSaveable { mutableStateOf("0") }
    NcScaffold(
        snackState = snackState,
        modifier = modifier.navigationBarsPadding(),
        topBar = {
            NcImageAppBar(backgroundRes = R.drawable.nc_bg_select_index)
        },
        bottomBar = {
            NcPrimaryDarkButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                onClick = { onSelectIndex(index.toInt()) },
            ) {
                Text(text = stringResource(id = R.string.nc_text_continue))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = "Select account index", style = NunchukTheme.typography.heading)

            NcTextField(
                title = "Account index", value = index,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Number
                ),
                rightContent = {
                    if (index.isNotEmpty()) {
                        IconButton(onClick = { index = "" }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_close),
                                contentDescription = "Close"
                            )
                        }
                    }
                }
            ) {
                if (it.isEmpty() || it.all { c -> c.isDigit() }) {
                    index = it
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            NcHintMessage(
                modifier = Modifier.fillMaxWidth(),
                messages = listOf(ClickAbleText(content = "Each key can support multiple accounts, as per BIP 32. Leave it as 0 (the first account) if you are unsure."))
            )
        }
    }
}

@Preview
@Composable
private fun SelectIndexScreenPreview() {
    NunchukTheme {
        SelectIndexScreen()
    }
}