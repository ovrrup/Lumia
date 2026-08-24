package lumia.tracker.util

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import lumia.tracker.viewmodel.ScholarViewModel

object MainActivityHelper {

    /**
     * Configures edge-to-edge system bars and cutout mode cleanly without erratic hiding.
     */
    fun applyDisplayCutoutAndBars(
        activity: Activity,
        displayLayoutMode: String = "Immersive"
    ) {
        val window = activity.window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes = window.attributes.apply {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.show(WindowInsetsCompat.Type.systemBars())
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
