package com.example.soulscript.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.soulscript.ui.viewmodels.MonthlyRewindViewModel
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import kotlinx.coroutines.delay

val moodOptionsForDisplay = listOf(
    Mood("Happy", Icons.Default.SentimentVerySatisfied, listOf(Color(0xFFF6D365), Color(0xFFFDA085))),
    Mood("Calm", Icons.Default.Spa, listOf(Color(0xFFA1C4FD), Color(0xFFC2E9FB))),
    Mood("Focused", Icons.Default.Lightbulb, listOf(Color(0xFF84FAB0), Color(0xFF8FD3F4))),
    Mood("Sad", Icons.Default.SentimentDissatisfied, listOf(Color(0xFFA3B7F2), Color(0xFFD5C3FB))),
    Mood("Stressed", Icons.Default.FlashOn, listOf(Color(0xFFFF9A9E), Color(0xFFFAD0C4))),
    Mood("Tired", Icons.Default.Nightlight, listOf(Color(0xFFB3B9FF), Color(0xFFA1C4FD))),
    Mood("Creative", Icons.Default.Palette, listOf(Color(0xFFFFC3A0), Color(0xFFFFAFBD)))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyRewindScreen(
    onNavigateBack: () -> Unit,
    viewModel: MonthlyRewindViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val dominantMoodGradient = moodOptionsForDisplay.find { it.label == uiState.dominantMood }?.gradient
        ?: listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface)

    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        startAnimation = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monthly Stats") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = dominantMoodGradient.first().copy(alpha = 0.1f)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(targetState = startAnimation, index = 0) {
                Text(
                    text = "Your Rewind for ${uiState.monthName}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedContent(targetState = startAnimation, index = 1) {
                StatItem(
                    icon = moodOptionsForDisplay.find { it.label == uiState.dominantMood }?.icon ?: Icons.Default.SentimentVerySatisfied,
                    label = "Your dominant mood was",
                    value = uiState.dominantMood ?: "N/A"
                )
            }

            Divider(modifier = Modifier.padding(vertical = 24.dp))

            AnimatedContent(targetState = startAnimation, index = 2) {
                StatItem(
                    icon = Icons.Default.Edit,
                    label = "You wrote a total of",
                    value = "${uiState.totalEntries} entries"
                )
            }

            Divider(modifier = Modifier.padding(vertical = 24.dp))

            AnimatedContent(targetState = startAnimation, index = 3) {
                SummarySection(title = "Your Word Cloud") {
                    WordCloud(wordCloudData = uiState.wordCloudData)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedContent(targetState = startAnimation, index = 4) {
                SummarySection(title = "Your Insight for this month") {
                    InsightCard(suggestion = uiState.suggestion)
                }
            }
        }
    }
}

@Composable
fun AnimatedContent(targetState: Boolean, index: Int, content: @Composable () -> Unit) {
    var animationFinished by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (animationFinished) 1f else 0f,
        animationSpec = tween(durationMillis = 500), label = ""
    )
    val offsetY by animateFloatAsState(
        targetValue = if (animationFinished) 0f else 20f,
        animationSpec = tween(durationMillis = 500), label = ""
    )

    LaunchedEffect(targetState) {
        delay(index * 150L)
        animationFinished = true
    }

    Box(
        modifier = Modifier
            .alpha(alpha)
            .offset(y = offsetY.dp)
    ) {
        content()
    }
}

@Composable
fun StatItem(icon: ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SummarySection(title: String, content: @Composable () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        content()
    }
}

@Composable
fun WordCloud(wordCloudData: Map<String, Int>) {
    Text(
        text = "(Mostly used words for this month)",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(16.dp))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 1.dp
    ) {
        if (wordCloudData.isEmpty()) {
            Text(
                "Not enough data to generate a word cloud yet.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            FlowRow(
                modifier = Modifier.padding(16.dp),
                mainAxisSpacing = 10.dp,
                crossAxisSpacing = 10.dp,
                mainAxisAlignment = MainAxisAlignment.Center
            ) {
                wordCloudData.entries
                    .sortedByDescending { it.value }
                    .take(20)
                    .forEach { (word, count) ->
                        Text(
                            text = word,
                            fontSize = (10 + count * 2).coerceAtMost(32).sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
            }
        }
    }
}

@Composable
fun InsightCard(suggestion: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "Insight",
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(32.dp)
            )
            Text(
                text = suggestion,
                textAlign = TextAlign.Justify,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}
