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

package com.nunchuk.android.core.profile

import com.nunchuk.android.core.network.Data
import retrofit2.http.*

interface UserProfileApi {

    @GET("user/me")
    suspend fun getUserProfile(): Data<UserResponseWrapper>

    @POST("user/me/delete-confirmation")
    suspend fun confirmDeleteAccount(@Body payload: DeleteConfirmationPayload): Data<Unit>

    @DELETE("user/me")
    suspend fun requestDeleteAccount(): Data<Unit>

    @DELETE("passport/log-out")
    suspend fun signOut(): Data<Unit>

    @PUT("user/me")
    suspend fun updateUserProfile(@Body updatePayload: UpdateUserProfilePayload): Data<UserResponseWrapper>

    @GET("user/devices")
    suspend fun getUserDevices(): Data<UserDeviceWrapper>

    @HTTP(method = "DELETE", path = "user/devices", hasBody = true)
    suspend fun deleteUserDevices(
        @Body payload: DeleteDevicesPayload
    ): Data<Any>

    @POST("user/devices/mark-compromised")
    suspend fun compromiseUserDevices(
        @Body payload: CompromiseDevicesPayload
    ): Data<Any>
}