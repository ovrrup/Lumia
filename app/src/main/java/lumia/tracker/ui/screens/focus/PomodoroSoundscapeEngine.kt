package lumia.tracker.ui.screens.focus

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.*
import java.util.Random
import kotlin.math.sin

/**
 * SoundscapeType - Built-in offline soothing audio soundscapes for deep focus.
 */
enum class SoundscapeType(val title: String, val emoji: String, val description: String) {
    OFF("None", "🔇", "Silence"),
    WHITE_NOISE("White Noise", "📻", "Smooth broadband focus mask"),
    RAIN("Gentle Rain", "🌧️", "Soothing stochastic rainfall"),
    DEEP_WAVES("Alpha Waves", "🌊", "10Hz binaural relaxation waves"),
    CAFE_MURMUR("Coffeehouse", "☕", "Subtle ambient murmur")
}

/**
 * PomodoroSoundscapeEngine - Pure Kotlin, offline, zero-dependency ambient audio synthesizer.
 * Generates continuous audio streams using Android AudioTrack without requiring any external MP3 files.
 */
object PomodoroSoundscapeEngine {
    private var audioTrack: AudioTrack? = null
    private var synthJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    var currentSoundscape: SoundscapeType = SoundscapeType.OFF
        private set

    var volume: Float = 0.5f
        set(value) {
            field = value.coerceIn(0f, 1f)
            audioTrack?.setVolume(field)
        }

    fun isPlaying(): Boolean = currentSoundscape != SoundscapeType.OFF && synthJob?.isActive == true

    /**
     * Start playing selected ambient soundscape.
     */
    fun play(type: SoundscapeType) {
        stop()
        if (type == SoundscapeType.OFF) return
        currentSoundscape = type

        synthJob = scope.launch {
            val sampleRate = 22050
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(4096)

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack = track
            track.setVolume(volume)
            track.play()

            val buffer = ShortArray(bufferSize / 2)
            val random = Random()
            var phase = 0.0
            var lastSample = 0.0

            try {
                while (isActive) {
                    when (type) {
                        SoundscapeType.WHITE_NOISE -> {
                            for (i in buffer.indices) {
                                // Filtered pink/white noise
                                val raw = (random.nextGaussian() * 4000).toInt()
                                buffer[i] = raw.coerceIn(-32768, 32767).toShort()
                            }
                        }
                        SoundscapeType.RAIN -> {
                            for (i in buffer.indices) {
                                // Rain drops simulation (brown noise + random droplets)
                                val white = random.nextGaussian() * 2500
                                lastSample = (lastSample * 0.95) + (white * 0.05)
                                val drop = if (random.nextInt(100) < 2) (random.nextDouble() * 5000).toInt() else 0
                                val sample = (lastSample + drop).toInt()
                                buffer[i] = sample.coerceIn(-32768, 32767).toShort()
                            }
                        }
                        SoundscapeType.DEEP_WAVES -> {
                            val freq = 136.1 // Ohm / Alpha relaxation base tone
                            val beatFreq = 10.0 // 10Hz Alpha wave modulation
                            for (i in buffer.indices) {
                                phase += 2 * Math.PI * freq / sampleRate
                                val mod = (sin(2 * Math.PI * beatFreq * (phase / freq)) + 1.0) * 0.5
                                val sample = (sin(phase) * 6000 * mod).toInt()
                                buffer[i] = sample.coerceIn(-32768, 32767).toShort()
                            }
                        }
                        SoundscapeType.CAFE_MURMUR -> {
                            for (i in buffer.indices) {
                                val low = random.nextGaussian() * 1800
                                lastSample = (lastSample * 0.98) + (low * 0.02)
                                buffer[i] = lastSample.toInt().coerceIn(-32768, 32767).toShort()
                            }
                        }
                        SoundscapeType.OFF -> break
                    }
                    track.write(buffer, 0, buffer.size)
                }
            } catch (e: Exception) {
                // Audio streaming cancelled or stopped
            } finally {
                try {
                    track.stop()
                    track.release()
                } catch (ignored: Exception) {}
            }
        }
    }

    /**
     * Stop synthesized audio playback and release audio hardware resources.
     */
    fun stop() {
        currentSoundscape = SoundscapeType.OFF
        synthJob?.cancel()
        synthJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (ignored: Exception) {}
        audioTrack = null
    }
}
