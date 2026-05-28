package id.alpine12.vehiclecare.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import id.alpine12.vehiclecare.ui.screens.history.HistoryScreen
import id.alpine12.vehiclecare.ui.screens.home.HomeScreen
import id.alpine12.vehiclecare.ui.screens.home.SelectVehicleScreen
import id.alpine12.vehiclecare.ui.screens.reminder.AddReminderScreen
import id.alpine12.vehiclecare.ui.screens.reminder.ReminderScreen
import id.alpine12.vehiclecare.ui.screens.service.AddServiceScreen
import id.alpine12.vehiclecare.ui.screens.splash.SplashScreen
import id.alpine12.vehiclecare.ui.screens.stats.StatsScreen
import id.alpine12.vehiclecare.ui.screens.vehicle.AddVehicleScreen
import id.alpine12.vehiclecare.ui.screens.vehicle.EditVehicleScreen
import id.alpine12.vehiclecare.ui.screens.vehicle.VehicleDetailScreen

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        Screen.Home,
        "Beranda",
        Icons.Filled.DirectionsCar,
        Icons.Outlined.DirectionsCar
    ),
    BottomNavItem(
        Screen.History,
        "Riwayat",
        Icons.Filled.History,
        Icons.Outlined.History
    ),
    BottomNavItem(
        Screen.Reminder,
        "Pengingat",
        Icons.Filled.Notifications,
        Icons.Outlined.Notifications
    ),
    BottomNavItem(
        Screen.Stats,
        "Statistik",
        Icons.Filled.PieChart,
        Icons.Outlined.PieChart
    )
)

@Composable
fun VehicleCareNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = listOf(
        Screen.Home.route,
        Screen.History.route,
        Screen.Reminder.route,
        Screen.Stats.route
    ).contains(currentDestination?.route)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(onTimeout = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                })
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToAddVehicle = { navController.navigate(Screen.AddVehicle.route) },
                    onNavigateToVehicleDetail = { vehicleId ->
                        navController.navigate(Screen.VehicleDetail.createRoute(vehicleId))
                    }
                )
            }

            composable(Screen.History.route) {
                HistoryScreen()
            }

            composable(Screen.Reminder.route) {
                ReminderScreen(
                    onNavigateToSelectVehicle = {
                        navController.navigate(Screen.SelectVehicle.route)
                    }
                )
            }

            composable(Screen.Stats.route) {
                StatsScreen()
            }

            composable(Screen.SelectVehicle.route) {
                SelectVehicleScreen(
                    title = "Pilih Kendaraan",
                    onVehicleSelected = { vehicleId ->
                        navController.navigate(Screen.AddReminder.createRoute(vehicleId)) {
                            popUpTo(Screen.Reminder.route)
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.AddVehicle.route) {
                AddVehicleScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(
                route = Screen.EditVehicle.route,
                arguments = listOf(navArgument("vehicleId") { type = NavType.LongType })
            ) { backStackEntry ->
                val vehicleId = backStackEntry.arguments?.getLong("vehicleId") ?: return@composable
                EditVehicleScreen(vehicleId = vehicleId, onNavigateBack = { navController.popBackStack() })
            }

            composable(
                route = Screen.VehicleDetail.route,
                arguments = listOf(navArgument("vehicleId") { type = NavType.LongType })
            ) { backStackEntry ->
                val vehicleId = backStackEntry.arguments?.getLong("vehicleId") ?: return@composable
                VehicleDetailScreen(
                    vehicleId = vehicleId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEditVehicle = { navController.navigate(Screen.EditVehicle.createRoute(vehicleId)) },
                    onNavigateToAddService = { navController.navigate(Screen.AddService.createRoute(vehicleId)) },
                    onNavigateToAddReminder = { navController.navigate(Screen.AddReminder.createRoute(vehicleId)) }
                )
            }

            composable(
                route = Screen.AddService.route,
                arguments = listOf(navArgument("vehicleId") { type = NavType.LongType })
            ) { backStackEntry ->
                val vehicleId = backStackEntry.arguments?.getLong("vehicleId") ?: return@composable
                AddServiceScreen(vehicleId = vehicleId, onNavigateBack = { navController.popBackStack() })
            }

            composable(
                route = Screen.AddReminder.route,
                arguments = listOf(navArgument("vehicleId") { type = NavType.LongType })
            ) { backStackEntry ->
                val vehicleId = backStackEntry.arguments?.getLong("vehicleId") ?: return@composable
                AddReminderScreen(vehicleId = vehicleId, onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}
