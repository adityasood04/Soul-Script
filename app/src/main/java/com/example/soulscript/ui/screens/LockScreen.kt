package com.example.soulscript.ui.screens

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soulscript.utils.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LockViewModel @Inject constructor(
    private val settingsManager: SettingsManager
) : ViewModel() {
    suspend fun checkPasscode(passcode: String): Boolean {
        val savedPasscode = settingsManager.passcodeFlow.first()
        return passcode == savedPasscode
    }
}

@Composable
fun LockScreen(
    onUnlock: () -> Unit,
    viewModel: LockViewModel = hiltViewModel()
) {
    var enteredPasscode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val biometricManager = BiometricManager.from(context)
    val canAuthWithBiometrics = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS

    val biometricPrompt = BiometricPrompt(
        context as FragmentActivity,
        ContextCompat.getMainExecutor(context),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onUnlock()
            }
        }
    )

    LaunchedEffect(Unit) {
        if (canAuthWithBiometrics) {
            biometricPrompt.authenticate(
                BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Unlock SoulScript")
                    .setSubtitle("Confirm your identity to continue")
                    .setNegativeButtonText("Use Passcode")
                    .build()
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Enter Passcode", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(4) { index ->
                val char = enteredPasscode.getOrNull(index)?.toString() ?: ""
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(char, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 16.dp))
        }

        Spacer(Modifier.height(32.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            (1..3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..3).forEach { col ->
                        val number = (row - 1) * 3 + col
                        Button(
                            onClick = { if (enteredPasscode.length < 4) enteredPasscode += number },
                            modifier = Modifier.size(72.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Text(number.toString(), fontSize = 20.sp)
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (canAuthWithBiometrics) {
                    IconButton(
                        onClick = {
                            biometricPrompt.authenticate(
                                BiometricPrompt.PromptInfo.Builder()
                                    .setTitle("Unlock SoulScript")
                                    .setSubtitle("Confirm your identity to continue")
                                    .setNegativeButtonText("Cancel")
                                    .build()
                            )
                        },
                        modifier = Modifier.size(72.dp)
                    ) {
                        Icon(Icons.Default.Fingerprint, contentDescription = "Use Biometrics", modifier = Modifier.size(36.dp),
                            MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Spacer(Modifier.size(72.dp))
                }
                Button(
                    onClick = { if (enteredPasscode.length < 4) enteredPasscode += 0 },
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text("0", fontSize = 20.sp)
                }
                IconButton(
                    onClick = { enteredPasscode = enteredPasscode.dropLast(1) },
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(Icons.Default.Backspace, contentDescription = "Backspace", modifier = Modifier.size(36.dp),
                        MaterialTheme.colorScheme.primary)
                }
            }
        }
    }

    LaunchedEffect(enteredPasscode) {
        if (enteredPasscode.length == 4) {
            if (viewModel.checkPasscode(enteredPasscode)) {
                onUnlock()
            } else {
                errorMessage = "Incorrect Passcode"
                enteredPasscode = ""
            }
        } else {
            errorMessage = null
        }
    }
}