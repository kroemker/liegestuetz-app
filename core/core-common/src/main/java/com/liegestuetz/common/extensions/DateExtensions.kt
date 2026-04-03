package com.liegestuetz.common.extensions

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime

/** Returns the current local date in the device's system time zone. */
fun LocalDate.Companion.today(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDate =
    Clock.System.now().toLocalDateTime(timeZone).date

/**
 * Returns how many days have elapsed since [startDate] up to and including this date.
 * Day 0 = startDate itself (the first day of a challenge).
 * Returns a negative value if this date is before [startDate].
 */
fun LocalDate.dayIndexSince(startDate: LocalDate): Int = startDate.daysUntil(this)

/**
 * Formats the date as a human-readable string, e.g. "3 April 2026".
 */
fun LocalDate.toDisplayString(): String =
    "$dayOfMonth ${month.name.lowercase().replaceFirstChar { it.uppercase() }} $year"

/**
 * Formats as ISO-8601 date string "YYYY-MM-DD", used as the Firestore completion document key.
 */
fun LocalDate.toIsoString(): String =
    "$year-${monthNumber.toString().padStart(2, '0')}-${dayOfMonth.toString().padStart(2, '0')}"
