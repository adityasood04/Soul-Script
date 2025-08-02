package com.example.soulscript.screens

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.outlined.LayersClear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawingScreen(
    onNavigateBack: () -> Unit,
    onSaveSketch: (String) -> Unit
) {
    val context = LocalContext.current
    var currentColor by remember { mutableStateOf(Color.Black) }
    var currentStrokeWidth by remember { mutableFloatStateOf(10f) }
    var isErasing by remember { mutableStateOf(false) }

    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    val imageBitmap = remember(bitmap) { bitmap?.asImageBitmap() }
    var currentPath by remember { mutableStateOf<Path?>(null) }

    val undoStack = remember { mutableStateListOf<Bitmap>() }

    val drawColor = if (isErasing) Color.White else currentColor

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add a Sketch") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (undoStack.isNotEmpty()) {
                                bitmap = undoStack.removeAt(undoStack.size -1 )
                            }
                        },
                        enabled = undoStack.isNotEmpty()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
                    }
                    IconButton(
                        onClick = {
                            bitmap?.let {
                                val config = it.config ?: Bitmap.Config.ARGB_8888
                                undoStack.add(it.copy(config, true))
                                val newBitmap = it.copy(config, true)
                                val canvas = android.graphics.Canvas(newBitmap)
                                canvas.drawColor(android.graphics.Color.WHITE)
                                bitmap = newBitmap
                                currentPath = null
                            }
                        },
                        enabled = undoStack.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear Canvas")
                    }
                    IconButton(
                        onClick = {
                            bitmap?.let {
                                val file = saveBitmapAsImage(context, it)
                                if (file != null) {
                                    onSaveSketch(file.absolutePath)
                                }
                            }
                        },
                        enabled = undoStack.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save Sketch")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.LightGray)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(isErasing, currentColor, currentStrokeWidth) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    bitmap?.let {
                                        val config = it.config ?: Bitmap.Config.ARGB_8888
                                        undoStack.add(it.copy(config, true))
                                    }
                                    currentPath = Path().apply { moveTo(offset.x, offset.y) }
                                },
                                onDrag = { change, _ ->
                                    currentPath?.lineTo(change.position.x, change.position.y)
                                },
                                onDragEnd = {
                                    currentPath?.let { path ->
                                        val newBitmap = bitmap ?: Bitmap.createBitmap(
                                            size.width.toInt(),
                                            size.height.toInt(),
                                            Bitmap.Config.ARGB_8888
                                        )
                                        val canvas = android.graphics.Canvas(newBitmap)
                                        val paint = createPaint(drawColor, currentStrokeWidth)
                                        canvas.drawPath(path.asAndroidPath(), paint)
                                        bitmap = newBitmap
                                    }
                                    currentPath = null
                                }
                            )
                        }
                ) {
                    if (bitmap == null && size.width > 0 && size.height > 0) {
                        val newBitmap = Bitmap.createBitmap(
                            size.width.toInt(),
                            size.height.toInt(),
                            Bitmap.Config.ARGB_8888
                        )
                        val canvas = android.graphics.Canvas(newBitmap)
                        canvas.drawColor(android.graphics.Color.WHITE)
                        bitmap = newBitmap
                    }

                    imageBitmap?.let {
                        drawImage(it)
                    }

                    currentPath?.let { path ->
                        drawPath(
                            path = path,
                            color = drawColor,
                            style = Stroke(
                                width = currentStrokeWidth,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tools", style = MaterialTheme.typography.titleMedium)
                        IconToggleButton(
                            checked = isErasing,
                            onCheckedChange = { isErasing = it }
                        ) {
                            Icon(
                                imageVector = if (isErasing) Icons.Filled.Brush else Icons.Outlined.LayersClear,
                                contentDescription = if (isErasing) "Draw Mode" else "Erase Mode"
                            )
                        }
                    }

                    ColorPicker(
                        selectedColor = currentColor,
                        onColorSelected = {
                            currentColor = it
                            isErasing = false
                        }
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Stroke Width: ${currentStrokeWidth.toInt()}",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Slider(
                        value = currentStrokeWidth,
                        onValueChange = { currentStrokeWidth = it },
                        valueRange = 2f..50f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorPicker(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    val colors = listOf(Color.Black, Color.Red, Color.Blue, Color.Green, Color.Yellow, Color.Magenta, Color.Cyan)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        colors.forEach { color ->
            val isSelected = color == selectedColor
            Box(
                modifier = Modifier
                    .size(if (isSelected) 40.dp else 32.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = 2.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable { onColorSelected(color) }
            )
        }
    }
}

private fun createPaint(color: Color, strokeWidth: Float): android.graphics.Paint {
    return android.graphics.Paint().apply {
        this.color = color.toArgb()
        this.strokeWidth = strokeWidth
        this.style = android.graphics.Paint.Style.STROKE
        this.strokeCap = android.graphics.Paint.Cap.ROUND
        this.strokeJoin = android.graphics.Paint.Join.ROUND
        this.isAntiAlias = true
    }
}

private fun saveBitmapAsImage(context: Context, bitmap: Bitmap): File? {
    val maxDimension = 1080
    val originalWidth = bitmap.width
    val originalHeight = bitmap.height
    val scaledWidth: Int
    val scaledHeight: Int

    if (originalWidth > originalHeight) {
        scaledWidth = maxDimension
        scaledHeight = (originalHeight.toFloat() / originalWidth.toFloat() * maxDimension).toInt()
    } else {
        scaledHeight = maxDimension
        scaledWidth = (originalWidth.toFloat() / originalHeight.toFloat() * maxDimension).toInt()
    }

    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
    val file = File(context.filesDir, "sketch_${System.currentTimeMillis()}.jpg")
    return try {
        FileOutputStream(file).use { fos ->
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, fos)
        }
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
