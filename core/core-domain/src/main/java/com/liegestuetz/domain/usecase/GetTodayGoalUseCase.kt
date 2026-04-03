package com.liegestuetz.domain.usecase

import com.liegestuetz.domain.model.Challenge
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

/**
 * Pure function — no I/O, no dependencies.
 *
 * Returns the rep goal for [challenge] on [date], or null if [date] is before
 * the challenge's start date or the challenge is not active.
 *
 * Example:
 *   startingReps=10, dailyIncrement=5
 *   Day 0 (startDate): 10
 *   Day 1:             15
 *   Day 6:             40
 */
class GetTodayGoalUseCase {

    operator fun invoke(challenge: Challenge, date: LocalDate): Int? {
        val dayIndex = challenge.startDate.daysUntil(date)
        if (dayIndex < 0) return null
        if (dayIndex >= challenge.durationDays) return null
        return challenge.goalForDayIndex(dayIndex)
    }
}
