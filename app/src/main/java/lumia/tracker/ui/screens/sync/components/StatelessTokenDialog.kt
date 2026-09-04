package lumia.tracker.ui.screens.sync.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Dialpad
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import lumia.tracker.sync.qr.QrCodeCanvas
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.BouncyOutlinedButton
import lumia.tracker.ui.components.ScholarCard
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.CRC32

/**
 * In-memory and local-network registry for mapping 6-digit pairing PINs
 * to full connection tokens. Enables zero-database frictionless pairing.
 */
object PairingPinRegistry {
    private const val PAIRING_MAGIC = 0x4C554D50 // "LUMP" (Lumia Pairing)
    private const val PAIRING_PORT = 51822

    private val pinToTokenMap = ConcurrentHashMap<String, String>()
    private val tokenToPinMap = ConcurrentHashMap<String, String>()

    fun computePin(token: String): String {
        if (token.isBlank()) return "000000"
        val crc = CRC32()
        crc.update(token.toByteArray(Charsets.UTF_8))
        val value = (crc.value % 900000L) + 100000L
        return value.toString()
    }

    fun register(token: String): String {
        if (token.isBlank()) return "------"
        val existing = tokenToPinMap[token]
        if (existing != null) return existing
        val pin = computePin(token)
        pinToTokenMap[pin] = token
        tokenToPinMap[token] = pin
        return pin
    }

    fun resolve(pin: String): String? {
        val clean = pin.trim()
        return pinToTokenMap[clean]
    }

    fun store(pin: String, token: String) {
        val cleanPin = pin.trim()
        val cleanToken = token.trim()
        if (cleanPin.length == 6 && cleanToken.isNotBlank()) {
            pinToTokenMap[cleanPin] = cleanToken
            tokenToPinMap[cleanToken] = cleanPin
        }
    }
}

/**
 * Seamless, frictionless pairing dialog featuring:
 * - Tab 1: "Show QR Code" with clean pure Kotlin QrCodeCanvas and a 6-digit easy pairing PIN.
 * - Tab 2: "Enter Code / Scan" with a clean 6-digit numeric input box or quick paste,
 *   without forcing users to inspect huge base64 tokens.
 */
