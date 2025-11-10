package com.rfcoding.chat.database.converter

import androidx.room.TypeConverter
import com.rfcoding.chat.database.model.ChatMessageEventSerializable
import kotlinx.serialization.json.Json
import kotlin.time.Instant

class Converters {

    @TypeConverter
    fun imageUrlsToString(value: List<String>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun imageUrlsFromString(value: String): List<String> {
        return Json.decodeFromString(value)
    }

    @TypeConverter
    fun eventToString(value: ChatMessageEventSerializable?): String? {
        if (value == null) {
            return null
        }

        return Json.encodeToString(value)
    }

    @TypeConverter
    fun eventFromString(value: String?): ChatMessageEventSerializable? {
        if (value == null) {
            return null
        }

        return Json.decodeFromString(value)
    }

    @TypeConverter
    fun instantToEpochMillis(value: Instant): Long {
        return value.toEpochMilliseconds()
    }

    @TypeConverter
    fun instantFromEpochMillis(value: Long): Instant {
        return Instant.fromEpochMilliseconds(value)
    }
}