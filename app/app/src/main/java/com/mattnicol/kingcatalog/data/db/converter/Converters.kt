package com.mattnicol.kingcatalog.data.db.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mattnicol.kingcatalog.data.model.Adaptation
import com.mattnicol.kingcatalog.data.model.Connection

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: String?): List<String> =
        if (value.isNullOrBlank()) emptyList()
        else gson.fromJson(value, object : TypeToken<List<String>>() {}.type)

    @TypeConverter
    fun toStringList(list: List<String>?): String =
        gson.toJson(list ?: emptyList<String>())

    @TypeConverter
    fun fromIntList(value: String?): List<Int> =
        if (value.isNullOrBlank()) emptyList()
        else gson.fromJson(value, object : TypeToken<List<Int>>() {}.type)

    @TypeConverter
    fun toIntList(list: List<Int>?): String =
        gson.toJson(list ?: emptyList<Int>())

    @TypeConverter
    fun fromAdaptationList(value: String?): List<Adaptation> =
        if (value.isNullOrBlank()) emptyList()
        else gson.fromJson(value, object : TypeToken<List<Adaptation>>() {}.type)

    @TypeConverter
    fun toAdaptationList(list: List<Adaptation>?): String =
        gson.toJson(list ?: emptyList<Adaptation>())

    @TypeConverter
    fun fromConnectionList(value: String?): List<Connection> =
        if (value.isNullOrBlank()) emptyList()
        else gson.fromJson(value, object : TypeToken<List<Connection>>() {}.type)

    @TypeConverter
    fun toConnectionList(list: List<Connection>?): String =
        gson.toJson(list ?: emptyList<Connection>())
}
