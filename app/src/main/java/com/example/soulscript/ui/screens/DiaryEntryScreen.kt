package com.example.soulscript.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.soulscript.ui.theme.SoulScriptTheme
import com.example.soulscript.ui.theme.handwritingStyle
import com.example.soulscript.ui.viewmodels.DiaryEntryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Mood(
    val label: String,
    val icon: ImageVector,
    val gradient: List<Color>
)

val moodOptions = listOf(
    Mood(
        "Happy",
        Icons.Default.SentimentVerySatisfied,
        listOf(Color(0xFFF6D365), Color(0xFFFDA085))
    ),
    Mood("Calm", Icons.Default.Spa, listOf(Color(0xFFA1C4FD), Color(0xFFC2E9FB))),
    Mood("Focused", Icons.Default.Lightbulb, listOf(Color(0xFF84FAB0), Color(0xFF8FD3F4))),
    Mood("Sad", Icons.Default.SentimentDissatisfied, listOf(Color(0xFFA3B7F2), Color(0xFFD5C3FB))),
    Mood("Stressed", Icons.Default.FlashOn, listOf(Color(0xFFFF9A9E), Color(0xFFFAD0C4))),
    Mood("Tired", Icons.Default.Nightlight, listOf(Color(0xFFB3B9FF), Color(0xFFA1C4FD))),
    Mood("Creative", Icons.Default.Palette, listOf(Color(0xFFFFC3A0), Color(0xFFFFAFBD)))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryEntryScreen(
    onNavigateBack: () -> Unit,
    viewModel: DiaryEntryViewModel
){
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf(moodOptions.first()) }

    val fixedTextColor = Color(0xFF0E0D0D)

    val shadowColor = Color(0x40000000)

    SoulScriptTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(selectedMood.gradient))
        ) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        modifier = Modifier.padding(4.dp),
                        title = {
                            Text(
                                "New Entry",
                                color = fixedTextColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = onNavigateBack,
                                modifier = Modifier
                                    .background(
                                        color = fixedTextColor.copy(alpha = 0.1f),
                                        shape = CircleShape
                                    )
                                    .padding(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = fixedTextColor
                                )
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = { viewModel.saveEntry(title, content, selectedMood.label) },
                                modifier = Modifier
                                    .background(
                                        color = fixedTextColor.copy(alpha = 0.1f),
                                        shape = CircleShape
                                    )
                                    .padding(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Save",
                                    tint = fixedTextColor
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                },
                containerColor = Color.Transparent
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    MoodSelector(
                        moods = moodOptions,
                        selectedMood = selectedMood,
                        onMoodSelected = { selectedMood = it },
                        textColor = fixedTextColor
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                    ) {
                        DiaryPaper(
                            title = title,
                            onTitleChange = { title = it },
                            content = content,
                            onContentChange = { content = it },
                            textColor = fixedTextColor,
                            shadowColor = shadowColor
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
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
    textColor: Color,
    shadowColor: Color,
    modifier: Modifier = Modifier
) {
    val paperColor = Color.White.copy(alpha = 0.9f)
    val linesColor = textColor.copy(alpha = 0.1f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(RoundedCornerShape(12.dp))
            .background(paperColor)
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date()),
            style = MaterialTheme.typography.labelMedium,
            color = textColor.copy(alpha = 0.6f),
            modifier = Modifier.align(Alignment.End)
        )
        Spacer(modifier = Modifier.height(16.dp))

        BasicTextField(
            value = title,
            onValueChange = onTitleChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.titleLarge.copy(
                color = textColor,
                fontWeight = FontWeight.SemiBold
            ),
            cursorBrush = SolidColor(textColor),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (title.isEmpty()) {
                        Text(
                            "Add a title...",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = textColor.copy(alpha = 0.4f),
                                fontWeight = FontWeight.SemiBold
                            )
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
                .fillMaxWidth()
                .defaultMinSize(minHeight = 200.dp)
                .drawBehind {
                    val lineHeight = 32.sp.toPx()
                    var y = lineHeight * 1.5f
                    while (y < size.height) {
                        drawLine(
                            color = linesColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f
                        )
                        y += lineHeight
                    }
                },
            textStyle = handwritingStyle.copy(
                color = textColor.copy(alpha = 0.9f),
                fontSize = 20.sp,
                lineHeight = 32.sp
            ),
            cursorBrush = SolidColor(textColor),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.TopStart) {
                    if (content.isEmpty()) {
                        Text(
                            "What's in your mind...",
                            style = handwritingStyle.copy(
                                fontSize = 18.sp,
                                lineHeight = 32.sp
                            ),
                            color = textColor.copy(alpha = 0.4f)
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
            horizontalArrangement = Arrangement.spacedBy(16.dp),
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
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) textColor.copy(alpha = 0.15f) else Color.Transparent,
        label = "MoodItemBackground"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) textColor.copy(alpha = 0.3f) else Color.Transparent,
        label = "MoodItemBorder"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = borderColor,
                    shape = CircleShape
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = mood.icon,
                contentDescription = mood.label,
                tint = textColor,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = mood.label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(bottom = 4.dp)
        )
    }
}

