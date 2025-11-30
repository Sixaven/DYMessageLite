package com.example.dymessagelite.data.datasource.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.dymessagelite.data.datasource.dao.ChatDao
import com.example.dymessagelite.data.model.ChatEntity

@Database( entities = [ChatEntity::class], version = 1)
abstract class ChatDatabase: RoomDatabase() {
    abstract fun chatDao(): ChatDao

    companion object{
        const val DATABASE_NAME = "chat_database"
        private var INSTANCE: ChatDatabase? = null;

        fun getDatabase(context: Context): ChatDatabase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChatDatabase::class.java,
                    DATABASE_NAME
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }

}