package com.example.dymessagelite.data.datasource.json

import android.content.Context
import com.example.dymessagelite.data.model.MegItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

class MegLocalDataSource(val context: Context) {
    fun loadAllMessage(): List<MegItem>{
        return try {
            context.assets.open("data.json").use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    val megListType = object : TypeToken<List<MegItem>>() {}.type
                    Gson().fromJson(reader, megListType)
                }
            }
        }catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}