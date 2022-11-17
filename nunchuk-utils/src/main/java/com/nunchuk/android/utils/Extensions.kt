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

package com.nunchuk.android.utils

import android.widget.EditText
import kotlin.math.roundToInt

fun CharSequence?.safeManualFee() = try {
    if (isNullOrEmpty()) 0 else (toString().toDouble() * 1000).roundToInt()
} catch (t: Throwable) {
    CrashlyticsReporter.recordException(t)
    0
}

fun CharSequence?.isNoneEmpty() = this?.toString().orEmpty().isNotEmpty()

fun EditText?.getTrimmedText() = this?.text?.trim().toString()

@Suppress("unused")
fun <T> (() -> T).safe(): T? = try {
    this()
} catch (t: Throwable) {
    CrashlyticsReporter.recordException(t)
    null
}

inline fun <T> trySafe(func: () -> T): T? = try {
    func()
} catch (t: Throwable) {
    CrashlyticsReporter.recordException(t)
    null
}