package com.example.dymessagelite.data.datasource.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dymessagelite.data.model.MegEntity

@Dao
interface MegDao {
    @Query("select * from MegEntity order by timestamp desc  limit :limit offset :offset")
    fun getMegList(limit: Int, offset: Int): List<MegEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdateMeg(meg: MegEntity)
}