@Composable
fun SimplePairingDialog(
    myToken: String,
    onConnectWithToken: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var inputPin by remember { mutableStateOf("") }
    var pastedFullToken by remember { mutableStateOf("") }

    val currentPin = remember(myToken) {
        if (myToken.isNotBlank()) PairingPinRegistry.register(myToken) else "------"
    }

    // Broadcast pairing PIN on local network while dialog is open and token is active
    LaunchedEffect(myToken, currentPin) {
        if (myToken.isNotBlank() && currentPin.length == 6) {
            withContext(Dispatchers.IO) {
                val pinBytes = currentPin.toByteArray(Charsets.UTF_8)
                val tokenBytes = myToken.toByteArray(Charsets.UTF_8)
                val bb = ByteBuffer.allocate(4 + 2 + pinBytes.size + 4 + tokenBytes.size)
                bb.putInt(0x4C554D50)
                bb.putShort(pinBytes.size.toShort())
                bb.put(pinBytes)
                bb.putInt(tokenBytes.size)
                bb.put(tokenBytes)
                val payload = bb.array()

                while (isActive) {
                    try {
                        val broadcastAddress = getBroadcastAddress() ?: InetAddress.getByName("255.255.255.255")
                        val socket = DatagramSocket()
                        socket.broadcast = true
                        val packet = DatagramPacket(payload, payload.size, broadcastAddress, 51822)
                        socket.send(packet)
                        socket.close()
                    } catch (e: Exception) {
                        // Suppress socket tick errors
                    }
                    delay(1500L)
                }
            }
        }
    }

    // Listen for peer pairing broadcasts on local network
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            var socket: DatagramSocket? = null
            try {
                socket = DatagramSocket(null).apply {
                    reuseAddress = true
                    bind(InetSocketAddress(51822))
                    broadcast = true
                }
                val buffer = ByteArray(2048)
                while (isActive) {
                    val packet = DatagramPacket(buffer, buffer.size)
                    socket.receive(packet)
                    if (packet.length >= 10) {
                        val bb = ByteBuffer.wrap(packet.data, 0, packet.length)
                        val magic = bb.int
                        if (magic == 0x4C554D50) {
                            val pinLen = bb.short.toInt() and 0xFFFF
                            if (pinLen in 1..8 && bb.remaining() >= pinLen + 4) {
                                val pinBytes = ByteArray(pinLen)
                                bb.get(pinBytes)
                                val pin = String(pinBytes, Charsets.UTF_8)

                                val tokenLen = bb.int
                                if (tokenLen in 1..bb.remaining()) {
                                    val tokenBytes = ByteArray(tokenLen)
                                    bb.get(tokenBytes)
                                    val token = String(tokenBytes, Charsets.UTF_8)
                                    PairingPinRegistry.store(pin, token)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Socket closed or unavailable
            } finally {
                try {
                    socket?.close()
                } catch (e: Exception) {
                    // Suppress close error
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pair Device",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    BouncyIconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Scan the QR code on your second device or enter the 6-digit PIN to connect immediately.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tab Switcher
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .padding(2.dp)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.QrCode,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Show QR Code", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Dialpad,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Enter Code / Scan", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    // Tab 1: Show QR Code & 6-digit easy pairing PIN
                    if (myToken.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            QrCodeCanvas(
                                content = myToken,
                                modifier = Modifier.size(190.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 6-digit PIN ScholarCard
                        ScholarCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "PAIRING PIN",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.2.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    currentPin.forEach { char ->
                                        Surface(
                                            modifier = Modifier.size(width = 38.dp, height = 44.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            border = BorderStroke(
                                                1.dp,
                                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                            )
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = char.toString(),
                                                    style = MaterialTheme.typography.titleLarge,
                                                    fontWeight = FontWeight.Black,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            BouncyOutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Lumia Pairing PIN", currentPin)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "PIN copied to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy PIN", fontWeight = FontWeight.Bold)
                            }

                            BouncyButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Lumia Pairing Code", myToken)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Pairing code copied to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy Code", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Preparing connection code...",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Setting up secure pairing session",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    // Tab 2: Enter Code / Scan with clean 6-digit numeric input box or quick paste
                    Text(
                        text = "Enter the 6-digit PIN shown on your second device, or tap Quick Paste to connect.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 6-digit numeric input box
                    SixDigitPinInput(
                        pin = inputPin,
                        onPinChange = {
                            inputPin = it
                            if (pastedFullToken.isNotBlank()) {
                                pastedFullToken = ""
                            }
                        },
                        onDone = {
                            if (inputPin.length == 6) {
                                val resolved = PairingPinRegistry.resolve(inputPin)
                                if (resolved != null) {
                                    onConnectWithToken(resolved)
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Clean status card if full token was pasted or PIN is recognized
                    if (pastedFullToken.isNotBlank()) {
                        ScholarCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Pairing Code Ready",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Code loaded from clipboard",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                BouncyIconButton(
                                    onClick = { pastedFullToken = "" },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Clear",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    } else if (inputPin.length == 6) {
                        val resolvedToken = PairingPinRegistry.resolve(inputPin)
                        ScholarCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            containerColor = if (resolvedToken != null) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            },
                            border = BorderStroke(
                                1.dp,
                                if (resolvedToken != null) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = if (resolvedToken != null) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (resolvedToken != null) {
                                        "Device found on local network"
                                    } else {
                                        "Searching local network for PIN..."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Quick Paste button
                    BouncyOutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = clipboard.primaryClip
                            if (clip != null && clip.itemCount > 0) {
                                val text = clip.getItemAt(0).text?.toString()?.trim().orEmpty()
                                if (text.isNotBlank()) {
                                    val numericOnly = text.filter { it.isDigit() }
                                    if (numericOnly.length == 6 && text.length == 6) {
                                        inputPin = numericOnly
                                        pastedFullToken = ""
                                        val resolved = PairingPinRegistry.resolve(numericOnly)
                                        if (resolved != null) {
                                            Toast.makeText(context, "PIN matched with local device", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Pasted 6-digit PIN", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        pastedFullToken = text
                                        inputPin = ""
                                        Toast.makeText(context, "Pairing code pasted from clipboard", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Rounded.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Quick Paste", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Connect button
                    val isReadyToConnect = pastedFullToken.isNotBlank() || inputPin.length == 6
                    BouncyButton(
                        onClick = {
                            if (pastedFullToken.isNotBlank()) {
                                onConnectWithToken(pastedFullToken.trim())
                            } else if (inputPin.length == 6) {
                                val resolved = PairingPinRegistry.resolve(inputPin)
                                if (resolved != null) {
                                    onConnectWithToken(resolved)
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Device not found on local network. Ensure both devices are on the same Wi-Fi or scan the QR code.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isReadyToConnect,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Rounded.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Connect", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
    }
}

/**
 * Backward-compatible wrapper delegating to SimplePairingDialog.
 */
@Composable
fun StatelessTokenDialog(
    myToken: String,
    onConnectWithToken: (String) -> Unit,
    onDismiss: () -> Unit
) {
    SimplePairingDialog(
        myToken = myToken,
        onConnectWithToken = onConnectWithToken,
        onDismiss = onDismiss
    )
}

@Composable
private fun SixDigitPinInput(
    pin: String,
    onPinChange: (String) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }

    BasicTextField(
        value = pin,
        onValueChange = { value ->
            val filtered = value.filter { it.isDigit() }.take(6)
            onPinChange(filtered)
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { onDone() }
        ),
        singleLine = true,
        textStyle = TextStyle(color = Color.Transparent),
        cursorBrush = SolidColor(Color.Transparent),
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        decorationBox = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (index in 0 until 6) {
                    val digit = pin.getOrNull(index)?.toString().orEmpty()
                    val isCurrent = pin.length == index || (index == 5 && pin.length == 6)
                    val hasDigit = digit.isNotEmpty()
                    val borderColor = when {
                        hasDigit -> MaterialTheme.colorScheme.primary
                        isCurrent -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    }
                    val surfaceColor = if (hasDigit) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    }
                    Surface(
                        modifier = Modifier.size(width = 44.dp, height = 52.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = surfaceColor,
                        border = BorderStroke(
                            width = if (isCurrent || hasDigit) 1.5.dp else 1.dp,
                            color = borderColor
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (hasDigit) {
                                Text(
                                    text = digit,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            } else {
                                Text(
                                    text = "-",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

private fun getBroadcastAddress(): InetAddress? {
    return try {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val iface = interfaces.nextElement()
            if (iface.isLoopback || !iface.isUp) continue
            for (interfaceAddress in iface.interfaceAddresses) {
                val broadcast = interfaceAddress.broadcast
                if (broadcast != null) {
                    return broadcast
                }
            }
        }
        InetAddress.getByName("255.255.255.255")
    } catch (e: Exception) {
        null
    }
}
