package com.nunchuk.android.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nunchuk.android.core.util.shorten
import com.nunchuk.android.model.CoinCollection

@Composable
fun CoinCollectionView(
    modifier: Modifier = Modifier,
    textStyle: TextStyle = NunchukTheme.typography.body,
    collection: CoinCollection,
    clickable: Boolean = false,
    onClick: () -> Unit = {}
) {
    Column(
        modifier
            .width(80.dp)
            .clickable(enabled = clickable, onClick = onClick),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp, 60.dp)
                .clip(CircleShape)
                .background(color = NcColor.beeswaxLight),
            contentAlignment = Alignment.Center
        ) {
            Text(text = collection.name.shorten())
        }
        Text(
            modifier = Modifier
                .padding(top = 12.dp)
                .fillMaxWidth(),
            text = collection.name,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = textStyle,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CoinCollectionViewPreview() {
    NunchukTheme {
        CoinCollectionView(
            collection = CoinCollection(
                id = 1, name = "Unfiltered coins"
            )
        )
    }
}