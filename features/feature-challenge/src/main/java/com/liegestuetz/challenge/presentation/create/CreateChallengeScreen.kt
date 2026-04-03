package com.liegestuetz.challenge.presentation.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChallengeScreen(
    onNavigateBack: () -> Unit,
    onChallengeCreated: (String) -> Unit,
    viewModel: CreateChallengeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.navigateToChallengeId.collectLatest { onChallengeCreated(it) }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(CreateChallengeEvent.ErrorDismissed)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Challenge") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.onEvent(CreateChallengeEvent.NameChanged(it)) },
                label = { Text("Challenge name *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.onEvent(CreateChallengeEvent.DescriptionChanged(it)) },
                label = { Text("Description (optional)") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = uiState.startingReps,
                    onValueChange = { viewModel.onEvent(CreateChallengeEvent.StartingRepsChanged(it)) },
                    label = { Text("Starting reps") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = uiState.dailyIncrement,
                    onValueChange = { viewModel.onEvent(CreateChallengeEvent.DailyIncrementChanged(it)) },
                    label = { Text("Daily +reps") },
                    supportingText = { Text("0 = flat") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }

            OutlinedTextField(
                value = uiState.durationDays,
                onValueChange = { viewModel.onEvent(CreateChallengeEvent.DurationDaysChanged(it)) },
                label = { Text("Duration (days)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            // Goal preview card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Goal preview", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    GoalPreviewRow("Day 1", uiState.previewDay1)
                    GoalPreviewRow("Day 7", uiState.previewDay7)
                    GoalPreviewRow("Day 14", uiState.previewDay14)
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { viewModel.onEvent(CreateChallengeEvent.Submit) },
                enabled = !uiState.isLoading && uiState.name.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(50.dp),
            ) {
                if (uiState.isLoading) CircularProgressIndicator(strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                else Text("Create Challenge")
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun GoalPreviewRow(label: String, reps: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text("$reps reps", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
