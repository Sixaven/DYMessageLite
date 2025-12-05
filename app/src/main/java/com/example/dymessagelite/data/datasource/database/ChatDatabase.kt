package com.example.dymessagelite.data.datasource.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.log
import kotlin.random.Random

@Database(entities = [ChatEntity::class, MegEntity::class], version = 2)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun megDao(): MegDao

    companion object {
        const val DATABASE_NAME = "chat_database"

        @Volatile
        private var INSTANCE: ChatDatabase? = null;
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        private val _isDatabaseCreated = MutableStateFlow(false)
        val isDatabaseCreated = _isDatabaseCreated.asStateFlow()

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE ChatEntity ADD COLUMN type INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE MegEntity ADD COLUMN type INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(
            context: Context
        ): ChatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = buildDatabase(context)
                INSTANCE = instance
                instance
            }
        }

        private fun buildDatabase(context: Context): ChatDatabase {
            val dbFile = context.getDatabasePath(DATABASE_NAME)
            _isDatabaseCreated.value = dbFile.exists()
            val instance = Room.databaseBuilder(
                context,
                ChatDatabase::class.java,
                DATABASE_NAME
            )
                .addCallback(DatabaseCallback(context))
                .addMigrations(MIGRATION_1_2)
                .build()
            return instance;
        }
    }

    private class DatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            INSTANCE?.let { database ->
                scope.launch {
                    val megDao = database.megDao()
                    val chatDao = database.chatDao()
//                添加消息列表信息
                    preFullMeg(megDao)
//                添加消息详情信息
                    preFullChat(chatDao)
                    _isDatabaseCreated.value = true
//                通知仓库数据库初始化完毕
                    Log.e("[databaseTest-init]", System.currentTimeMillis().toString())
                }
            }
        }

        private suspend fun preFullMeg(megDao: MegDao) {
            val megList = JsonUtils.loadJsonData(
                context,
                "megData.json",
                object : TypeToken<List<MegEntity>>() {}
            )
            val megListWithTimestamp = setDescendingTimestamps(megList) { item, timestamp ->
                item.copy(timestamp = timestamp)
            }
            megListWithTimestamp.forEach { megDao.insertOrUpdateMeg(it) }
        }

        private suspend fun preFullChat(chatDao: ChatDao) {
            val chatList = JsonUtils.loadJsonData(
                context,
                "chatData.json",
                object : TypeToken<List<ChatEntity>>() {}
            )
            val chatListWithTimestamp = setDescendingTimestamps(chatList) { item, timestamp ->
                item.copy(timestamp = timestamp)
            }
            chatListWithTimestamp.forEach { chatDao.insertChat(it) }
        }

        private fun <T> setDescendingTimestamps(
            originalList: List<T>,
            copyWithTimestamp: (item: T, timestamp: Long) -> T
        ): List<T> {
            var currentTime = System.currentTimeMillis()

            return originalList.map { originalItem ->
                // 调用传入的 lambda 来创建一个带新时间戳的副本
                val itemWithTimestamp = copyWithTimestamp(originalItem, currentTime)

                // 为下一个 item 准备一个更早的时间戳 (随机减少5到60分钟)
                val randomMinutes = Random.nextInt(5, 60)
                currentTime -= randomMinutes * 60 * 1000L

                itemWithTimestamp
            }
        }
    }
}