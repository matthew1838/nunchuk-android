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

package com.nunchuk.android.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.nunchuk.android.persistence.dao.ContactDao
import com.nunchuk.android.persistence.dao.SyncEventDao
import com.nunchuk.android.persistence.dao.SyncFileDao
import com.nunchuk.android.persistence.entity.ContactEntity
import com.nunchuk.android.persistence.entity.SyncEventEntity
import com.nunchuk.android.persistence.entity.SyncFileEntity

@Database(
    entities = [
        ContactEntity::class,
        SyncFileEntity::class,
        SyncEventEntity::class,
    ],
    version = DATABASE_VERSION,
    exportSchema = true
)

internal abstract class NunchukDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun syncFileDao(): SyncFileDao
    abstract fun syncEventDao(): SyncEventDao

    companion object {

        @Volatile
        private var INSTANCE: NunchukDatabase? = null

        fun getInstance(context: Context) = INSTANCE ?: synchronized(this) {
            INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
        }

        private fun buildDatabase(applicationContext: Context) =
            Room.databaseBuilder(applicationContext, NunchukDatabase::class.java, DATABASE_NAME)
                .addMigrations(DBMigrations.MIGRATION_1_2)
                .addMigrations(DBMigrations.MIGRATION_2_3)
                .build()
    }
}

