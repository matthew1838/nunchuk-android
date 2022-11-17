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

package com.nunchuk.android.core.di

import com.nunchuk.android.core.profile.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface UserProfileDomainModule {
    @Binds
    fun bindCompromiseUserDevicesUseCase(userCase: CompromiseUserDevicesUseCaseImpl): CompromiseUserDevicesUseCase

    @Binds
    fun bindDeleteUserDevicesUseCase(userCase: DeleteUserDevicesUseCaseImpl): DeleteUserDevicesUseCase

    @Binds
    fun bindGetUserDevicesUseCase(userCase: GetUserDevicesUseCaseImpl): GetUserDevicesUseCase

    @Binds
    fun bindGetUserProfileUseCase(userCase: GetUserProfileUseCaseImpl): GetUserProfileUseCase

    @Binds
    fun bindUpdateUserProfileUseCase(userCase: UpdateUseProfileUseCaseImpl): UpdateUseProfileUseCase

}

@Module
@InstallIn(SingletonComponent::class)
internal interface UserProfileDataModule {

    @Binds
    fun bindGetUserProfileRepository(userCase: UserProfileRepositoryImpl): UserProfileRepository

}

@Module
@InstallIn(SingletonComponent::class)
internal object UserProfileNetworkModule {

    @Singleton
    @Provides
    fun provideUserProfileApi(retrofit: Retrofit): UserProfileApi =
        retrofit.create(UserProfileApi::class.java)

}