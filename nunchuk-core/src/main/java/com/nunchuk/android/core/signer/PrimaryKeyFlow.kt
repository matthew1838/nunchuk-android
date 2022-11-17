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

import androidx.annotation.IntDef

object PrimaryKeyFlow {

    const val NONE = 0
    const val SIGN_UP = 1
    const val SIGN_IN = 2
    const val REPLACE = 3

    @IntDef(
        NONE,
        SIGN_UP,
        SIGN_IN,
        REPLACE
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class PrimaryFlowInfo

    fun Int.isPrimaryKeyFlow() = this != NONE
    fun Int.isSignUpFlow() = this == SIGN_UP
    fun Int.isSignInFlow() = this == SIGN_IN
    fun Int.isReplaceFlow() = this == REPLACE
}