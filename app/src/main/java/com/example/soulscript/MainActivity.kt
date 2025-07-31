package com.example.soulscript

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.soulscript.data.SettingsManager
import com.example.soulscript.data.ThemeOption
import com.example.soulscript.navigation.Routes
import com.example.soulscript.navigation.navigationItems
import com.example.soulscript.screens.DiaryEntryScreen
import com.example.soulscript.screens.HomeScreen
import com.example.soulscript.screens.StatsScreen
import com.example.soulscript.ui.screens.HistoryScreen
import com.example.soulscript.ui.screens.NameEntryScreen
import com.example.soulscript.ui.screens.NoteDetailScreen
import com.example.soulscript.ui.screens.SettingsScreen
import com.example.soulscript.ui.screens.WelcomeScreen
import com.example.soulscript.ui.theme.SoulScriptTheme
import com.example.soulscript.ui.viewmodels.DiaryEntryViewModel
import com.example.soulscript.ui.viewmodels.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val theme by settingsViewModel.theme.collectAsState()
            SoulScriptTheme(
                darkTheme = when (theme) {
                    ThemeOption.Light -> false
                    ThemeOption.Dark -> true
                    ThemeOption.System -> isSystemInDarkTheme()
                }
            ) {
                RootNavigation()
            }
        }
    }
}

@Composable
fun RootNavigation(
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val onboardingCompleted by settingsViewModel.onboardingCompletedFlow.collectAsState(initial = null)

    when (onboardingCompleted) {
        null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        true -> {
            MainScreen()
        }
        false -> {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "welcome") {
                composable("welcome") {
                    WelcomeScreen(onNavigateToNext = { navController.navigate("name_entry") })
                }
                composable("name_entry") {
                    NameEntryScreen()
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                navigationItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentRoute == item.route) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(text = item.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Home,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.Home) {
                HomeScreen(
                    onAddEntryClick = {
                        navController.navigate(Routes.DiaryEntry)
                    },
                    onNoteClick = { noteId ->
                        navController.navigate("${Routes.NoteDetail}/${noteId}")
                    }
                )
            }
            composable(Routes.History) {
                HistoryScreen(onNoteClick = {noteId->
                    navController.navigate("${Routes.NoteDetail}/${noteId}")
                })
            }
            composable(Routes.Stats) { StatsScreen() }
            composable(Routes.Settings) { SettingsScreen() }
            composable(Routes.DiaryEntry) {
                val diaryEntryViewModel: DiaryEntryViewModel = hiltViewModel()

                DiaryEntryScreen(
                    onNavigateBack = { navController.popBackStack() },
                    viewModel = diaryEntryViewModel,
                    onSaveNote = { navController.popBackStack()
                        Toast.makeText(context, "Your note has been saved", Toast.LENGTH_SHORT).show()}
                )
            }

            composable(
                route = "${Routes.NoteDetail}/{noteId}",
                arguments = listOf(navArgument("noteId") { type = NavType.IntType })
            ) {
                NoteDetailScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
