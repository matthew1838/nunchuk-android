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

package com.nunchuk.android.auth.components.signin

import com.nunchuk.android.core.network.ErrorDetail
import com.nunchuk.android.model.PrimaryKey

internal sealed class SignInEvent {
    object EmailRequiredEvent : SignInEvent()
    object EmailValidEvent : SignInEvent()
    object EmailInvalidEvent : SignInEvent()
    object PasswordRequiredEvent : SignInEvent()
    object PasswordValidEvent : SignInEvent()
    object ProcessingEvent : SignInEvent()
    data class SignInSuccessEvent(val token: String, val deviceId: String) : SignInEvent()
    data class SignInErrorEvent(val code: Int? = null, val message: String? = null, val errorDetail: ErrorDetail? = null) : SignInEvent()
    data class CheckPrimaryKeyAccountEvent(val accounts: ArrayList<PrimaryKey>) : SignInEvent()
}