package com.liegestuetz.challenge.presentation.detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liegestuetz.common.extensions.today
import com.liegestuetz.domain.model.ChallengeStatus
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.until

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSettings: (String) -> Unit,
    viewModel: ChallengeDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboard = LocalClipboardManager.current

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.challenge?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Copy invite code
                    uiState.challenge?.inviteCode?.let { code ->
                        IconButton(onClick = {
                            clipboard.setText(AnnotatedString(code))
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy invite code")
                        }
                    }
                    // Settings (creator only)
                    if (uiState.challenge?.creatorId == uiState.currentUserId) {
                        IconButton(onClick = { uiState.challenge?.id?.let(onNavigateToSettings) }) {
                            Icon(Icons.Default.Settings, "Settings")
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            uiState.challenge == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Challenge not found")
            }
            else -> ChallengeDetailContent(
                uiState = uiState,
                onMarkComplete = viewModel::markComplete,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun ChallengeDetailContent(
    uiState: ChallengeDetailUiState,
    onMarkComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val challenge = uiState.challenge!!
    val isActive = challenge.status == ChallengeStatus.ACTIVE

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        // ── Hero: rep count ──────────────────────────────────────────────
        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Day ${uiState.dayIndex + 1} of ${challenge.durationDays}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                if (uiState.todayGoal != null) {
                    Text(
                        text = "${uiState.todayGoal}",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "push-ups today",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text("Challenge finished!", style = MaterialTheme.typography.headlineMedium)
                }
                Spacer(Modifier.height(24.dp))

                // ── Mark complete button ──────────────────────────────────
                val buttonColor by animateColorAsState(
                    targetValue = if (uiState.completedToday) MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.primary,
                    label = "complete_button_color",
                )
                AnimatedContent(targetState = uiState.completedToday, label = "complete_button") { done ->
                    Button(
                        onClick = onMarkComplete,
                        enabled = isActive && !done && !uiState.isMarkingComplete && uiState.todayGoal != null,
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                    ) {
                        if (uiState.isMarkingComplete) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary)
                        } else if (done) {
                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.padding(end = 8.dp))
                            Text("Completed today!", fontWeight = FontWeight.SemiBold)
                        } else {
                            Text("Mark as Complete", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Invite code hint
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Invite code: ${challenge.inviteCode}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }

        // ── Completion history (7-day heatmap) ───────────────────────────
        item {
            if (uiState.completionHistory.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                CompletionHeatmap(
                    completions = uiState.completionHistory,
                    challengeStartDate = challenge.startDate,
                    durationDays = challenge.durationDays,
                    modifier = Modifier.padding(16.dp),
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }

        // ── Participants leaderboard ──────────────────────────────────────
        item {
            Text(
                text = "Participants",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }

        items(uiState.participants, key = { it.participant.userId }) { row ->
            ParticipantListItem(row = row, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        }
    }
}

@Composable
private fun ParticipantListItem(row: ParticipantRow, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Avatar placeholder
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = row.participant.displayName.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(row.participant.displayName, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "🔥 ${row.participant.currentStreak} day streak · ${row.participant.totalCompletions} total",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Icon(
            imageVector = if (row.completedToday) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = if (row.completedToday) "Done" else "Not done",
            tint = if (row.completedToday) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun CompletionHeatmap(
    completions: List<com.liegestuetz.domain.model.Completion>,
    challengeStartDate: kotlinx.datetime.LocalDate,
    durationDays: Int,
    modifier: Modifier = Modifier,
) {
    val completedDates = completions.map { it.date }.toSet()
    val today = today()
    val endDay = minOf(durationDays - 1, challengeStartDate.until(today, DateTimeUnit.DAY))
    val startDay = maxOf(0, endDay - 13)
    val days = (startDay..endDay).map { i ->
        challengeStartDate.plus(i, DateTimeUnit.DAY)
    }

    Column(modifier = modifier) {
        Text("Your last ${days.size} days", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            days.forEach { date ->
                val done = date in completedDates
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(
                            if (done) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                )
            }
        }
    }
}
