package com.example.dymessagelite.data.datasource.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.dymessagelite.data.model.ChatEntity

@Dao
interface ChatDao {
    @Query("select * from ChatEntity where senderId = :senderId order by timestamp asc")
     fun getChatList(senderId: String): List<ChatEntity>

    @Insert
     fun insertChat(chat: ChatEntity)

}