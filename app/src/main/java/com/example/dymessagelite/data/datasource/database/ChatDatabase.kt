package com.example.dymessagelite.data.datasource.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.dymessagelite.common.util.JsonUtils
import com.example.dymessagelite.data.datasource.dao.ChatDao
import com.example.dymessagelite.data.datasource.dao.MegDao
import com.example.dymessagelite.data.model.ChatEntity
import com.example.dymessagelite.data.model.MegEntity
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Database(entities = [ChatEntity::class, MegEntity::class], version = 1)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun megDao(): MegDao

    companion object {
        const val DATABASE_NAME = "chat_database"
        private var INSTANCE: ChatDatabase? = null;

        fun getDatabase(
            context: Context
        ): ChatDatabase {
            return INSTANCE ?: synchronized(this) {
                val scope = CoroutineScope(Dispatchers.IO)
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChatDatabase::class.java,
                    DATABASE_NAME
                )
                .addCallback(DatabaseCallback(context,scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val context: Context,
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
           scope.launch {
               val database = getDatabase(context)

               val megDao = database.megDao();
               val chatDao = database.chatDao();

               preFullMeg(megDao)
               preFullChat(chatDao)
           }
        }

        private suspend fun preFullMeg(megDao: MegDao) {
            val megList = JsonUtils.loadJsonData(
                context,
                "megData.json",
                object : TypeToken<List<MegEntity>>() {}
            )

            megList.forEach { megDao.insertOrUpdateMeg(it) }
        }

        private suspend fun preFullChat(chatDao: ChatDao) {
            val chatList = JsonUtils.loadJsonData(
                context,
                "chatData.json",
                object : TypeToken<List<ChatEntity>>() {}
            )
            chatList.forEach { chatDao.insertChat(it) }
        }
    }

}