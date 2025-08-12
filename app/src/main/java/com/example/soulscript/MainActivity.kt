package com.example.soulscript

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.soulscript.utils.ThemeOption
import com.example.soulscript.navigation.Routes
import com.example.soulscript.navigation.navigationItems
import com.example.soulscript.ui.screens.DiaryEntryScreen
import com.example.soulscript.ui.screens.DrawingScreen
import com.example.soulscript.ui.screens.NoteDetailScreen
import com.example.soulscript.ui.screens.StatsScreen
import com.example.soulscript.ui.screens.HistoryScreen
import com.example.soulscript.ui.screens.HomeScreen
import com.example.soulscript.ui.screens.LockScreen
import com.example.soulscript.ui.screens.NameEntryScreen
import com.example.soulscript.ui.screens.SettingsScreen
import com.example.soulscript.ui.screens.TemplatesScreen
import com.example.soulscript.ui.screens.WelcomeScreen
import com.example.soulscript.ui.theme.SoulScriptTheme
import com.example.soulscript.ui.viewmodels.DiaryEntryViewModel
import com.example.soulscript.ui.viewmodels.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
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
    val lockEnabled by settingsViewModel.lockEnabled.collectAsState()
    var isUnlocked by remember { mutableStateOf(false) }

    when (onboardingCompleted) {
        null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier.size(100.dp)
                )
            }
        }
        true -> {
            if (lockEnabled && !isUnlocked) {
                LockScreen(onUnlock = { isUnlocked = true })
            } else {
                MainScreen()
            }
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

val TAG = "Adi"

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
                    },
                    onGuidedJournalingClick = {
                        navController.navigate(Routes.Templates)
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

            composable(
                route = "${Routes.DiaryEntry}?templateTitle={templateTitle}&templateContent={templateContent}&sketchPath={sketchPath}",
                arguments = listOf(
                    navArgument("templateTitle") { nullable = true; type = NavType.StringType },
                    navArgument("templateContent") { nullable = true; type = NavType.StringType },
                    navArgument("sketchPath") { nullable = true; type = NavType.StringType }
                )
            ) { backStackEntry ->
                val diaryEntryViewModel: DiaryEntryViewModel = hiltViewModel()
                val sketchPath = backStackEntry.savedStateHandle.get<String>("sketchPath")
                val encodedTitle = backStackEntry.arguments?.getString("templateTitle")
                val encodedContent = backStackEntry.arguments?.getString("templateContent")

                Log.i(TAG, "Received : $encodedTitle $encodedContent")
                diaryEntryViewModel.onParametersReceived(
                    encodedTitle?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) },
                    encodedContent?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) }
                )
                DiaryEntryScreen(
                    onNavigateBack = { navController.popBackStack() },
                    viewModel = diaryEntryViewModel,
                    onSaveNote = {
                        Toast.makeText(context, "Note saved successfully", Toast.LENGTH_SHORT)
                            .show()
                        navController.popBackStack()
                    },
                    onNavigateToDrawing = { navController.navigate(Routes.Drawing) },
                    sketchPath = sketchPath
                )
            }

            composable(Routes.Templates) {
                TemplatesScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onTemplateSelected = { title, content ->
                        Log.i("Adi", "Template: $title $content")
                        val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
                        val encodedContent = URLEncoder.encode(content, StandardCharsets.UTF_8.toString())
                        val sketchPath = null;
                        navController.navigate("${Routes.DiaryEntry}?templateTitle=$encodedTitle&templateContent=$encodedContent&sketchPath=$sketchPath")
                    }
                )
            }
            composable(Routes.Drawing) {
                DrawingScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onSaveSketch = { path ->
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("sketchPath", path)
                        navController.popBackStack()
                    }
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
