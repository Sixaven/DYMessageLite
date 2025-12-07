package com.example.dymessagelite.data.datasource.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.dymessagelite.data.model.detail.ChatEntity
import com.example.dymessagelite.data.model.detail.ChatType
import com.example.dymessagelite.data.model.list.MegEntity

@Dao
interface ChatDao {


    @Insert
    suspend fun insertChat(chat: ChatEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateChat(meg: ChatEntity)

    @Query("select * from ChatEntity where id = :chatId")
    suspend fun getChatById(chatId: Int): ChatEntity?

    @Query("select * from ChatEntity where senderId = :senderId order by timestamp asc")
    suspend fun getChatList(senderId: String): List<ChatEntity>

    @Query(
        """
        SELECT * FROM ChatEntity 
        WHERE content LIKE '%' || :keyword || '%' 
        AND type = ${ChatType.TEXT} 
        ORDER BY timestamp DESC
    """
    )
    suspend fun searchChatsByContent(keyword: String): List<ChatEntity>?

    @Query(
        """
        SELECT COUNT(*)
        FROM ChatEntity 
        WHERE senderId = :senderId 
        AND isRead = 0 
        AND timestamp >= :sinceTimestamp 
        AND isMine = 0
    """
    )
    suspend fun getTodayUnreadCountBySenderId(senderId: String, sinceTimestamp: Long): Int


    @Query(
        """
        SELECT COUNT(*) 
        FROM ChatEntity 
        WHERE senderId = :senderId 
        AND isClick = 1
        AND isMine = 0
    """
    )
    suspend fun getClickCountBySenderId(senderId: String): Int

    @Query(
        """
        SELECT COUNT(*) 
        FROM ChatEntity 
        WHERE senderId = :senderId 
        AND isDisplay = 1
        AND isMine = 0
    """
    )
    suspend fun getDisplayCountBySenderId(senderId: String): Int

    @Query("""
        SELECT COUNT(*) 
        FROM ChatEntity 
        WHERE senderId = :senderId
        AND isMine = 0
    """)
    suspend fun getAllCountBySenderId(senderId: String): Int

    @Query("""
        SELECT COUNT(*) 
        FROM ChatEntity 
        WHERE senderId = :senderId 
        AND type = :type 
        AND isDisplay = 1
        AND isMine = 0
    """)
    suspend fun getDisplayCountBySenderIdAndType(senderId: String, type: Int): Int

    @Query("""
        SELECT COUNT(*) 
        FROM ChatEntity 
        WHERE senderId = :senderId 
        AND type = :type 
        AND isMine = 0
    """)
    suspend fun getAllCountBySenderIdAndType(senderId: String, type: Int): Int
}