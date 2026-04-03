package com.liegestuetz.domain

import com.liegestuetz.domain.model.Challenge
import com.liegestuetz.domain.model.ChallengeStatus
import com.liegestuetz.domain.usecase.GetTodayGoalUseCase
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class GetTodayGoalUseCaseTest {

    private val useCase = GetTodayGoalUseCase()

    private val baseChallenge = Challenge(
        id = "test",
        name = "Test",
        description = null,
        creatorId = "user1",
        startDate = LocalDate(2026, 4, 1),
        durationDays = 30,
        startingReps = 10,
        dailyIncrement = 5,
        inviteCode = "ABC123",
        participantIds = listOf("user1"),
        status = ChallengeStatus.ACTIVE,
        createdAt = Instant.fromEpochSeconds(0),
    )

    @Test
    fun `day 0 returns startingReps`() {
        val goal = useCase(baseChallenge, LocalDate(2026, 4, 1))
        assertEquals(10, goal)
    }

    @Test
    fun `day 1 adds one increment`() {
        val goal = useCase(baseChallenge, LocalDate(2026, 4, 2))
        assertEquals(15, goal)
    }

    @Test
    fun `day 6 returns correct compounded goal`() {
        val goal = useCase(baseChallenge, LocalDate(2026, 4, 7))
        assertEquals(40, goal)
    }

    @Test
    fun `flat challenge has same goal every day`() {
        val flat = baseChallenge.copy(dailyIncrement = 0)
        assertEquals(10, useCase(flat, LocalDate(2026, 4, 1)))
        assertEquals(10, useCase(flat, LocalDate(2026, 4, 15)))
        assertEquals(10, useCase(flat, LocalDate(2026, 4, 30)))
    }

    @Test
    fun `date before startDate returns null`() {
        val goal = useCase(baseChallenge, LocalDate(2026, 3, 31))
        assertNull(goal)
    }

    @Test
    fun `date beyond durationDays returns null`() {
        // Day 30 is out of range for a 30-day challenge (valid indices: 0–29)
        val goal = useCase(baseChallenge, LocalDate(2026, 5, 1))
        assertNull(goal)
    }
}
