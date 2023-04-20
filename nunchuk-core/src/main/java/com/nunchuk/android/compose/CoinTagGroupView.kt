package com.nunchuk.android.compose

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nunchuk.android.core.R
import com.nunchuk.android.core.util.openExternalLink
import com.nunchuk.android.model.CoinTag

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CoinTagGroupView(
    modifier: Modifier = Modifier,
    note: String = "",
    tagIds: Set<Int>,
    tags: Map<Int, CoinTag>,
    onViewTagDetail: (tag: CoinTag) -> Unit = {}
) {
    val context = LocalContext.current
    var isTextOverFlow by remember { mutableStateOf(false) }
    var isNoteExpand by remember { mutableStateOf(false) }
    var onTagExpand by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, NcColor.border, RoundedCornerShape(12.dp))
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            maxItemsInEachRow = 4
        ) {
            tagIds.take(if (onTagExpand) Int.MAX_VALUE else 5).mapNotNull { tags[it] }
                .sortedBy { it.name }.forEach { coinTag ->
                    CoinTagView(
                        modifier = Modifier
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                            .clickable { onViewTagDetail(coinTag) },
                        tag = coinTag
                    )
                }
            if (tagIds.size > 5) {
                Text(
                    modifier = Modifier
                        .padding(top = 4.dp, start = 4.dp, end = 8.dp)
                        .background(
                            color = NcColor.greyLight,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = NcColor.border,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .clickable { onTagExpand = onTagExpand.not() },
                    text = if (onTagExpand) stringResource(R.string.nc_show_less) else "${tagIds.size - 5} more tags",
                    style = NunchukTheme.typography.bodySmall,
                )
            }
        }
        Row(
            modifier = Modifier
                .padding(bottom = if (note.isNotEmpty()) 8.dp else 0.dp)
                .clickable {
                    runCatching {
                        val matcher = Patterns.WEB_URL.matcher(note)
                        if (matcher.find()) {
                            val link = note.substring(matcher.start(1), matcher.end())
                            context.openExternalLink(link)
                        }
                    }
                },
        ) {
            if (note.isNotEmpty()) {
                Icon(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .border(1.dp, color = NcColor.border, shape = CircleShape)
                        .padding(4.dp),
                    tint = MaterialTheme.colors.primary,
                    painter = painterResource(id = R.drawable.ic_transaction_note),
                    contentDescription = "Transaction Note"
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                ) {
                    NcLinkifyText(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        text = note,
                        maxLines = if (isNoteExpand) Int.MAX_VALUE else 1,
                        overflow = TextOverflow.Ellipsis,
                        style = NunchukTheme.typography.bodySmall,
                        onTextLayout = {
                            if (it.hasVisualOverflow) {
                                isTextOverFlow = true
                            }
                        }
                    )
                    if (isNoteExpand) {
                        Text(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .background(
                                    color = NcColor.greyLight,
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = NcColor.border,
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                .clickable { isNoteExpand = false },
                            text = stringResource(R.string.nc_show_less),
                            style = NunchukTheme.typography.bodySmall,
                        )
                    }
                }
                if (isTextOverFlow && isNoteExpand.not()) {
                    Text(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background(
                                color = NcColor.greyLight,
                                shape = RoundedCornerShape(24.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = NcColor.border,
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                            .clickable { isNoteExpand = true },
                        text = stringResource(R.string.nc_more),
                        style = NunchukTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CoinTagGroupViewPreview() {
    NunchukTheme {
        CoinTagGroupView(
            modifier = Modifier.padding(16.dp),
            tagIds = setOf(1, 2, 3, 4, 5, 6, 7),
            tags = mapOf(
                1 to CoinTag(id = 1, name = "badcoins", color = "#000000"),
                2 to CoinTag(id = 2, name = "badcoins", color = "#000000"),
                3 to CoinTag(id = 3, name = "badcoins", color = "#000000"),
                4 to CoinTag(id = 4, name = "badcoins", color = "#000000"),
                5 to CoinTag(id = 5, name = "badcoins", color = "#000000"),
                6 to CoinTag(id = 6, name = "badcoins", color = "#000000"),
                7 to CoinTag(id = 7, name = "badcoins", color = "#000000"),
            ),
            note = "Send to Bob on Silk Road"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CoinTagGroupViewPreviewNoNote() {
    NunchukTheme {
        CoinTagGroupView(
            modifier = Modifier.padding(16.dp),
            tagIds = setOf(1, 2, 3, 4, 5, 6, 7),
            tags = mapOf(
                1 to CoinTag(id = 1, name = "badcoins", color = "#DDFFFF"),
                2 to CoinTag(id = 2, name = "badcoins", color = "#000000"),
                3 to CoinTag(id = 3, name = "badcoins", color = "#000000"),
                4 to CoinTag(id = 4, name = "badcoins", color = "#000000"),
                5 to CoinTag(id = 5, name = "badcoins", color = "#000000"),
                6 to CoinTag(id = 6, name = "badcoins", color = "#000000"),
                7 to CoinTag(id = 7, name = "badcoins", color = "#000000"),
            ),
            note = "",
        )
    }
}