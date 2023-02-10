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

package com.nunchuk.android.database.di

import android.content.Context
import androidx.room.Room
import com.nunchuk.android.persistence.DATABASE_NAME
import com.nunchuk.android.persistence.DBMigrations
import com.nunchuk.android.persistence.NunchukDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object NunchukPersistenceModule {

    @Singleton
    @Provides
    fun provideDatabase(context: Context) =
        Room.databaseBuilder(context, NunchukDatabase::class.java, DATABASE_NAME)
            .addMigrations(DBMigrations.MIGRATION_1_2)
            .addMigrations(DBMigrations.MIGRATION_2_3)
            .addMigrations(DBMigrations.MIGRATION_3_4)
            .addMigrations(DBMigrations.MIGRATION_4_5)
            .build()

    @Singleton
    @Provides
    fun provideContactDao(database: NunchukDatabase) = database.contactDao()

    @Singleton
    @Provides
    fun provideSyncFileDao(database: NunchukDatabase) = database.syncFileDao()

    @Singleton
    @Provides
    fun provideMembershipStepDao(database: NunchukDatabase) = database.membershipDao()

    @Singleton
    @Provides
    fun provideSyncEventDao(database: NunchukDatabase) = database.syncEventDao()

    @Singleton
    @Provides
    fun provideHandledEventDao(database: NunchukDatabase) = database.handledEventDao()
}