package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.outlined.Accessibility
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.FitnessViewModel
import com.example.ui.screens.BlueprintScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.screens.TrackerScreen
import com.example.ui.theme.CardSlate
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.TechBlack

class MainActivity : ComponentActivity() {

    private val viewModel: FitnessViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AppMainShell(viewModel)
            }
        }
    }
}

// Named routes for safe navigation architecture
const val ROUTE_DASHBOARD = "dashboard"
const val ROUTE_BLUEPRINT = "blueprint"
const val ROUTE_TRACKER = "tracker"
const val ROUTE_TOOLS = "tools"

@Composable
fun AppMainShell(viewModel: FitnessViewModel) {
    val navController = rememberNavController()
    var currentRoute by remember { mutableStateOf(ROUTE_DASHBOARD) }

    val navItems = listOf(
        NavigationItem(
            route = ROUTE_DASHBOARD,
            title = "Mission",
            selectedIcon = Icons.Filled.Dashboard,
            unselectedIcon = Icons.Outlined.Dashboard
        ),
        NavigationItem(
            route = ROUTE_BLUEPRINT,
            title = "Blueprint",
            selectedIcon = Icons.Filled.Accessibility,
            unselectedIcon = Icons.Outlined.Accessibility
        ),
        NavigationItem(
            route = ROUTE_TRACKER,
            title = "Tracker",
            selectedIcon = Icons.Filled.FitnessCenter,
            unselectedIcon = Icons.Outlined.FitnessCenter
        ),
        NavigationItem(
            route = ROUTE_TOOLS,
            title = "Tools",
            selectedIcon = Icons.Filled.EmojiEvents,
            unselectedIcon = Icons.Outlined.EmojiEvents
        )
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = TechBlack,
        bottomBar = {
            NavigationBar(
                containerColor = CardSlate,
                tonalElevation = NavigationBarDefaults.Elevation
            ) {
                navItems.forEach { item ->
                    val isSelected = currentRoute == item.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != item.route) {
                                currentRoute = item.route
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title,
                                tint = if (isSelected) TechBlack else Color.White.copy(alpha = 0.6f)
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) NeonBlue else Color.White.copy(alpha = 0.6f)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = NeonBlue
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ROUTE_DASHBOARD,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(ROUTE_DASHBOARD) {
                DashboardScreen(viewModel = viewModel)
            }
            composable(ROUTE_BLUEPRINT) {
                BlueprintScreen(viewModel = viewModel)
            }
            composable(ROUTE_TRACKER) {
                TrackerScreen(viewModel = viewModel)
            }
            composable(ROUTE_TOOLS) {
                ToolsScreen(viewModel = viewModel)
            }
        }
    }
}

data class NavigationItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)
