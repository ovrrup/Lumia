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
            try {
                toneGenerator = ToneGenerator(AudioManager.STREAM_SYSTEM, 60)
            } catch (e: Exception) {
                android.util.Log.e("SoundManager", "Failed to initialize ToneGenerator", e)
                toneGenerator = null
            }
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
        if (playSound) {
            // Tactile light analog click that sounds pleasant and satisfies premium tap feel
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_KEYPAD_VOLUME_KEY_LITE, 20)
        }
        if (playHaptic) {
            val vibrator = getVibrator(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(20)
            }
        }
    }

    fun playTransitionSound(context: Context, playSound: Boolean, playHaptic: Boolean) {
        if (playSound) {
            // Satisfying and gentle dual analog micro-ticks for screen navigation transitions
            CoroutineScope(Dispatchers.Default).launch {
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_KEYPAD_VOLUME_KEY_LITE, 15)
                delay(60)
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_KEYPAD_VOLUME_KEY_LITE, 15)
            }
        }
        if (playHaptic) {
            val vibrator = getVibrator(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(10)
            }
        }
    }

    fun playPomodoroStart(context: Context, playSound: Boolean, playHaptic: Boolean) {
        if (playSound) {
            // Beautiful soft alert chimes to start the focus session on an ambient note
            CoroutineScope(Dispatchers.Default).launch {
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 60)
                delay(120)
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_INCALL_LITE, 80)
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
