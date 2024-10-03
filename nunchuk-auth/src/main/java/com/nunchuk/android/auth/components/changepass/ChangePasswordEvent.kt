/**************************************************************************
 * This file is part of the Nunchuk software (https://nunchuk.io/)        *
 * Copyright (C) 2022, 2023 Nunchuk                                       *
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

package com.nunchuk.android.auth.components.changepass

internal sealed class ChangePasswordEvent {
    data class ShowEmailSentEvent(val email: String) : ChangePasswordEvent()
    data object OldPasswordRequiredEvent : ChangePasswordEvent()
    data object NewPasswordRequiredEvent : ChangePasswordEvent()
    data object ConfirmPasswordRequiredEvent : ChangePasswordEvent()
    data object ConfirmPasswordNotMatchedEvent : ChangePasswordEvent()
    data object OldPasswordValidEvent : ChangePasswordEvent()
    data object NewPasswordValidEvent : ChangePasswordEvent()
    data object ConfirmPasswordValidEvent : ChangePasswordEvent()
    data object ChangePasswordSuccessEvent : ChangePasswordEvent()
    data object LoadingEvent : ChangePasswordEvent()
    data class ChangePasswordSuccessError(val errorMessage: String?) : ChangePasswordEvent()
    data class ResendPasswordSuccessEvent(val email: String) : ChangePasswordEvent()
    data object NewPasswordLengthErrorEvent : ChangePasswordEvent()
    data object NewPasswordSpecialCharErrorEvent : ChangePasswordEvent()
    data object NewPasswordUpperCaseErrorEvent : ChangePasswordEvent()
    data object NewPasswordNumberErrorEvent : ChangePasswordEvent()
}