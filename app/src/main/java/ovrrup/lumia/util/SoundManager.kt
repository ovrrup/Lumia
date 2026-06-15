package ovrrup.lumia.util

import android.content.Context
import android.media.AudioManager
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object SoundManager {
    private var toneGenerator: ToneGenerator? = null
    
    fun init() {
        if (toneGenerator == null) {
            toneGenerator = ToneGenerator(AudioManager.STREAM_SYSTEM, ToneGenerator.MAX_VOLUME)
        }
    }

    private fun getVibrator(context: Context): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
    
    fun playClickSound(context: Context, playSound: Boolean, playHaptic: Boolean) {
        if (playSound) toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 25)
        if (playHaptic) {
            val vibrator = getVibrator(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(20)
            }
        }
    }

    fun playTransitionSound(context: Context, playSound: Boolean, playHaptic: Boolean) {
        if (playSound) toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 50)
        if (playHaptic) {
            val vibrator = getVibrator(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(10)
            }
        }
    }

    fun playPomodoroStart(context: Context, playSound: Boolean, playHaptic: Boolean) {
        if (playSound) {
            CoroutineScope(Dispatchers.Default).launch {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 100)
                delay(150)
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 150)
            }
        }
        if (playHaptic) {
            val vibrator = getVibrator(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(100)
            }
        }
    }

    fun playPomodoroComplete(context: Context, useDefaultAlarm: Boolean, playHaptic: Boolean) {
        if (useDefaultAlarm) {
            try {
                val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val ringtone = RingtoneManager.getRingtone(context, alarmUri)
                ringtone?.play()
                // Auto stop after 5 seconds to not annoy
                Handler(Looper.getMainLooper()).postDelayed({
                    if (ringtone?.isPlaying == true) {
                        ringtone.stop()
                    }
                }, 5000)
            } catch (e: Exception) {
                playSuccessMelody()
            }
        } else {
            playSuccessMelody()
        }
        if (playHaptic) {
            val vibrator = getVibrator(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 200, 100, 300)
                val amplitudes = intArrayOf(0, VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 200, 100, 300), -1)
            }
        }
    }

    fun playSuccessMelody() {
        CoroutineScope(Dispatchers.Default).launch {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 100)
            delay(150)
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 150)
            delay(200)
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_INCALL_LITE, 300)
        }
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}
