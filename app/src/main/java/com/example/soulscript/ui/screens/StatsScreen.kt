package com.example.soulscript.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.soulscript.ui.viewmodels.MoodStat
import com.example.soulscript.ui.viewmodels.StatsViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.statsUiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Your Activity", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatCard(
                        title = "Current Streak",
                        value = "${uiState.streak} days",
                        icon = { Icon(Icons.Default.LocalFireDepartment, contentDescription = "Streak", tint = Color(0xFFFFA000), modifier = Modifier.size(32.dp)) },
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Top Mood",
                        value = uiState.mostFrequentMood ?: "N/A",
                        icon = {
                            val moodIcon = moodOptions.find { it.label == uiState.mostFrequentMood }?.icon ?: Icons.Default.SentimentVerySatisfied
                            Icon(moodIcon, contentDescription = "Mood", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(32.dp))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            val months = (5 downTo 0).map {
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.MONTH, -it)
                calendar
            }

            item {
                Text(
                    "Last 6 Months",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                val lazyListState = rememberLazyListState()
                val coroutineScope = rememberCoroutineScope()
                LaunchedEffect(Unit) {
                    coroutineScope.launch {
                        lazyListState.animateScrollToItem(months.size - 1)
                    }
                }
                LazyRow(
                    state = lazyListState,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(months.size) { index ->
                        MonthSection(calendar = months[index], heatmapData = uiState.heatmapData)
                    }
                }
            }

            if (uiState.moodDistribution.isNotEmpty()) {
                item {
                    MoodDistributionChart(moodStats = uiState.moodDistribution)
                }
            }

            if (uiState.monthlyEntries.isNotEmpty()) {
                item {
                    MonthlyEntriesChart(monthlyStats = uiState.monthlyEntries)
                }
            }
        }
    }
}

@Composable
fun MoodDistributionChart(moodStats: List<MoodStat>) {
    Card(
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Mood Distribution",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PieChart(moodStats = moodStats, modifier = Modifier.size(150.dp))
                Spacer(modifier = Modifier.width(16.dp))
                ChartLegend(moodStats = moodStats, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun PieChart(moodStats: List<MoodStat>, modifier: Modifier = Modifier) {
    var startAngle = 0f
    Canvas(modifier = modifier) {
        val total = moodStats.sumOf { it.count }.toFloat()
        if (total > 0) {
            moodStats.forEach { moodStat ->
                val sweepAngle = (moodStat.count / total) * 360f
                drawArc(
                    color = moodStat.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle - 1f,
                    useCenter = true,
                    size = Size(size.width, size.height)
                )
                startAngle += sweepAngle
            }
        }
    }
}

@Composable
fun ChartLegend(moodStats: List<MoodStat>, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        moodStats.forEach { moodStat ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(moodStat.color, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${moodStat.mood} (${"%.1f".format(moodStat.percentage)}%)",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun MonthlyEntriesChart(monthlyStats: List<Pair<String, Int>>) {
    val maxEntries = monthlyStats.maxOfOrNull { it.second } ?: 0
    Card(
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Monthly Entries",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                monthlyStats.forEach { (month, count) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = count.toString(), style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        val barHeight = if (maxEntries > 0) (count.toFloat() / maxEntries.toFloat()) else 0f
                        Box(
                            modifier = Modifier
                                .fillMaxHeight(barHeight * 0.8f)
                                .width(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = month, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: @Composable () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            icon()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MonthSection(calendar: Calendar, heatmapData: Map<Long, Int>) {
    val monthName = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(calendar.time)
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfMonth = (calendar.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
    val startDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK)
    val emptyStartCells = startDayOfWeek - 1
    val today = Calendar.getInstance()

    val monthCells = (1..(emptyStartCells + daysInMonth)).map {
        if (it <= emptyStartCells) null else it - emptyStartCells
    }.chunked(7)

    Card(
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .width(200.dp)
            .height(230.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
            Text(
                text = monthName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                    Text(text = day, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                monthCells.forEach { week ->
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        week.forEach { day ->
                            var cellModifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)

                            if (day != null) {
                                val cellCalendar = (calendar.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, day) }
                                val dayTimestamp = cellCalendar.timeInMillis / (1000 * 60 * 60 * 24)
                                val entryCount = heatmapData[dayTimestamp] ?: 0

                                val color = when (entryCount) {
                                    0 -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                    1 -> Color(0xFF69F0AE).copy(alpha = 0.6f)
                                    2 -> Color(0xFF00E676).copy(alpha = 0.8f)
                                    else -> Color(0xFF00C853)
                                }

                                val isToday = cellCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                                        cellCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)

                                cellModifier = cellModifier
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(color)

                                if (isToday) {
                                    cellModifier = cellModifier.border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(2.dp)
                                    )
                                }
                            }
                            Box(modifier = cellModifier)
                        }
                        if (week.size < 7) {
                            for (i in 0 until (7 - week.size)) {
                                Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}
