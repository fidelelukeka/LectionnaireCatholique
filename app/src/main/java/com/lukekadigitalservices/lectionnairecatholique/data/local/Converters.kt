package com.lukekadigitalservices.lectionnairecatholique.data.local

import com.google.gson.Gson
import com.lukekadigitalservices.lectionnairecatholique.data.MessesResponse

class Converters {
    private val gson = Gson()

    @androidx.room.TypeConverter
    fun fromMessesResponse(value: MessesResponse): String = gson.toJson(value)

    @androidx.room.TypeConverter
    fun toMessesResponse(value: String): MessesResponse =
        gson.fromJson(value, MessesResponse::class.java)
}