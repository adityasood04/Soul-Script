package com.example.soulscript

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import com.example.soulscript.ui.theme.MoodDiaryTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoodDiaryApp()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MoodDiaryApp() {
    MoodDiaryTheme {
    }
}