package lumia.tracker.sync.security

import java.nio.ByteBuffer
import kotlin.math.abs

/**
 * Short Authentication String (SAS) derivation for out-of-band MITM (Man-In-The-Middle) verification.
 * Derives a human-verifiable numeric code and phonetic word sequence from the HKDF-derived master secret.
 */
object SasVerification {

    private val SAS_WORD_DICTIONARY = listOf(
        "ALPHA", "BRAVO", "CHARLIE", "DELTA",
        "ECHO", "FOXTROT", "GOLF", "HOTEL",
        "INDIA", "JULIET", "KILO", "LIMA",
        "MIKE", "NOVEMBER", "OSCAR", "PAPA",
        "QUEBEC", "ROMEO", "SIERRA", "TANGO",
        "UNIFORM", "VICTOR", "WHISKEY", "XRAY",
        "YANKEE", "ZULU", "APEX", "BEACON",
        "CIPHER", "ENCLAVE", "FALCON", "ZENITH"
    )

    data class SasPayload(
        val numericCode: String,        // e.g. "849 201"
        val rawNumeric: Int,            // e.g. 849201
        val wordTokens: List<String>,   // e.g. ["ALPHA", "DELTA", "FALCON", "ZENITH"]
        val wordsSummary: String        // e.g. "ALPHA • DELTA • FALCON • ZENITH"
    )

    /**
     * Derives a Short Authentication String (SAS) from the HKDF-derived SAS key material.
     * Uses 8 bytes from the HKDF output to deterministically create both a 6-digit number
     * and a 4-word phonetic token sequence without emojis.
     */
    fun deriveSas(sasKeyMaterial: ByteArray): SasPayload {
        require(sasKeyMaterial.size >= 8) { "SAS key material must be at least 8 bytes" }

        val buffer = ByteBuffer.wrap(sasKeyMaterial)
        val num1 = abs(buffer.int)
        val num2 = abs(buffer.int)

        // 6-digit code between 100000 and 999999
        val rawCode = (num1 % 900000) + 100000
        val formattedCode = "${rawCode.toString().take(3)} ${rawCode.toString().takeLast(3)}"

        // 4 phonetic words selected from the 32-word dictionary (5 bits per word, 20 bits total)
        val idx1 = (num2 and 0x1F) % SAS_WORD_DICTIONARY.size
        val idx2 = ((num2 ushr 5) and 0x1F) % SAS_WORD_DICTIONARY.size
        val idx3 = ((num2 ushr 10) and 0x1F) % SAS_WORD_DICTIONARY.size
        val idx4 = ((num2 ushr 15) and 0x1F) % SAS_WORD_DICTIONARY.size

        val words = listOf(
            SAS_WORD_DICTIONARY[idx1],
            SAS_WORD_DICTIONARY[idx2],
            SAS_WORD_DICTIONARY[idx3],
            SAS_WORD_DICTIONARY[idx4]
        )

        val summary = words.joinToString(" • ")

        return SasPayload(
            numericCode = formattedCode,
            rawNumeric = rawCode,
            wordTokens = words,
            wordsSummary = summary
        )
    }
}
