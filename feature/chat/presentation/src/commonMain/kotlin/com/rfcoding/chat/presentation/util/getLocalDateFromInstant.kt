package com.rfcoding.chat.presentation.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

fun getLocalDateFromInstant(value: Instant): LocalDate {
    val dateTime = value.toLocalDateTime(TimeZone.currentSystemDefault())
    return LocalDate(
        dateTime.year,
        dateTime.month,
        dateTime.day
    )
}