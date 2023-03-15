package com.nunchuk.android.wallet.components.coin.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nunchuk.android.compose.NcColor
import com.nunchuk.android.compose.NunchukTheme
import com.nunchuk.android.core.util.getBTCAmount
import com.nunchuk.android.model.Amount
import com.nunchuk.android.model.CoinTag
import com.nunchuk.android.model.UnspentOutput
import com.nunchuk.android.type.TransactionStatus
import com.nunchuk.android.utils.formatByHour
import com.nunchuk.android.utils.simpleDateFormat
import com.nunchuk.android.wallet.R
import java.util.*

@Composable
fun PreviewCoinCard(
    output: UnspentOutput,
    tags: Map<Int, CoinTag>,
    selectable: Boolean = false,
    isSelected: Boolean = false,
    onViewCoinDetail: (output: UnspentOutput) -> Unit = {},
    onSelectCoin: (output: UnspentOutput, isSelected: Boolean) -> Unit = { _, _ -> }
) {
    Box(modifier = Modifier.run {
        if (selectable.not()) {
            this.clickable { onViewCoinDetail(output) }
        } else {
            this
        }
    }) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (LocalView.current.isInEditMode)
                        "${output.amount.value} sats"
                    else
                        output.amount.getBTCAmount(),
                    style = NunchukTheme.typography.title
                )
                if (output.isChange) {
                    Text(
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .background(
                                color = MaterialTheme.colors.background,
                                shape = RoundedCornerShape(24.dp)
                            )
                            .border(
                                1.dp,
                                color = NcColor.whisper,
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        text = stringResource(R.string.nc_change),
                        style = NunchukTheme.typography.titleSmall.copy(fontSize = 10.sp)
                    )
                }
                if (output.isLocked) {
                    Icon(
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .background(
                                color = colorResource(id = R.color.nc_whisper_color),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                            .size(12.dp),
                        painter = painterResource(id = R.drawable.ic_lock),
                        contentDescription = "Lock"
                    )
                }
                if (output.scheduleTime > 0L) {
                    Icon(
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .background(
                                color = colorResource(id = R.color.nc_whisper_color),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                            .size(12.dp),
                        painter = painterResource(id = R.drawable.ic_schedule),
                        contentDescription = "Schedule"
                    )
                }
            }
            if (output.time > 0L) {
                val date = Date(output.time)
                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = "${date.simpleDateFormat()} at ${date.formatByHour()}",
                    style = NunchukTheme.typography.bodySmall
                )
            } else {
                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = "--/--/--",
                    style = NunchukTheme.typography.bodySmall
                )
            }

            if (output.tags.isNotEmpty() || output.memo.isNotEmpty()) {
                CoinTagGroupView(
                    modifier = Modifier.padding(top = 4.dp),
                    note = output.memo,
                    tagIds = output.tags,
                    tags = tags
                )
            }
        }
        if (selectable) {
            Checkbox(modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp),
                checked = isSelected,
                onCheckedChange = { select ->
                    onSelectCoin(output, select)
                })
        } else {
            IconButton(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp),
                onClick = { onViewCoinDetail(output) }) {
                Icon(painter = painterResource(id = R.drawable.ic_arrow), contentDescription = "")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCoinCardPreview() {
    NunchukTheme {
        PreviewCoinCard(
            output = UnspentOutput(
                amount = Amount(1000000L),
                isLocked = true,
                scheduleTime = System.currentTimeMillis(),
                isChange = true,
                time = System.currentTimeMillis(),
                tags = setOf(1, 2, 3, 4),
                memo = "Send to Bob on Silk Road",
                status = TransactionStatus.PENDING_CONFIRMATION
            ),
            tags = emptyMap()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCoinCardPreview2() {
    NunchukTheme {
        PreviewCoinCard(
            output = UnspentOutput(
                amount = Amount(1000000L),
                isLocked = false,
                scheduleTime = System.currentTimeMillis(),
                time = System.currentTimeMillis(),
                tags = setOf(),
                memo = "",
                status = TransactionStatus.PENDING_CONFIRMATION
            ),
            tags = emptyMap()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCoinCardPreview3() {
    NunchukTheme {
        PreviewCoinCard(
            output = UnspentOutput(
                amount = Amount(1000000L),
                isLocked = false,
                scheduleTime = System.currentTimeMillis(),
                time = System.currentTimeMillis(),
                tags = setOf(),
                memo = "",
                status = TransactionStatus.PENDING_CONFIRMATION
            ),
            tags = emptyMap(),
            selectable = true,
            isSelected = true
        )
    }
}