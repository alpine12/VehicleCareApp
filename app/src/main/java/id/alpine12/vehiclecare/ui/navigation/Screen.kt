package id.alpine12.vehiclecare.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Home : Screen("home")
    data object History : Screen("history")
    data object Reminder : Screen("reminder")
    data object Stats : Screen("stats")
    data object AddVehicle : Screen("add_vehicle")
    data object EditVehicle : Screen("edit_vehicle/{vehicleId}") {
        fun createRoute(vehicleId: Long) = "edit_vehicle/$vehicleId"
    }
    data object VehicleDetail : Screen("vehicle_detail/{vehicleId}") {
        fun createRoute(vehicleId: Long) = "vehicle_detail/$vehicleId"
    }
    data object AddService : Screen("add_service/{vehicleId}") {
        fun createRoute(vehicleId: Long) = "add_service/$vehicleId"
    }
    data object AddReminder : Screen("add_reminder/{vehicleId}") {
        fun createRoute(vehicleId: Long) = "add_reminder/$vehicleId"
    }
    data object SelectVehicle : Screen("select_vehicle") {
        const val FOR_REMINDER = "for_reminder"
        const val FOR_SERVICE = "for_service"
    }
}
