package lumia.tracker.sync.security

import java.nio.ByteBuffer
import kotlin.math.abs

/**
 * Short Authentication String (SAS) derivation for out-of-band MITM (Man-In-The-Middle) verification.
 * Derives a human-verifiable numeric code and emoji word sequence from the HKDF-derived master secret.
 */
object SasVerification {

    private val SAS_EMOJI_WORDS = listOf(
        "🦁 Lion", "⚡ Bolt", "🛡️ Shield", "🚀 Rocket",
        "💎 Gem", "🔥 Flame", "👑 Crown", "⚓ Anchor",
        "🌊 Ocean", "🪐 Planet", "🦅 Falcon", "🐺 Wolf",
        "🌲 Pine", "🌈 Prism", "🎯 Target", "🔑 Key",
        "🌟 Star", "🧭 Compass", "🔔 Bell", "🍀 Clover",
        "⚔️ Sword", "🦉 Owl", "🌙 Moon", "☀️ Sun",
        "🪐 Orbit", "🧩 Puzzle", "🏰 Castle", "🌋 Volcano",
        "🏎️ Swift", "🔮 Crystal", "⛵ Sail", "🪐 Zenith"
    )

    data class SasPayload(
        val numericCode: String,      // e.g. "849 201"
        val rawNumeric: Int,          // e.g. 849201
        val emojiWords: List<String>, // e.g. ["🦁 Lion", "⚡ Bolt", "🛡️ Shield", "🚀 Rocket"]
        val emojiSummary: String      // e.g. "🦁 ⚡ 🛡️ 🚀"
    )

    /**
     * Derives a Short Authentication String (SAS) from the HKDF-derived SAS key material.
     * Uses 8 bytes from the HKDF output to deterministically create both a 6-digit number
     * and a 4-word emoji sequence.
     */
    fun deriveSas(sasKeyMaterial: ByteArray): SasPayload {
        require(sasKeyMaterial.size >= 8) { "SAS key material must be at least 8 bytes" }

        val buffer = ByteBuffer.wrap(sasKeyMaterial)
        val num1 = abs(buffer.int)
        val num2 = abs(buffer.int)

        // 6-digit code between 100000 and 999999
        val rawCode = (num1 % 900000) + 100000
        val formattedCode = "${rawCode.toString().take(3)} ${rawCode.toString().takeLast(3)}"

        // 4 emoji words selected from the 32-word dictionary (5 bits per word, 20 bits total)
        val idx1 = (num2 and 0x1F) % SAS_EMOJI_WORDS.size
        val idx2 = ((num2 ushr 5) and 0x1F) % SAS_EMOJI_WORDS.size
        val idx3 = ((num2 ushr 10) and 0x1F) % SAS_EMOJI_WORDS.size
        val idx4 = ((num2 ushr 15) and 0x1F) % SAS_EMOJI_WORDS.size

        val words = listOf(
            SAS_EMOJI_WORDS[idx1],
            SAS_EMOJI_WORDS[idx2],
            SAS_EMOJI_WORDS[idx3],
            SAS_EMOJI_WORDS[idx4]
        )

        val emojisOnly = words.joinToString(" ") { it.substringBefore(" ") }

        return SasPayload(
            numericCode = formattedCode,
            rawNumeric = rawCode,
            emojiWords = words,
            emojiSummary = emojisOnly
        )
    }
}
