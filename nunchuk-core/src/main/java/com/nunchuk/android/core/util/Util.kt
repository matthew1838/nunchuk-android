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

package com.nunchuk.android.core.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.util.Linkify
import android.widget.TextView
import com.nunchuk.android.core.network.UNKNOWN_ERROR
import com.nunchuk.android.model.Transaction
import com.nunchuk.android.type.ConnectionStatus
import com.nunchuk.android.utils.CrashlyticsReporter
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.properties.ObservableProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun Throwable.readableMessage() = message ?: UNKNOWN_ERROR

fun Exception.messageOrUnknownError() = message.orUnknownError()

fun String?.orUnknownError() = this ?: UNKNOWN_ERROR

fun String.isValidCvc() = length in MIN_CVC_LENGTH..MAX_CVC_LENGTH

const val MAX_CVC_LENGTH = 32
const val MIN_CVC_LENGTH = 6

const val CHAIN_CODE_LENGTH = 64

var BTC_USD_EXCHANGE_RATE = 45000.0

var BLOCKCHAIN_STATUS: ConnectionStatus? = null

const val TWITTER_LINK = "https://twitter.com/nunchuk_io"
const val CONTACT_EMAIL = "contact@nunchuk.io"

const val SATOSHI_BTC_EXCHANGE_RATE = 0.00000001
const val BTC_SATOSHI_EXCHANGE_RATE = 100000000

const val NFC_CARD_TIMEOUT = 5000
const val NFC_DEFAULT_NAME = "NFC Key"

fun Long.formatDate(): String = SimpleDateFormat("MM/dd/yyyy 'at' HH:mm aaa", Locale.US).format(Date(this * 1000))

fun Transaction.getFormatDate(): String = if (blockTime <= 0) "--/--/--" else (blockTime).formatDate()

fun String.fromMxcUriToMatrixDownloadUrl(): String {
    if (this.isEmpty()) return ""

    // Sample: https://matrix.nunchuk.io/_matrix/media/r0/download/nunchuk.io/occyhYuhbbpkHNbJLZwwdtuf
    val contentUriInfo = this.removePrefix("mxc://").split("/")

    val serverName = if (contentUriInfo.isEmpty()) "" else contentUriInfo[0]
    val mediaId = if (contentUriInfo.isEmpty()) "" else contentUriInfo[1]
    return BASE_DOWNLOAD_URL_MATRIX.plus(serverName).plus("/").plus(mediaId)
}

internal const val BASE_DOWNLOAD_URL_MATRIX = "https://matrix.nunchuk.io/_matrix/media/r0/download/"

fun InputStream.saveToFile(file: String) = try {
    use { input ->
        File(file).outputStream().use { output ->
            input.copyTo(output)
        }
    }
} catch (t: Throwable) {
    CrashlyticsReporter.recordException(t)
    0
}

fun TextView.linkify(textToLink: String, url: String) {
    val pattern = Pattern.compile(textToLink)
    Linkify.addLinks(this, pattern, url, { _, _, _ -> true }, { _, _ -> "" })
}

inline fun <T> observable(
    initialValue: T,
    crossinline onChange: (newValue: T) -> Unit
): ReadWriteProperty<Any?, T> = object : ObservableProperty<T>(initialValue) {
    override fun beforeChange(property: KProperty<*>, oldValue: T, newValue: T): Boolean {
        return oldValue != newValue
    }

    override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) = onChange(newValue)
}

fun Context.copyToClipboard(label: String, text: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip =
        ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
}

