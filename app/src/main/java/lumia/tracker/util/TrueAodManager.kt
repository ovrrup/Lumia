package lumia.tracker.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

@SuppressLint("StaticFieldLeak")
object TrueAodManager {
    private var windowManager: WindowManager? = null
    private var composeView: ComposeView? = null
    private var lifecycleOwner: OverlayLifecycleOwner? = null

    class OverlayLifecycleOwner : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
        private val lifecycleRegistry = LifecycleRegistry(this)
        private val mViewModelStore = ViewModelStore()
        private val savedStateRegistryController = SavedStateRegistryController.create(this)

        init {
            lifecycleRegistry.currentState = Lifecycle.State.INITIALIZED
        }

        fun onCreate() {
            savedStateRegistryController.performRestore(null)
            lifecycleRegistry.currentState = Lifecycle.State.CREATED
        }

        fun onStart() {
            lifecycleRegistry.currentState = Lifecycle.State.STARTED
        }

        fun onResume() {
            lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        }

        fun onDestroy() {
            lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
            mViewModelStore.clear()
        }

        override val lifecycle: Lifecycle = lifecycleRegistry
        override val viewModelStore: ViewModelStore = mViewModelStore
        override val savedStateRegistry: SavedStateRegistry = savedStateRegistryController.savedStateRegistry
    }

    fun isOverlayShowing(): Boolean {
        return composeView != null
    }

    @SuppressLint("ClickableViewAccessibility")
    fun showAodOverlay(
        context: Context,
        useAccessibility: Boolean,
        dimnessLevel: Float,
        sensitivity: String,
        motionSensitivity: Float = 1.2f,
        lockTimeoutSeconds: Int,
        onExit: () -> Unit
    ) {
        if (composeView != null) return

        val overlayContext = if (useAccessibility) {
            lumia.tracker.service.AodAccessibilityService.instance ?: context
        } else {
            context
        }

        val wm = overlayContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager = wm

        val localLifecycle = OverlayLifecycleOwner()
        localLifecycle.onCreate()
        localLifecycle.onStart()
        localLifecycle.onResume()
        lifecycleOwner = localLifecycle

        val layoutParams = WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            type = if (useAccessibility && overlayContext is android.accessibilityservice.AccessibilityService) {
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                }
            }
            flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
            screenBrightness = 0.01f
            format = PixelFormat.TRANSLUCENT
            gravity = Gravity.FILL
        }

        val view = ComposeView(overlayContext).apply {
            setViewTreeLifecycleOwner(localLifecycle)
            setViewTreeViewModelStoreOwner(localLifecycle)
            setViewTreeSavedStateRegistryOwner(localLifecycle)
            
            setContent {
                TrueAodOverlayUi(
                    dimnessLevel = dimnessLevel,
                    sensitivity = sensitivity,
                    motionSensitivity = motionSensitivity,
                    lockTimeoutSeconds = lockTimeoutSeconds,
                    onExitRequest = {
                        dismissAodOverlay()
                        onExit()
                    }
                )
            }
        }

        try {
            wm.addView(view, layoutParams)
            composeView = view
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun dismissAodOverlay() {
        val wm = windowManager ?: return
        val view = composeView ?: return
        try {
            wm.removeView(view)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        composeView = null
        windowManager = null
        lifecycleOwner?.onDestroy()
        lifecycleOwner = null
    }
}
