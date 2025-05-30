package com.sloy.sevibus.data.database

import androidx.room.TypeConverter
import com.sloy.sevibus.data.api.model.PositionDto
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.LocalTime

object LocalDateTimeConverter {
    @TypeConverter
    fun serializeDate(dateString: String): LocalDate = LocalDate.parse(dateString)

    @TypeConverter
    fun deserializeDate(date: LocalDate): String = date.toString()

    @TypeConverter
    fun serializeTime(dateString: String): LocalTime = LocalTime.parse(dateString)

    @TypeConverter
    fun deserializeTime(time: LocalTime): String = time.toString()
}

object StringListConverter {
    @TypeConverter
    fun serialize(list: List<String>): String = list.joinToString(",")

    @TypeConverter
    fun deserialize(serialized: String): List<String> = serialized.split(",")
}

object IntListConverter {
    @TypeConverter
    fun serialize(list: List<Int>): String = list.joinToString(",")

    @TypeConverter
    fun deserialize(serialized: String): List<Int> = serialized.split(",").map { it.toInt() }
}

object PositionListConverter {
    @TypeConverter
    fun serialize(list: List<PositionDto>): String = Json.encodeToString(list)

    @TypeConverter
    fun deserialize(serialized: String): List<PositionDto> = Json.decodeFromString(serialized)
}