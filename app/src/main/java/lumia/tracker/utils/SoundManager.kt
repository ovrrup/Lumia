package lumia.tracker.utils

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log

object SoundManager {
    private const val TAG = "SoundManager"
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            // Use STREAM_MUSIC or STREAM_SYSTEM for satisfying media volume interaction
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize ToneGenerator: ${e.message}")
        }
    }

    private fun isSoundEnabled(context: Context): Boolean {
        return try {
            val globalPrefs = context.getSharedPreferences("global_profiles", Context.MODE_PRIVATE)
            val activeId = globalPrefs.getString("active_profile_id", "DEFAULT") ?: "DEFAULT"
            val prefName = if (activeId == "DEFAULT") "lumia_prefs" else "lumia_prefs_$activeId"
            val prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
            prefs.getBoolean("sound_effects_enabled", true)
        } catch (e: Exception) {
            true
        }
    }

    fun playClick(context: Context) {
        if (!isSoundEnabled(context)) return
        try {
            val tg = toneGenerator ?: ToneGenerator(AudioManager.STREAM_MUSIC, 70).also { toneGenerator = it }
            // TONE_CDMA_PIP is a very pleasant, short click sound
            tg.startTone(ToneGenerator.TONE_CDMA_PIP, 35)
        } catch (e: Exception) {
            Log.e(TAG, "Error playing click tone: ${e.message}")
        }
    }

    fun playSuccess(context: Context) {
        if (!isSoundEnabled(context)) return
        try {
            val tg = toneGenerator ?: ToneGenerator(AudioManager.STREAM_MUSIC, 70).also { toneGenerator = it }
            // TONE_PROP_ACK is a beautiful high-tech double-beep success chime
            tg.startTone(ToneGenerator.TONE_PROP_ACK, 120)
        } catch (e: Exception) {
            Log.e(TAG, "Error playing success tone: ${e.message}")
        }
    }

    fun playTimerComplete(context: Context) {
        if (!isSoundEnabled(context)) return
        try {
            val tg = toneGenerator ?: ToneGenerator(AudioManager.STREAM_MUSIC, 70).also { toneGenerator = it }
            // TONE_PROP_PROMPT is a lovely notification alert sequence
            tg.startTone(ToneGenerator.TONE_PROP_PROMPT, 300)
        } catch (e: Exception) {
            Log.e(TAG, "Error playing timer complete tone: ${e.message}")
        }
    }

    fun playBuzzer(context: Context) {
        if (!isSoundEnabled(context)) return
        try {
            val tg = toneGenerator ?: ToneGenerator(AudioManager.STREAM_MUSIC, 70).also { toneGenerator = it }
            tg.startTone(ToneGenerator.TONE_SUP_ERROR, 200)
        } catch (e: Exception) {
            Log.e(TAG, "Error playing buzzer tone: ${e.message}")
        }
    }
}
