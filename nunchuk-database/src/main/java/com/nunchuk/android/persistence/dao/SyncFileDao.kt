package com.nunchuk.android.persistence.dao

import androidx.room.Dao
import androidx.room.Query
import com.nunchuk.android.persistence.BaseDao
import com.nunchuk.android.persistence.TABLE_SYNC_FILE
import com.nunchuk.android.persistence.entity.SyncFileEntity
import io.reactivex.Flowable

@Dao
interface SyncFileDao : BaseDao<SyncFileEntity> {

    @Query("SELECT * FROM $TABLE_SYNC_FILE WHERE user_id = :userId")
    fun getSyncFiles(userId: String): Flowable<List<SyncFileEntity>>

    @Query("DELETE FROM $TABLE_SYNC_FILE WHERE id IN (:syncFileIds)")
    fun deleteSyncFiles(syncFileIds: List<String>)

}
