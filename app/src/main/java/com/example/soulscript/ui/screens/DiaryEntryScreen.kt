package com.example.soulscript.screens

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.soulscript.ui.theme.Poppins
import com.example.soulscript.ui.theme.SoulScriptTheme
import com.example.soulscript.ui.theme.handwritingStyle
import com.example.soulscript.ui.viewmodels.DiaryEntryViewModel
import java.text.SimpleDateFormat
import java.util.*

data class Mood(
    val label: String,
    val icon: ImageVector,
    val gradient: List<Color>
)

val moodOptions = listOf(
    Mood("Happy", Icons.Default.SentimentVerySatisfied, listOf( Color(0xFFCB5D40), Color(0xFFF5C42A))),
    Mood("Calm", Icons.Default.Spa, listOf(Color(0xFF3A7ACE), Color(0xFF89F7FE))),
    Mood("Focused", Icons.Default.Lightbulb, listOf(Color(0xFF485563), Color(0xFF29323C))),
    Mood("Sad", Icons.Default.SentimentDissatisfied, listOf(Color(0xFF2C3E50), Color(0xFF4CA1AF))),
    Mood("Stressed", Icons.Default.FlashOn, listOf(Color(0xFFFC4568),Color(0xFFB43A91))),
    Mood("Tired", Icons.Default.Nightlight, listOf(Color(0xFF232526), Color(0xFF484D50))),
    Mood("Creative", Icons.Default.Palette, listOf(Color(0xFF831ED9), Color(0xFF4A00E0)))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryEntryScreen(
    onNavigateBack: () -> Unit,
    viewModel: DiaryEntryViewModel,
    onSaveNote: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf(moodOptions.first()) }

    val context = LocalContext.current
    val fixedForegroundColor = Color.White.copy(alpha = 0.9f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(selectedMood.gradient))
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    modifier = Modifier.padding(4.dp),
                    title = { Text("New Entry", color = fixedForegroundColor, fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = fixedForegroundColor
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                if(title.isEmpty()){
                                    Toast.makeText(context, "Please add a title", Toast.LENGTH_SHORT).show()
                                } else if(content.isEmpty()){
                                    Toast.makeText(context, "Please add the content", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.saveEntry(title, content, selectedMood.label)
                                    onSaveNote()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save",
                                tint = fixedForegroundColor
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                MoodSelector(
                    moods = moodOptions,
                    selectedMood = selectedMood,
                    onMoodSelected = { selectedMood = it },
                    textColor = fixedForegroundColor
                )

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                        .defaultMinSize(minHeight = 400.dp)
                ) {
                    DiaryPaper(
                        title = title,
                        onTitleChange = { title = it },
                        content = content,
                        onContentChange = { content = it }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun DiaryPaper(
    title: String,
    onTitleChange: (String) -> Unit,
    content: String,
    onContentChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val paperColor = MaterialTheme.colorScheme.surface
    val onPaperColor = MaterialTheme.colorScheme.onSurface
    val placeholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
    val linesColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color.Black.copy(alpha = 0.3f)
            )
            .clip(RoundedCornerShape(12.dp))
            .background(paperColor)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Text(
            text = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date()),
            style = MaterialTheme.typography.labelMedium,
            color = onPaperColor.copy(alpha = 0.6f),
            modifier = Modifier.align(Alignment.End)
        )
        Spacer(modifier = Modifier.height(16.dp))

        BasicTextField(
            value = title,
            onValueChange = onTitleChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.titleLarge.copy(
                color = onPaperColor,
                fontWeight = FontWeight.SemiBold
            ),
            cursorBrush = SolidColor(onPaperColor),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (title.isEmpty()) {
                        Text(
                            "Add a title...",
                            style = MaterialTheme.typography.titleLarge,
                            color = placeholderColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    innerTextField()
                }
            }
        )

        Divider(
            modifier = Modifier.padding(vertical = 16.dp),
            color = linesColor.copy(alpha = 0.3f),
            thickness = 1.dp
        )

        BasicTextField(
            value = content,
            onValueChange = onContentChange,
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val spacingPx = 36.sp.toPx()
                    var y = spacingPx
                    while (y < size.height) {
                        drawLine(
                            color = linesColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.5f
                        )
                        y += spacingPx
                    }
                },
            textStyle = handwritingStyle.copy(
                color = onPaperColor.copy(alpha = 0.9f),
                fontSize = 24.sp,
                lineHeight = 36.sp
            ),
            cursorBrush = SolidColor(onPaperColor),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.TopStart) {
                    if (content.isEmpty()) {
                        Text(
                            "What's in your mind...",
                            style = handwritingStyle.copy(
                                fontSize = 24.sp,
                                lineHeight = 36.sp
                            ),
                            color = placeholderColor
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
fun MoodSelector(
    moods: List<Mood>,
    selectedMood: Mood,
    onMoodSelected: (Mood) -> Unit,
    textColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = "How are you feeling?",
            style = MaterialTheme.typography.labelLarge,
            color = textColor,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(moods) { mood ->
                MoodItem(
                    mood = mood,
                    isSelected = mood == selectedMood,
                    onClick = { onMoodSelected(mood) },
                    textColor = textColor
                )
            }
        }
    }
}

@Composable
fun MoodItem(
    mood: Mood,
    isSelected: Boolean,
    onClick: () -> Unit,
    textColor: Color
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) textColor.copy(alpha = 0.8f) else Color.Transparent,
        label = "MoodItemBorder"
    )

    val labelStyle = remember {
        TextStyle(
            fontFamily = Poppins,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(90.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .background(Color.Black.copy(alpha = 0.2f))
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(vertical = 12.dp, horizontal = 8.dp)

    ) {
        Text(
            text = mood.label,
            style = labelStyle,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(8.dp))
        Icon(
            imageVector = mood.icon,
            contentDescription = mood.label,
            tint = textColor,
            modifier = Modifier.size(28.dp)
        )
    }
}

private fun getContrastingTextColor(backgroundColor: Color): Color {
    val luminance = (0.299 * backgroundColor.red + 0.587 * backgroundColor.green + 0.114 * backgroundColor.blue)
    return if (luminance > 0.5) Color.Black.copy(alpha = 0.8f) else Color.White
}
