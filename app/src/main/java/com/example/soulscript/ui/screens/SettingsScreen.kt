package com.example.soulscript.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.soulscript.data.ThemeOption
import com.example.soulscript.ui.viewmodels.ExportState
import com.example.soulscript.ui.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val theme by viewModel.theme.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val exportState by viewModel.exportState.collectAsState()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showChangeNameDialog by remember { mutableStateOf(false) }


    val context = LocalContext.current

    LaunchedEffect(exportState) {
        when (exportState) {
            ExportState.Success -> {
                Toast.makeText(context, "Journal exported to Downloads folder", Toast.LENGTH_LONG)
                    .show()
                viewModel.resetExportState()
            }

            ExportState.Error -> {
                Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
                viewModel.resetExportState()
            }

            else -> {}
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
                Toast.makeText(context, "All data has been cleared", Toast.LENGTH_SHORT).show()
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
                    onClick = {
                        showThemeDialog = true

                    }
                )
            }

            item {
                SettingsSectionTitle("Notifications")
                SettingSwitchItem(
                    icon = Icons.Default.Notifications,
                    title = "Daily Reminder",
                    subtitle = if (notificationsEnabled) "Enabled" else "Disabled",
                    checked = notificationsEnabled,
                    onCheckedChange = {
                        Toast.makeText(context, "Coming soon", Toast.LENGTH_SHORT).show()
                    }
                )
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
                    onClick = { Toast.makeText(context, "Coming soon", Toast.LENGTH_SHORT).show() }
                )
                SettingClickableItem(
                    icon = Icons.Default.DeleteForever,
                    title = "Clear All Data",
                    subtitle = "Permanently delete all entries",
                    onClick = { showClearDataDialog = true }
                )
            }

            item {
                SettingsSectionTitle("About")
                SettingInfoItem(title = "App Version", subtitle = "1.0.0")
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
            tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface.copy(
                alpha = 0.38f
            )
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) LocalContentColor.current else LocalContentColor.current.copy(
                    alpha = 0.38f
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = 0.38f
                )
            )
        }
        if (isLoading) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(24.dp)
            )
        } else if (enabled) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ThemeSelectionDialog(
    currentTheme: ThemeOption,
    onThemeSelected: (ThemeOption) -> Unit,
    onDismiss: () -> Unit,
    context: Context
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
                                Toast.makeText(context, "Might experience issues in light theme!!", Toast.LENGTH_SHORT)
                                    .show()
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
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Delete", color = MaterialTheme.colorScheme.onError)
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
