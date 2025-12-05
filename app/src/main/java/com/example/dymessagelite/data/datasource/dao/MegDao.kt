package com.example.dymessagelite.data.datasource.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dymessagelite.data.model.MegEntity

@Dao
interface MegDao {
    @Query("select * from MegEntity order by timestamp desc  limit :limit offset :offset")
    suspend fun getMegList(limit: Int, offset: Int): List<MegEntity>

    @Query("select * from MegEntity where name = :senderId")
    suspend fun getMegBySenderId(senderId: String): MegEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMeg(meg: MegEntity)
    @Query("SELECT * FROM MegEntity WHERE name LIKE '%' || :keyword || '%'")
    suspend fun searchMegsByName(keyword: String): List<MegEntity>
    @Query("select * from MegEntity")
    suspend fun getAllMegs(): List<MegEntity>


}