package com.example.dymessagelite.data.datasource.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.dymessagelite.data.model.ChatEntity

@Dao
interface ChatDao {
    @Query("select * from ChatEntity where senderId = :senderId order by timestamp asc")
    suspend fun getChatList(senderId: String): List<ChatEntity>

    @Insert
    suspend fun insertChat(chat: ChatEntity)

}