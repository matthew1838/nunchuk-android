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

package com.nunchuk.android.core.signer

import android.os.Parcelable
import com.nunchuk.android.model.JoinKey
import com.nunchuk.android.model.SingleSigner
import com.nunchuk.android.type.SignerType
import kotlinx.parcelize.Parcelize
import java.util.regex.Pattern

@Parcelize
data class SignerModel(
    val id: String,
    val name: String,
    val derivationPath: String,
    val fingerPrint: String,
    val used: Boolean = false,
    val type: SignerType = SignerType.AIRGAP,
    val software: Boolean = false,
    val localKey: Boolean = true,
    val isPrimaryKey: Boolean = false,
    val cardId: String = "",
) : Parcelable {
    val isEditablePath: Boolean
        get() = type == SignerType.HARDWARE || type == SignerType.SOFTWARE

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SignerModel

        if (fingerPrint != other.fingerPrint) return false
        if (derivationPath != other.derivationPath) return false

        return true
    }

    override fun hashCode(): Int {
        var result = derivationPath.hashCode()
        result = 31 * result + fingerPrint.hashCode()
        return result
    }

    fun getXfpOrCardIdLabel() = if (type == SignerType.NFC && cardId.isNotEmpty()) {
        "Card ID: ...${cardIdShorten()}"
    } else if (fingerPrint.isNotEmpty()) {
        "XFP: $fingerPrint"
    } else ""

    fun cardIdShorten() = cardId.takeLast(5)
}

fun SingleSigner.toModel(isPrimaryKey: Boolean = false) = SignerModel(
    id = masterSignerId,
    name = name.ifEmpty { masterFingerprint },
    derivationPath = derivationPath,
    type = type,
    used = used,
    software = type == SignerType.SOFTWARE,
    fingerPrint = masterFingerprint,
    isPrimaryKey = isPrimaryKey
)

fun JoinKey.toSignerModel() = SignerModel(
    id = chatId,
    name = name,
    derivationPath = derivationPath,
    fingerPrint = masterFingerprint,
    type = SignerType.valueOf(signerType)
)

data class SignerInput(
    val fingerPrint: String,
    val derivationPath: String,
    val xpub: String
)

class InvalidSignerFormatException(override val message: String) : Exception()

fun String.toSigner(): SignerInput {
    val trimmed = trim()
    val pattern = Pattern.compile("^\\[([0-9a-fA-F]{8})/(.*)]([^/]+).*\$")
    val matcher = pattern.matcher(trimmed)
    if (matcher.find()) {
        val fingerPrint = requireNotNull(matcher.group(1))
        val derivationPath = "m/${requireNotNull(matcher.group(2))}"
        val xpub = requireNotNull(matcher.group(3))
        return SignerInput(fingerPrint = fingerPrint, derivationPath = derivationPath, xpub = xpub)
    }
    throw InvalidSignerFormatException(this)
}
