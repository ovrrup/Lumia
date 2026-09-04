package lumia.tracker.sync.qr

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Pure Kotlin QR Code Matrix generator with standard Byte encoding and Reed-Solomon error correction.
 * Operates 100% offline without external heavy native libraries.
 */
object QrCodeGenerator {

    fun encodeToMatrix(content: String): Array<BooleanArray> {
        val bytes = content.toByteArray(Charsets.UTF_8)
        val version = selectVersion(bytes.size)
        val matrixSize = 21 + (version - 1) * 4
        val matrix = Array(matrixSize) { BooleanArray(matrixSize) }
        val reserved = Array(matrixSize) { BooleanArray(matrixSize) }

        // 1. Finder patterns (top-left, top-right, bottom-left)
        placeFinderPattern(matrix, reserved, 0, 0)
        placeFinderPattern(matrix, reserved, matrixSize - 7, 0)
        placeFinderPattern(matrix, reserved, 0, matrixSize - 7)

        // 2. Separators around finders
        placeSeparators(reserved, matrixSize)

        // 3. Timing patterns
        for (i in 8 until matrixSize - 8) {
            val bit = (i % 2 == 0)
            if (!reserved[6][i]) { matrix[6][i] = bit; reserved[6][i] = true }
            if (!reserved[i][6]) { matrix[i][6] = bit; reserved[i][6] = true }
        }

        // 4. Dark module
        matrix[8][4 * version + 9] = true
        reserved[8][4 * version + 9] = true

        // 5. Reserve format info areas
        for (i in 0..8) {
            reserved[8][i] = true; reserved[i][8] = true
            reserved[8][matrixSize - 1 - i] = true; reserved[matrixSize - 1 - i][8] = true
        }

        // 6. Encode data stream
        val dataCodewords = encodeByteData(bytes, version)
        val ecCodewords = generateEC(dataCodewords, getECCapacity(version))
        val allCodewords = dataCodewords + ecCodewords

        // 7. Place data bits (zigzag upward/downward)
        placeDataBits(matrix, reserved, allCodewords, matrixSize)

        // 8. Apply mask pattern (pattern 0: (row + col) % 2 == 0) and write format info
        applyMaskAndFormat(matrix, reserved, matrixSize)

        return matrix
    }

    private fun selectVersion(byteCount: Int): Int = when {
        byteCount <= 14 -> 1
        byteCount <= 26 -> 2
        byteCount <= 42 -> 3
        byteCount <= 62 -> 4
        byteCount <= 84 -> 5
        byteCount <= 106 -> 6
        byteCount <= 122 -> 7
        byteCount <= 152 -> 8
        byteCount <= 180 -> 9
        byteCount <= 213 -> 10
        byteCount <= 251 -> 11
        byteCount <= 287 -> 12
        byteCount <= 331 -> 13
        byteCount <= 362 -> 14
        byteCount <= 412 -> 15
        byteCount <= 480 -> 17
        byteCount <= 580 -> 20
        byteCount <= 750 -> 25
        else -> 30
    }

    private fun getDataCapacity(version: Int): Int = when (version) {
        1 -> 19; 2 -> 34; 3 -> 55; 4 -> 80; 5 -> 108
        6 -> 136; 7 -> 156; 8 -> 194; 9 -> 232; 10 -> 274
        11 -> 324; 12 -> 370; 13 -> 428; 14 -> 461; 15 -> 523
        17 -> 647; 20 -> 858; 25 -> 1220; else -> 1400
    }

    private fun getECCapacity(version: Int): Int = when (version) {
        1 -> 7; 2 -> 10; 3 -> 15; 4 -> 20; 5 -> 26
        6 -> 36; 7 -> 40; 8 -> 48; 9 -> 60; 10 -> 72
        11 -> 80; 12 -> 96; 13 -> 104; 14 -> 120; 15 -> 132
        17 -> 168; 20 -> 224; 25 -> 320; else -> 400
    }

    private fun placeFinderPattern(matrix: Array<BooleanArray>, reserved: Array<BooleanArray>, r: Int, c: Int) {
        for (dr in 0..6) {
            for (dc in 0..6) {
                val isBorder = dr == 0 || dr == 6 || dc == 0 || dc == 6
                val isCenter = dr in 2..4 && dc in 2..4
                matrix[r + dr][c + dc] = isBorder || isCenter
                reserved[r + dr][c + dc] = true
            }
        }
    }

    private fun placeSeparators(reserved: Array<BooleanArray>, size: Int) {
        for (i in 0..7) {
            if (i < size && 7 < size) {
                reserved[i][7] = true; reserved[7][i] = true
                reserved[size - 1 - i][7] = true; reserved[size - 8][i] = true
                reserved[i][size - 8] = true; reserved[7][size - 1 - i] = true
            }
        }
    }

