package com.rfcoding.chat.presentation.util

import chirp.feature.chat.presentation.generated.resources.Res
import chirp.feature.chat.presentation.generated.resources.today
import chirp.feature.chat.presentation.generated.resources.today_x
import chirp.feature.chat.presentation.generated.resources.yesterday
import chirp.feature.chat.presentation.generated.resources.yesterday_x
import com.rfcoding.core.presentation.util.UiText
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

object DateUtils {

    fun formatDateSeparator(date: LocalDate, clock: Clock = Clock.System): UiText {
        val timeZone = TimeZone.currentSystemDefault()
        val todayDate = clock.now().toLocalDateTime(timeZone).date
        val yesterdayDate = todayDate.minus(1, DateTimeUnit.DAY)
        val isSameYear = todayDate.year == date.year

        val formattedMonthDay = date.format(
            LocalDate.Format {
                monthName(
                    names = MonthNames(
                        january = "January",
                        february = "February",
                        march = "March",
                        april = "April",
                        may = "May",
                        june = "June",
                        july = "July",
                        august = "August",
                        september = "September",
                        october = "October",
                        november = "November",
                        december = "December"
                    )
                )
                char(' ')
                day()
            }
        )
        val formattedWithYear = date.format(
            LocalDate.Format {
                chars(formattedMonthDay)
                chars(", ")
                year()
            }
        )

        return when {
            todayDate == date -> UiText.Resource(Res.string.today)
            yesterdayDate == date -> UiText.Resource(Res.string.yesterday)
            isSameYear -> UiText.DynamicText(formattedMonthDay)
            else -> UiText.DynamicText(formattedWithYear)
        }
    }

    fun formatMessageTime(instant: Instant, clock: Clock = Clock.System): UiText {
        val timeZone = TimeZone.currentSystemDefault()
        val messageDateTime = instant.toLocalDateTime(timeZone)
        val todayDate = clock.now().toLocalDateTime(timeZone).date
        val yesterdayDate = todayDate.minus(1, DateTimeUnit.DAY)

        val formattedTime = messageDateTime.format(
            LocalDateTime.Format {
                amPmHour()
                char(':')
                minute()
                char(' ')
                amPmMarker("AM", "PM")
            }
        )
        val formattedDayOfWeekWithTime = messageDateTime.format(
            LocalDateTime.Format {
                dayOfWeek(
                    DayOfWeekNames(
                        monday = "Monday",
                        tuesday = "Tuesday",
                        wednesday = "Wednesday",
                        thursday = "Thursday",
                        friday = "Friday",
                        saturday = "Saturday",
                        sunday = "Sunday"
                    )
                )
                chars(", ")
                chars(formattedTime)
            }
        )
        val formattedDateTime = messageDateTime.format(
            LocalDateTime.Format {
                monthNumber()
                char('/')
                day()
                char('/')
                year()
                char(' ')
                chars(formattedTime)
            }
        )

        return when {
            messageDateTime.date == todayDate -> UiText.Resource(
                Res.string.today_x,
                arrayOf(formattedTime)
            )
            messageDateTime.date == yesterdayDate -> UiText.Resource(
                Res.string.yesterday_x,
                arrayOf(formattedTime)
            )
            isWithinThisWeek(messageDateTime) -> UiText.DynamicText(formattedDayOfWeekWithTime)
            else -> UiText.DynamicText(formattedDateTime)
        }
    }

    private val dayMap = mapOf(
        DayOfWeek.MONDAY to 0,
        DayOfWeek.TUESDAY to 1,
        DayOfWeek.WEDNESDAY to 2,
        DayOfWeek.THURSDAY to 3,
        DayOfWeek.FRIDAY to 4,
        DayOfWeek.SATURDAY to 5,
        DayOfWeek.SUNDAY to 6,
    )

    private fun isWithinThisWeek(dateTime: LocalDateTime): Boolean {
        val numberOfDaysToGetMonday = dayMap[dateTime.dayOfWeek]!!
        val mondayDate = dateTime.date.minus(numberOfDaysToGetMonday, DateTimeUnit.DAY)
        val sundayDate = mondayDate.plus(6, DateTimeUnit.DAY)

        val weekRange = mondayDate..sundayDate

        return dateTime.date in weekRange
    }
}