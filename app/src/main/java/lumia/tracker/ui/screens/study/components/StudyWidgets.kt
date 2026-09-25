package lumia.tracker.ui.screens.study.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.BouncyTextButton
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.components.ScholarCardDefaults
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.theme.bouncyClick

@ValueScore(
    score = 70,
    importance = Importance.MEDIUM,
    description = "Reusable section header with prominent glassmorphic icon badge, action button and optional trailing content",
    category = "Study"
)
@Composable
fun StudySectionHeader(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    onAddClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(iconTint.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.3).sp
            )
        }
        if (trailing != null) {
            trailing()
        } else if (onAddClick != null) {
            Surface(
                shape = CircleShape,
                color = iconTint.copy(alpha = 0.10f),
                border = BorderStroke(1.dp, iconTint.copy(alpha = 0.22f)),
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .bouncyClick(onClick = onAddClick)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add Item",
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@ValueScore(
    score = 70,
    importance = Importance.MEDIUM,
    description = "Glassmorphic empty state card with frosted border highlight, streamlined typography, and capsule action CTA",
    category = "Study"
)
@Composable
fun StudyEmptySectionCard(
    text: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    buttonText: String? = null,
    onClick: (() -> Unit)? = null,
    icon: ImageVector? = null,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    val isDark = isSystemInDarkTheme()
    val cardBorder = ScholarCardDefaults.glassBorder(isDark, accentColor = accentColor.copy(alpha = 0.25f))

    ScholarCard(
        modifier = modifier.fillMaxWidth(),
        shape = ScholarCardDefaults.compactShape,
        border = cardBorder,
        glassmorphic = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(accentColor.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center
                )
            }
            if (buttonText != null && onClick != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.30f)),
                    modifier = Modifier
                        .clip(CircleShape)
                        .bouncyClick(onClick = onClick)
                ) {
                    Text(
                        text = buttonText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
                    )
                }
            }
        }
    }
}

@ValueScore(
    score = 65,
    importance = Importance.LOW,
    description = "CircleShape capsule pill badge for instructor, schedule, and study metadata tags with frosted border highlight",
    category = "Study"
)
@Composable
fun DetailMetaBadge(
    icon: ImageVector,
    text: String,
    tint: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = tint.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.20f)),
        shape = CircleShape
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = tint
            )
        }
    }
}

@ValueScore(
    score = 65,
    importance = Importance.LOW,
    description = "Attendance breakdown summary item featuring a CircleShape capsule counter pill with colored status dot",
    category = "Study"
)
@Composable
fun AttendanceCounterItem(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.10f),
            border = BorderStroke(1.dp, color.copy(alpha = 0.22f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(color, CircleShape)
                )
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = color
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@ValueScore(
    score = 60,
    importance = Importance.MEDIUM,
    description = "Subject and course overview statistic metric item with streamlined typography and capsule detail badge",
    category = "Study"
)
@Composable
fun StudyStatItem(
    label: String,
    count: Int,
    modifier: Modifier = Modifier,
    detail: String? = null,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = tint,
            letterSpacing = (-0.5).sp
        )
        Spacer(modifier = Modifier.height(1.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (detail != null) {
            Spacer(modifier = Modifier.height(3.dp))
            Surface(
                shape = CircleShape,
                color = tint.copy(alpha = 0.10f),
                border = BorderStroke(0.5.dp, tint.copy(alpha = 0.20f))
            ) {
                Text(
                    text = detail,
                    style = MaterialTheme.typography.labelSmall,
                    color = tint,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                )
            }
        }
    }
}

/**
 * StudyFilterChipCapsule - Interactive filter chip formatted as a CircleShape capsule pill
 * with subtle frosted glass highlight borders and selected state indicator.
 */
@ValueScore(
    score = 70,
    importance = Importance.MEDIUM,
    description = "Interactive CircleShape capsule filter chip with frosted border highlight",
    category = "Study"
)
@Composable
fun StudyFilterChipCapsule(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    count: Int? = null
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (selected) {
        accentColor.copy(alpha = if (isDark) 0.20f else 0.14f)
    } else {
        if (isDark) MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.50f)
        else MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
    }
    val borderColor = if (selected) {
        accentColor.copy(alpha = 0.45f)
    } else {
        if (isDark) Color.White.copy(alpha = 0.12f)
        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.30f)
    }

    Surface(
        shape = CircleShape,
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
            .clip(CircleShape)
            .bouncyClick(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = if (selected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (count != null) {
                Surface(
                    shape = CircleShape,
                    color = if (selected) accentColor.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.60f)
                ) {
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

/**
 * StudyProgressBarCapsule - Sleek capsule-shaped progress bar with frosted container track,
 * animated fill, and an optional percentage or status capsule indicator.
 */
@ValueScore(
    score = 70,
    importance = Importance.MEDIUM,
    description = "Capsule progress indicator with frosted track and optional value pill",
    category = "Study"
)
@Composable
fun StudyProgressBarCapsule(
    progress: Float,
    modifier: Modifier = Modifier,
    label: String? = null,
    valueText: String? = null,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    height: Dp = 6.dp,
    animated: Boolean = true
) {
    val clampedProgress = progress.coerceIn(0f, 1f)
    val displayProgress by if (animated) {
        animateFloatAsState(targetValue = clampedProgress, label = "studyProgressCapsule")
    } else {
        remember(clampedProgress) { mutableFloatStateOf(clampedProgress) }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (label != null || valueText != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (label != null) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
                if (valueText != null) {
                    Surface(
                        shape = CircleShape,
                        color = color.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
                    ) {
                        Text(
                            text = valueText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = color,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
        LinearProgressIndicator(
            progress = { displayProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(CircleShape),
            color = color,
            trackColor = trackColor
        )
    }
}

/**
 * StudyStatusIndicatorPill - CircleShape capsule pill for attendance, priority, or activity state.
 * Features a solid status dot, concise label, and frosted border highlight.
 */
@ValueScore(
    score = 60,
    importance = Importance.LOW,
    description = "CircleShape status indicator capsule pill with frosted highlight border",
    category = "Study"
)
@Composable
fun StudyStatusIndicatorPill(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null
) {
    val pillModifier = if (onClick != null) {
        modifier
            .clip(CircleShape)
            .bouncyClick(onClick = onClick)
    } else {
        modifier
    }

    Surface(
        shape = CircleShape,
        color = color.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.22f)),
        modifier = pillModifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(13.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(color, CircleShape)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

