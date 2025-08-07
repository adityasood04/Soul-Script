package com.example.soulscript.screens

import android.Manifest
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.soulscript.utils.ThemeOption
import com.example.soulscript.ui.viewmodels.ExportState
import com.example.soulscript.ui.viewmodels.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val theme by viewModel.theme.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val exportState by viewModel.exportState.collectAsState()
    val reminderTime by viewModel.reminderTime.collectAsState()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showChangeNameDialog by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.setReminder(context, true, reminderTime.first, reminderTime.second)
            } else {
                Toast.makeText(context, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    )

    val timePickerState = rememberTimePickerState(
        initialHour = reminderTime.first,
        initialMinute = reminderTime.second,
        is24Hour = false
    )

    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            onConfirm = {
                viewModel.setReminder(context, true, timePickerState.hour, timePickerState.minute)
                showTimePicker = false
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }

    LaunchedEffect(exportState) {
        when (exportState) {
            ExportState.Success -> {
                Toast.makeText(context, "Journal exported to Downloads folder", Toast.LENGTH_LONG).show()
                viewModel.resetExportState()
            }
            ExportState.Error -> {
                Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
                viewModel.resetExportState()
            }
            else -> { /* Idle or InProgress */ }
        }
    }


    if (showThemeDialog) {
        ThemeSelectionDialog(
            context = context,
            currentTheme = theme,
            onThemeSelected = { viewModel.setTheme(it) },
            onDismiss = { showThemeDialog = false }
        )
    }

    if (showClearDataDialog) {
        ClearDataConfirmationDialog(
            onConfirm = {
                viewModel.clearAllData()
                showClearDataDialog = false
            },
            onDismiss = { showClearDataDialog = false }
        )
    }

    if (showChangeNameDialog) {
        ChangeNameDialog(
            currentName = userName,
            onNameChange = { newName ->
                viewModel.setUserName(newName)
                showChangeNameDialog = false
            },
            onDismiss = { showChangeNameDialog = false }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
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
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                SettingsSectionTitle("Account")
                SettingClickableItem(
                    icon = Icons.Default.AccountCircle,
                    title = "Change Name",
                    subtitle = userName,
                    onClick = { showChangeNameDialog = true }
                )
            }

            item {
                SettingsSectionTitle("Appearance")
                SettingClickableItem(
                    icon = Icons.Default.DarkMode,
                    title = "Theme",
                    subtitle = theme.name,
                    onClick = { showThemeDialog = true }
                )
            }

            item {
                SettingsSectionTitle("Notifications")
                SettingSwitchItem(
                    icon = Icons.Default.Notifications,
                    title = "Daily Reminder",
                    subtitle = if (notificationsEnabled) "Enabled at ${formatTime(reminderTime.first, reminderTime.second)}" else "Disabled",
                    checked = notificationsEnabled,
                    onCheckedChange = { isEnabled ->
                        if (isEnabled) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                viewModel.setReminder(context, true, reminderTime.first, reminderTime.second)
                            }
                        } else {
                            viewModel.setReminder(context, false, reminderTime.first, reminderTime.second)
                        }
                    }
                )
                if (notificationsEnabled) {
                    SettingClickableItem(
                        icon = Icons.Default.Schedule,
                        title = "Reminder Time",
                        subtitle = formatTime(reminderTime.first, reminderTime.second),
                        onClick = { showTimePicker = true }
                    )
                }
            }

            item {
                SettingsSectionTitle("Data Management")
                val isLoading = exportState is ExportState.InProgress
                val progress = if (exportState is ExportState.InProgress) {
                    (exportState as ExportState.InProgress).progress
                } else 0f

                SettingClickableItem(
                    icon = Icons.Default.Download,
                    title = "Export Journal",
                    subtitle = "Save a backup of all entries",
                    enabled = !isLoading,
                    isLoading = isLoading,
                    progress = progress,
                    onClick = { Toast.makeText(context, "Coming soon!", Toast.LENGTH_SHORT).show() }
                )
                SettingClickableItem(
                    icon = Icons.Default.DeleteForever,
                    title = "Clear All Data",
                    subtitle = "Permanently delete all entries",
                    onClick = { showClearDataDialog = true }
                )
            }

            item {
                SettingsSectionTitle("Links & Info")
                SettingClickableItem(
                    icon = Icons.Default.AlternateEmail,
                    title = "Contact Developer",
                    subtitle = "Find me on X (formerly Twitter)",
                    onClick = { uriHandler.openUri("https://x.com/adityasood04") }
                )
                SettingClickableItem(
                    icon = Icons.Default.Code,
                    title = "Source Code",
                    subtitle = "View the project on GitHub",
                    onClick = { uriHandler.openUri("https://github.com/adityasood04/Soul-Script") }
                )
                SettingInfoItem(title = "App Version", subtitle = "1.2.0")
            }
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun SettingClickableItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    progress: Float = 0f
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick, enabled = enabled)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) LocalContentColor.current else LocalContentColor.current.copy(alpha = 0.38f)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            )
        }
        if (isLoading) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(24.dp)
            )
        } else if (enabled) {
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SettingSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingInfoItem(title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ThemeSelectionDialog(
    context: Context,
    currentTheme: ThemeOption,
    onThemeSelected: (ThemeOption) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Theme") },
        text = {
            Column {
                ThemeOption.values().forEach { theme ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (theme == ThemeOption.Light) {
                                    Toast.makeText(context, "May experience issues with light mode", Toast.LENGTH_SHORT).show()
                                }
                                onThemeSelected(theme)
                                onDismiss()
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentTheme == theme,
                            onClick = {
                                onThemeSelected(theme)
                                onDismiss()
                            }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(theme.name)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ClearDataConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Clear All Data?") },
        text = { Text("This will permanently delete all your journal entries. This action cannot be undone.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ChangeNameDialog(
    currentName: String,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newName by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Your Name") },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("Name") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { onNameChange(newName) },
                enabled = newName.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                content()
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.align(Alignment.End)
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onConfirm) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

private fun formatTime(hour: Int, minute: Int): String {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(calendar.time)
}
