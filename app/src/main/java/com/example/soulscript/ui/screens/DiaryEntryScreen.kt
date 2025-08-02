package com.example.soulscript.screens

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.soulscript.ui.theme.Poppins
import com.example.soulscript.ui.theme.SoulScriptTheme
import com.example.soulscript.ui.theme.handwritingStyle
import com.example.soulscript.ui.viewmodels.DiaryEntryViewModel
import java.io.File
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
    onSaveNote: () -> Unit,
    onNavigateToDrawing: () -> Unit,
    sketchPath: String?
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(sketchPath) {
        if (sketchPath != null) {
            viewModel.onSketchPathChange(sketchPath)
        }
    }

    val context = LocalContext.current
    val fixedForegroundColor = Color.White.copy(alpha = 0.9f)
    val selectedMood = uiState.mood

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
                                if(uiState.title.isBlank()){
                                    Toast.makeText(context, "Please add a title", Toast.LENGTH_SHORT).show()
                                } else if(uiState.content.isBlank()){
                                    Toast.makeText(context, "Please add the content", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.saveEntry()
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
                    onMoodSelected = { viewModel.onMoodChange(it) },
                    textColor = fixedForegroundColor
                )

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    DiaryPaper(
                        title = uiState.title,
                        onTitleChange = { viewModel.onTitleChange(it) },
                        content = uiState.content,
                        onContentChange = { viewModel.onContentChange(it) },
                        sketchPath = uiState.sketchPath,
                        onAttachSketchClick = onNavigateToDrawing,
                        onRemoveSketchClick = { viewModel.onSketchPathChange(null) }
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
    sketchPath: String?,
    onAttachSketchClick: () -> Unit,
    onRemoveSketchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val paperColor = MaterialTheme.colorScheme.surface
    val onPaperColor = MaterialTheme.colorScheme.onSurface
    val placeholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
    val linesColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)

    Column(
        modifier = modifier
            .fillMaxWidth()
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
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next
            ),
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

        if (sketchPath != null) {
            Box {
                AsyncImage(
                    model = File(sketchPath),
                    contentDescription = "Attached Sketch",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onAttachSketchClick() }
                )
                IconButton(
                    onClick = onRemoveSketchClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove Sketch",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        } else {
            OutlinedButton(
                onClick = onAttachSketchClick,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Brush,
                    contentDescription = "Brush",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Attach a Sketch", color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        BasicTextField(
            value = content,
            onValueChange = onContentChange,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 300.dp)
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