    private fun encodeByteData(bytes: ByteArray, version: Int): IntArray {
        val bits = mutableListOf<Int>()
        // Mode indicator for Byte: 0100
        bits.addAll(listOf(0, 1, 0, 0))

        // Character count indicator (8 bits for versions 1-9, 16 bits for 10+)
        val countBits = if (version <= 9) 8 else 16
        for (i in countBits - 1 downTo 0) {
            bits.add((bytes.size shr i) and 1)
        }

        // Data bytes
        for (b in bytes) {
            val v = b.toInt() and 0xFF
            for (i in 7 downTo 0) {
                bits.add((v shr i) and 1)
            }
        }

        // Terminator (up to 4 zeros)
        val capBits = getDataCapacity(version) * 8
        val termLen = minOf(4, capBits - bits.size)
        repeat(termLen) { bits.add(0) }

        // Pad to byte boundary
        while (bits.size % 8 != 0) bits.add(0)

        // Pad codewords (0xEC, 0x11 alternating)
        val padBytes = intArrayOf(0xEC, 0x11)
        var padIdx = 0
        while (bits.size < capBits) {
            val pad = padBytes[padIdx % 2]
            for (i in 7 downTo 0) bits.add((pad shr i) and 1)
            padIdx++
        }

        val codewords = IntArray(bits.size / 8)
        for (i in codewords.indices) {
            var v = 0
            for (b in 0..7) v = (v shl 1) or bits[i * 8 + b]
            codewords[i] = v
        }
        return codewords
    }

    // GF(256) Reed-Solomon generator
    private fun generateEC(data: IntArray, ecCount: Int): IntArray {
        val gen = rsGeneratorPoly(ecCount)
        val msg = IntArray(data.size + ecCount)
        System.arraycopy(data, 0, msg, 0, data.size)
        for (i in data.indices) {
            val coef = msg[i]
            if (coef != 0) {
                for (j in gen.indices) {
                    msg[i + j] = msg[i + j] xor gfMul(gen[j], coef)
                }
            }
        }
        val ec = IntArray(ecCount)
        System.arraycopy(msg, data.size, ec, 0, ecCount)
        return ec
    }

    private val gfExp = IntArray(512)
    private val gfLog = IntArray(256)

    init {
        var x = 1
        for (i in 0..254) {
            gfExp[i] = x
            gfExp[i + 255] = x
            gfLog[x] = i
            x = (x shl 1)
            if (x >= 256) x = x xor 0x11D
        }
    }

    private fun gfMul(a: Int, b: Int): Int {
        if (a == 0 || b == 0) return 0
        return gfExp[gfLog[a] + gfLog[b]]
    }

    private fun rsGeneratorPoly(ecCount: Int): IntArray {
        var poly = intArrayOf(1)
        for (i in 0 until ecCount) {
            val factor = intArrayOf(1, gfExp[i])
            val next = IntArray(poly.size + 1)
            for (j in poly.indices) {
                next[j] = next[j] xor gfMul(poly[j], factor[0])
                next[j + 1] = next[j + 1] xor gfMul(poly[j], factor[1])
            }
            poly = next
        }
        return poly
    }

    private fun placeDataBits(matrix: Array<BooleanArray>, reserved: Array<BooleanArray>, codewords: IntArray, size: Int) {
        val bits = mutableListOf<Boolean>()
        for (cw in codewords) {
            for (i in 7 downTo 0) bits.add(((cw shr i) and 1) == 1)
        }
        var bitIdx = 0
        var col = size - 1
        var upward = true

        while (col > 0) {
            if (col == 6) col-- // Skip timing column
            val rows = if (upward) (size - 1 downTo 0) else (0 until size)
            for (row in rows) {
                for (c in intArrayOf(col, col - 1)) {
                    if (!reserved[row][c]) {
                        matrix[row][c] = if (bitIdx < bits.size) bits[bitIdx++] else false
                    }
                }
            }
            upward = !upward
            col -= 2
        }
    }

    private fun applyMaskAndFormat(matrix: Array<BooleanArray>, reserved: Array<BooleanArray>, size: Int) {
        // Mask 0: (row + col) % 2 == 0
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (!reserved[r][c] && (r + c) % 2 == 0) {
                    matrix[r][c] = !matrix[r][c]
                }
            }
        }
        // Write standard L-EC format info with mask 0
        val formatBits = intArrayOf(1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 0, 0, 1, 0, 0)
        var bit = 0
        for (i in 0..8) {
            if (i != 6) {
                matrix[8][i] = formatBits[bit] == 1
                matrix[size - 1 - i][8] = formatBits[bit] == 1
                bit++
            }
        }
    }
}

@Composable
fun QrCodeCanvas(
    content: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    contentColor: Color = Color.Black
) {
    val matrix = remember(content) {
        try {
            QrCodeGenerator.encodeToMatrix(content)
        } catch (e: Exception) {
            null
        }
    }

    if (matrix != null) {
        val size = matrix.size
        Box(
            modifier = modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(backgroundColor)
                .padding(12.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cellSize = this.size.width / size
                val cornerRadius = CornerRadius(cellSize * 0.25f, cellSize * 0.25f)
                for (r in 0 until size) {
                    for (c in 0 until size) {
                        if (matrix[r][c]) {
                            drawRoundRect(
                                color = contentColor,
                                topLeft = Offset(c * cellSize, r * cellSize),
                                size = Size(cellSize, cellSize),
                                cornerRadius = cornerRadius
                            )
                        }
                    }
                }
            }
        }
    }
}
