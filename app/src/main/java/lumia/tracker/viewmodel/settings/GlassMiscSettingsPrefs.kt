package lumia.tracker.viewmodel.settings

import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GlassMiscSettingsPrefs(private val prefs: SharedPreferences) {

    private val _glassBackdropStyle = MutableStateFlow(prefs.getString("glass_backdrop_style", "Translucent") ?: "Translucent")
    val glassBackdropStyle: StateFlow<String> = _glassBackdropStyle.asStateFlow()

    private val _glassOpacityValue = MutableStateFlow(prefs.getFloat("glass_opacity_value", 0.6f))
    val glassOpacityValue: StateFlow<Float> = _glassOpacityValue.asStateFlow()

    private val _betaGlassDynamic = MutableStateFlow(prefs.getBoolean("beta_glass_dynamic", true))
    val betaGlassDynamic: StateFlow<Boolean> = _betaGlassDynamic.asStateFlow()

    private val _betaFrostGlass = MutableStateFlow(prefs.getBoolean("beta_frost_glass", true))
    val betaFrostGlass: StateFlow<Boolean> = _betaFrostGlass.asStateFlow()

    fun updateGlassBackdropStyle(style: String) {
        _glassBackdropStyle.value = style
        prefs.edit().putString("glass_backdrop_style", style).apply()
    }

    fun updateGlassOpacityValue(value: Float) {
        _glassOpacityValue.value = value
        prefs.edit().putFloat("glass_opacity_value", value).apply()
    }

    fun updateBetaGlassDynamic(enabled: Boolean) {
        _betaGlassDynamic.value = enabled
        prefs.edit().putBoolean("beta_glass_dynamic", enabled).apply()
    }

    fun updateBetaFrostGlass(enabled: Boolean) {
        _betaFrostGlass.value = enabled
        prefs.edit().putBoolean("beta_frost_glass", enabled).apply()
    }

    fun reloadFromPrefs(key: String, value: String) {
        when (key) {
            "glass_backdrop_style" -> _glassBackdropStyle.value = value
            "glass_opacity_value" -> _glassOpacityValue.value = value.toFloatOrNull() ?: 0.6f
            "beta_glass_dynamic" -> _betaGlassDynamic.value = value.toBooleanStrictOrNull() ?: true
            "beta_frost_glass" -> _betaFrostGlass.value = value.toBooleanStrictOrNull() ?: true
        }
    }
}
