package lumia.tracker.viewmodel.settings

import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NavBarSettingsPrefs(private val prefs: SharedPreferences) {

    private val _betaFloatingNav = MutableStateFlow(prefs.getBoolean("beta_floating_nav", false))
    val betaFloatingNav: StateFlow<Boolean> = _betaFloatingNav.asStateFlow()

    private val _navBarHeight = MutableStateFlow(prefs.getFloat("nav_bar_height", 80f))
    val navBarHeight: StateFlow<Float> = _navBarHeight.asStateFlow()

    private val _navBarPaddingHorizontal = MutableStateFlow(prefs.getFloat("nav_bar_padding_horizontal", 24f))
    val navBarPaddingHorizontal: StateFlow<Float> = _navBarPaddingHorizontal.asStateFlow()

    private val _navBarPaddingBottom = MutableStateFlow(prefs.getFloat("nav_bar_padding_bottom", 24f))
    val navBarPaddingBottom: StateFlow<Float> = _navBarPaddingBottom.asStateFlow()

    private val _navBarCornerRadius = MutableStateFlow(prefs.getFloat("nav_bar_corner_radius", 32f))
    val navBarCornerRadius: StateFlow<Float> = _navBarCornerRadius.asStateFlow()

    private val _navBarLabelMode = MutableStateFlow(prefs.getString("nav_bar_label_mode", "Always") ?: "Always")
    val navBarLabelMode: StateFlow<String> = _navBarLabelMode.asStateFlow()

    private val _navBarGlassForceEnabled = MutableStateFlow(prefs.getBoolean("nav_bar_glass_force_enabled", false))
    val navBarGlassForceEnabled: StateFlow<Boolean> = _navBarGlassForceEnabled.asStateFlow()

    private val _navBarIndicatorAlpha = MutableStateFlow(prefs.getFloat("nav_bar_indicator_alpha", 0.15f))
    val navBarIndicatorAlpha: StateFlow<Float> = _navBarIndicatorAlpha.asStateFlow()

    private val _betaNavBarSizeControls = MutableStateFlow(prefs.getBoolean("beta_nav_bar_size_controls", false))
    val betaNavBarSizeControls: StateFlow<Boolean> = _betaNavBarSizeControls.asStateFlow()

    private val _navBarGlassLinkedToMain = MutableStateFlow(prefs.getBoolean("nav_bar_glass_linked_to_main", true))
    val navBarGlassLinkedToMain: StateFlow<Boolean> = _navBarGlassLinkedToMain.asStateFlow()

    private val _navBarGlassBackdropStyle = MutableStateFlow(prefs.getString("nav_bar_glass_backdrop_style", "Translucent") ?: "Translucent")
    val navBarGlassBackdropStyle: StateFlow<String> = _navBarGlassBackdropStyle.asStateFlow()

    private val _navBarGlassDynamic = MutableStateFlow(prefs.getBoolean("nav_bar_glass_dynamic", true))
    val navBarGlassDynamic: StateFlow<Boolean> = _navBarGlassDynamic.asStateFlow()

    private val _navBarGlassOpacityValue = MutableStateFlow(0.6f)
    val navBarGlassOpacityValue: StateFlow<Float> = _navBarGlassOpacityValue.asStateFlow()

    fun updateBetaFloatingNav(enabled: Boolean) {
        _betaFloatingNav.value = enabled
        prefs.edit().putBoolean("beta_floating_nav", enabled).apply()
    }

    fun updateNavBarHeight(height: Float) {
        _navBarHeight.value = height
        prefs.edit().putFloat("nav_bar_height", height).apply()
    }

    fun updateNavBarPaddingHorizontal(padding: Float) {
        _navBarPaddingHorizontal.value = padding
        prefs.edit().putFloat("nav_bar_padding_horizontal", padding).apply()
    }

    fun updateNavBarPaddingBottom(padding: Float) {
        _navBarPaddingBottom.value = padding
        prefs.edit().putFloat("nav_bar_padding_bottom", padding).apply()
    }

    fun updateNavBarCornerRadius(radius: Float) {
        _navBarCornerRadius.value = radius
        prefs.edit().putFloat("nav_bar_corner_radius", radius).apply()
    }

    fun updateNavBarLabelMode(mode: String) {
        _navBarLabelMode.value = mode
        prefs.edit().putString("nav_bar_label_mode", mode).apply()
    }

    fun updateNavBarGlassForceEnabled(enabled: Boolean) {
        _navBarGlassForceEnabled.value = enabled
        prefs.edit().putBoolean("nav_bar_glass_force_enabled", enabled).apply()
    }

    fun updateNavBarIndicatorAlpha(alpha: Float) {
        _navBarIndicatorAlpha.value = alpha
        prefs.edit().putFloat("nav_bar_indicator_alpha", alpha).apply()
    }

    fun updateBetaNavBarSizeControls(enabled: Boolean) {
        _betaNavBarSizeControls.value = enabled
        prefs.edit().putBoolean("beta_nav_bar_size_controls", enabled).apply()
    }

    fun updateNavBarGlassLinkedToMain(enabled: Boolean) {
        _navBarGlassLinkedToMain.value = enabled
        prefs.edit().putBoolean("nav_bar_glass_linked_to_main", enabled).apply()
    }

    fun updateNavBarGlassBackdropStyle(style: String) {
        _navBarGlassBackdropStyle.value = style
        prefs.edit().putString("nav_bar_glass_backdrop_style", style).apply()
    }

    fun updateNavBarGlassDynamic(enabled: Boolean) {
        _navBarGlassDynamic.value = enabled
        prefs.edit().putBoolean("nav_bar_glass_dynamic", enabled).apply()
    }

    fun updateNavBarGlassOpacityValue(value: Float, alias: String, isDark: Boolean) {
        val key = "nav_glass_opacity_${alias}_${if (isDark) "dark" else "light"}"
        _navBarGlassOpacityValue.value = value
        prefs.edit().putFloat(key, value).apply()
    }

    fun refreshNavBarGlassOpacity(alias: String, isDark: Boolean) {
        val key = "nav_glass_opacity_${alias}_${if (isDark) "dark" else "light"}"
        _navBarGlassOpacityValue.value = prefs.getFloat(key, 0.6f)
    }

    fun reloadFromPrefs(key: String, value: String) {
        when (key) {
            "beta_floating_nav" -> _betaFloatingNav.value = value.toBooleanStrictOrNull() ?: false
            "nav_bar_height" -> _navBarHeight.value = value.toFloatOrNull() ?: 80f
            "nav_bar_padding_horizontal" -> _navBarPaddingHorizontal.value = value.toFloatOrNull() ?: 24f
            "nav_bar_padding_bottom" -> _navBarPaddingBottom.value = value.toFloatOrNull() ?: 24f
            "nav_bar_corner_radius" -> _navBarCornerRadius.value = value.toFloatOrNull() ?: 32f
            "nav_bar_label_mode" -> _navBarLabelMode.value = value
            "nav_bar_glass_force_enabled" -> _navBarGlassForceEnabled.value = value.toBooleanStrictOrNull() ?: false
            "nav_bar_indicator_alpha" -> _navBarIndicatorAlpha.value = value.toFloatOrNull() ?: 0.15f
            "beta_nav_bar_size_controls" -> _betaNavBarSizeControls.value = value.toBooleanStrictOrNull() ?: false
            "nav_bar_glass_linked_to_main" -> _navBarGlassLinkedToMain.value = value.toBooleanStrictOrNull() ?: true
            "nav_bar_glass_backdrop_style" -> _navBarGlassBackdropStyle.value = value
            "nav_bar_glass_dynamic" -> _navBarGlassDynamic.value = value.toBooleanStrictOrNull() ?: true
            "nav_bar_glass_opacity_value" -> _navBarGlassOpacityValue.value = value.toFloatOrNull() ?: 0.6f
        }
    }
}
