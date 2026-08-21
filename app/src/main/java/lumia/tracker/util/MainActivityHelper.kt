package lumia.tracker.util

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import lumia.tracker.viewmodel.ScholarViewModel

object MainActivityHelper {

    fun applyDisplayCutoutAndBars(
        activity: Activity,
        displayLayoutMode: String,
        systemBarVisible: Boolean
    ) {
        val window = activity.window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes = window.attributes.apply {
                layoutInDisplayCutoutMode = if (displayLayoutMode == "Immersive") {
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                } else {
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
                }
            }
        }
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        if (displayLayoutMode == "Immersive" && !systemBarVisible) {
            controller.hide(WindowInsetsCompat.Type.statusBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            controller.show(WindowInsetsCompat.Type.statusBars())
        }
    }

    fun handleIntentNavigation(
        intent: Intent?,
        navController: NavController,
        viewModel: ScholarViewModel
    ) {
        intent ?: return
        intent.getStringExtra("OPEN_SCREEN")?.takeIf { it.isNotEmpty() }?.let {
            navController.navigate(it) { launchSingleTop = true }
            intent.removeExtra("OPEN_SCREEN")
        }
        val openTab = intent.getIntExtra("OPEN_TAB", -1)
        if (openTab != -1) {
            viewModel.setSelectedDashboardTab(openTab)
            navController.navigate("dashboard") {
                popUpTo("dashboard") { inclusive = false }
                launchSingleTop = true
            }
            intent.removeExtra("OPEN_TAB")
        }
        if (intent.action == "ACTION_OPEN_POMODORO" || intent.getBooleanExtra("OPEN_POMODORO", false)) {
            navController.navigate("pomodoro") { launchSingleTop = true }
            intent.removeExtra("OPEN_POMODORO")
            intent.action = null
        }
    }
}
