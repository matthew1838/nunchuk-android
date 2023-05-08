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

package com.nunchuk.android.compose

import androidx.compose.material.Colors
import androidx.compose.ui.graphics.Color

object NcColor {
    val greyDark = Color(0xFF595959)
    val boulder = Color(0xFF757575)
    val border = Color(0xFFDEDEDE)
    val whisper = Color(0xFFEAEAEA)
    val denimTint = Color(0xFFD0E2FF)
    val greyLight = Color(0xFFF5F5F5)
    val denimDark = Color(0xFF2F466C)
    val white = Color(0xFFFFFFFF)
    val beeswaxLight = Color(0xFFFDD95C)
}

val Colors.border: Color
    get() = Color(0xFFDEDEDE)

val Colors.greyLight: Color
    get() = Color(0xFFF5F5F5)

val Colors.whisper: Color
    get() = Color(0xFFEAEAEA)

val Colors.denimTint: Color
    get() = Color(0xFFD0E2FF)
