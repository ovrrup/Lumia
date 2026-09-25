package lumia.tracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lumia.tracker.ui.theme.bouncyScale
import lumia.tracker.viewmodel.ScholarViewModel

/**
 * Modernized Safety PIN Dialog component that presents conflicts, recommendations,
 * and security verification with circular glass capsules for PIN input circles and numpad buttons.
 */
@Composable
fun SafetyPinDialog(viewModel: ScholarViewModel) {
    val safetyPinDialogData by viewModel.safetyPinDialogData.collectAsStateWithLifecycle()

    safetyPinDialogData?.let { data ->
        AlertDialog(
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 0.dp,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f),
            icon = {
                // Circular glass capsule for alert icon
                Surface(
                    shape = CircleShape,
                    color = (if (data.isConflict) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer).copy(alpha = 0.35f),
                    border = BorderStroke(
                        1.dp,
                        (if (data.isConflict) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary).copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (data.isConflict) Icons.Rounded.Warning else Icons.Rounded.Info,
                            contentDescription = null,
                            tint = if (data.isConflict) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = data.title,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = data.description,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            },
            onDismissRequest = { data.onIgnore() },
            confirmButton = {
                BouncyButton(
                    onClick = data.onConfirm,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (data.isConflict) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        contentColor = if (data.isConflict) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 22.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = if (data.isConflict) "Continue" else "Apply",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                BouncyButton(
                    onClick = data.onIgnore,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(text = if (data.isConflict) "Stop" else "Ignore")
                }
            }
        )
    }
}

/**
 * Modernized PIN input circles rendered as circular glass capsules.
 * Each circle uses CircleShape with smooth transitions and subtle glass borders.
 */
@Composable
fun PinInputCircles(
    pinLength: Int = 4,
    enteredLength: Int,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    errorColor: Color = MaterialTheme.colorScheme.error
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until pinLength) {
            val isFilled = i < enteredLength
            val isCurrent = i == enteredLength

            val targetColor = when {
                isError -> errorColor
                isFilled -> activeColor
                isCurrent -> activeColor.copy(alpha = 0.35f)
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            }
            val animColor by animateColorAsState(targetValue = targetColor, label = "pinCircleColor")

            val targetScale = if (isFilled) 1.15f else if (isCurrent) 1.05f else 1.0f
            val animScale by animateFloatAsState(
                targetValue = targetScale,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "pinCircleScale"
            )

            val borderColor = when {
                isError -> errorColor.copy(alpha = 0.6f)
                isFilled -> activeColor.copy(alpha = 0.8f)
                isCurrent -> activeColor.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            }

            Box(
                modifier = Modifier
                    .size(20.dp)
                    .scale(animScale)
                    .clip(CircleShape)
                    .background(animColor)
                    .border(BorderStroke(1.2.dp, borderColor), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isFilled) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.9f))
                    )
                }
            }
        }
    }
}

/**
 * Modern circular glass capsule numpad button.
 * Uses CircleShape with translucent glass container and subtle border.
 */
@Composable
fun NumpadButton(
    modifier: Modifier = Modifier,
    text: String = "",
    subtext: String = "",
    icon: ImageVector? = null,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = modifier
            .size(72.dp)
            .clip(CircleShape)
            .bouncyScale(interactionSource),
        interactionSource = interactionSource
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (subtext.isNotEmpty()) {
                        Text(
                            text = subtext,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Modernized circular glass capsule numpad keypad.
 */
@Composable
fun PinNumpad(
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
    onClearClick: (() -> Unit)? = null
) {
    val digits = listOf(
        listOf("1" to "", "2" to "ABC", "3" to "DEF"),
        listOf("4" to "GHI", "5" to "JKL", "6" to "MNO"),
        listOf("7" to "PQRS", "8" to "TUV", "9" to "WXYZ")
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        digits.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally)
            ) {
                row.forEach { (digit, subtext) ->
                    NumpadButton(
                        text = digit,
                        subtext = subtext,
                        onClick = { onDigitClick(digit) }
                    )
                }
            }
        }

        // Bottom row: Clear/blank, 0, Backspace
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onClearClick != null) {
                Surface(
                    onClick = onClearClick,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.35f),
                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "C",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.size(72.dp))
            }

            NumpadButton(
                text = "0",
                subtext = "+",
                onClick = { onDigitClick("0") }
            )

            NumpadButton(
                icon = Icons.AutoMirrored.Rounded.Backspace,
                onClick = onDeleteClick
            )
        }
    }
}

/**
 * Modernized standalone Safety PIN Dialog with circular glass capsules for PIN circles and numpad.
 */
@Composable
fun SafetyPinInputDialog(
    title: String = "Safety PIN",
    description: String = "Enter your 4-digit security PIN to proceed",
    pinLength: Int = 4,
    onPinEntered: (String) -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.95f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header Icon Capsule
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Title & Description
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                // PIN Input Circles (Circular glass capsules)
                PinInputCircles(
                    pinLength = pinLength,
                    enteredLength = enteredPin.length,
                    isError = isError,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Numpad Buttons (Circular glass capsules)
                PinNumpad(
                    onDigitClick = { digit ->
                        if (enteredPin.length < pinLength) {
                            val newPin = enteredPin + digit
                            enteredPin = newPin
                            isError = false
                            if (newPin.length == pinLength) {
                                onPinEntered(newPin)
                            }
                        }
                    },
                    onDeleteClick = {
                        if (enteredPin.isNotEmpty()) {
                            enteredPin = enteredPin.dropLast(1)
                            isError = false
                        }
                    },
                    onClearClick = {
                        enteredPin = ""
                        isError = false
                    }
                )

                // Cancel Button - Sleek capsule pill
                BouncyButton(
                    onClick = onDismiss,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(44.dp)
                ) {
                    Text("Cancel", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
