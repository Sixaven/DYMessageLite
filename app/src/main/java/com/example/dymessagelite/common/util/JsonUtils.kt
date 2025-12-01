package com.example.dymessagelite.common.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

object JsonUtils {
    fun <T>loadJsonData(context: Context, fileName: String,
                        type: TypeToken<List<T>>): List<T>{
        return try {
            val jsonString = context.assets.open(fileName)
                .bufferedReader()
                .use{ it.readText()}
            Gson().fromJson(jsonString, type.type)
        }catch (e: IOException){
            e.printStackTrace()
            emptyList()
        }
    }
}