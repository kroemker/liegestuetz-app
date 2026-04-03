package com.liegestuetz.home.presentation

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToChallenge: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToJoin: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showFabSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Liegestuetz", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showFabSheet = true },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Challenge") },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                uiState.challenges.isEmpty() -> EmptyState(
                    onCreateClick = onNavigateToCreate,
                    onJoinClick = onNavigateToJoin,
                    modifier = Modifier.align(Alignment.Center),
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(uiState.challenges, key = { it.challenge.id }) { card ->
                        ChallengeCard(card = card, onClick = { onNavigateToChallenge(card.challenge.id) })
                    }
                }
            }
        }
    }

    if (showFabSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFabSheet = false },
            sheetState = sheetState,
        ) {
            Column(
                modifier = Modifier.padding(16.dp).padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("New Challenge", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                TextButton(
                    onClick = { showFabSheet = false; onNavigateToCreate() },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.FitnessCenter, null, modifier = Modifier.padding(end = 8.dp))
                    Text("Create a challenge")
                }
                TextButton(
                    onClick = { showFabSheet = false; onNavigateToJoin() },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.GroupAdd, null, modifier = Modifier.padding(end = 8.dp))
                    Text("Join with invite code")
                }
            }
        }
    }
}

@Composable
private fun ChallengeCard(card: ChallengeCardState, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = card.challenge.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (card.todayGoal != null) "Today: ${card.todayGoal} reps · Day ${card.dayIndex + 1} of ${card.challenge.durationDays}"
                    else "Not started yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${card.challenge.participantIds.size} participant${if (card.challenge.participantIds.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (card.completedToday) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Completed today",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
    }
}

@Composable
private fun EmptyState(onCreateClick: () -> Unit, onJoinClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            Icons.Default.FitnessCenter,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(16.dp))
        Text("No challenges yet", style = MaterialTheme.typography.titleMedium)
        Text(
            "Create one or join your friends",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
        TextButton(onClick = onCreateClick) { Text("Create a challenge") }
        TextButton(onClick = onJoinClick) { Text("Join with invite code") }
    }
}
