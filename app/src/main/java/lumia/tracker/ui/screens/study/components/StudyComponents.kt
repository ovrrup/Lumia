package lumia.tracker.ui.screens.study.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.components.ScholarCardDefaults
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.theme.bouncyClick

/**
 * MetricSummaryTile - Compact glassmorphic metric summary card with prominent headline value,
 * streamlined label typography, and subtle frosted border highlights.
 * Deduplicates repeated metric counters across SelfStudy and Analytics screens.
 */
@ValueScore(
    score = 75,
    importance = Importance.HIGH,
    description = "Glassmorphic metric summary tile with bold headline value and frosted border highlight",
    category = "Study"
)
@Composable
fun MetricSummaryTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.primary,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    shape: Shape = ScholarCardDefaults.compactShape,
    containerColor: Color? = null,
    border: BorderStroke? = null,
    onClick: (() -> Unit)? = null
) {
    val isDark = isSystemInDarkTheme()
    val resolvedContainerColor = containerColor ?: ScholarCardDefaults.glassContainerColor(isDark, alpha = if (isDark) 0.50f else 0.65f)
    val resolvedBorder = border ?: ScholarCardDefaults.glassBorder(isDark, accentColor = valueColor)

    val tileModifier = if (onClick != null) {
        modifier
            .clip(shape)
            .bouncyClick(onClick = onClick)
    } else {
        modifier.clip(shape)
    }

    Surface(
        modifier = tileModifier,
        shape = shape,
        color = resolvedContainerColor,
        border = resolvedBorder,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = valueColor,
                letterSpacing = (-0.5).sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.2.sp
            )
        }
    }
}

/**
 * PriorityBadge - CircleShape capsule status pill with colored indicator dot,
 * streamlined label, and frosted border highlight.
 * Deduplicates priority rendering across TaskItemCard, TagsHubScreen, and SelfStudyTab.
 */
@ValueScore(
    score = 65,
    importance = Importance.MEDIUM,
    description = "CircleShape priority indicator capsule pill with color dot and frosted border highlight",
    category = "Study"
)
@Composable
fun PriorityBadge(
    priority: Int,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val isDark = isSystemInDarkTheme()
    val (pText, pTint) = when (priority) {
        2 -> "High" to MaterialTheme.colorScheme.error
        1 -> (if (compact) "Med" else "Medium") to MaterialTheme.colorScheme.secondary
        else -> "Low" to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = CircleShape,
        color = pTint.copy(alpha = if (isDark) 0.16f else 0.10f),
        border = BorderStroke(1.dp, pTint.copy(alpha = if (isDark) 0.35f else 0.25f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (compact) 7.dp else 9.dp,
                vertical = if (compact) 2.dp else 3.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(if (compact) 4.dp else 5.dp)
                    .background(pTint, CircleShape)
            )
            Text(
                text = pText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = pTint
            )
        }
    }
}

/**
 * ChartContainerCard - Standardized card container wrapper for analytical charts and visualizations.
 * Built with subtle glassmorphism and frosted border highlights.
 * Deduplicates repeated chart card wrapping logic across AnalyticsTab.
 */
@ValueScore(
    score = 75,
    importance = Importance.HIGH,
    description = "Glassmorphic card container with frosted border highlight for analytic charts",
    category = "Analytics"
)
@Composable
fun ChartContainerCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    shape: Shape = ScholarCardDefaults.shape,
    containerColor: Color? = null,
    border: BorderStroke? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    headerTrailing: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val resolvedBorder = border ?: ScholarCardDefaults.glassBorder(isDark)

    ScholarCard(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        containerColor = containerColor,
        border = resolvedBorder,
        glassmorphic = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding)
        ) {
            if (title != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (subtitle != null) 2.dp else 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (headerTrailing != null) {
                        headerTrailing()
                    }
                }
            }
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            content()
        }
    }
}

/**
 * StatMetricColumn - Vertical numerical metric column with streamlined typography.
 * Used inside analytical cards for streaks, test summaries, and quick metrics.
 */
@ValueScore(
    score = 50,
    importance = Importance.MEDIUM,
    description = "Vertical metric column with streamlined header label and bold numerical display",
    category = "Analytics"
)
@Composable
fun StatMetricColumn(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.3.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = valueColor,
            letterSpacing = (-0.5).sp
        )
    }
}

/**
 * StudyCapsuleFilterChip - Interactive CircleShape capsule pill filter chip for categories,
 * priorities, or topics with frosted glass border highlight and tactile bounce feedback.
 */
@ValueScore(
    score = 70,
    importance = Importance.MEDIUM,
    description = "Interactive CircleShape capsule filter chip with frosted border highlight",
    category = "Study"
)
@Composable
fun StudyCapsuleFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    leadingDotColor: Color? = null,
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
            if (leadingDotColor != null) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(leadingDotColor, CircleShape)
                )
            } else if (icon != null) {
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
 * StudyCapsuleProgressBar - Sleek capsule-shaped progress bar with frosted container track
 * and optional value badge pill in CircleShape geometry.
 */
@ValueScore(
    score = 70,
    importance = Importance.MEDIUM,
    description = "Capsule progress indicator with frosted track and optional value pill",
    category = "Study"
)
@Composable
fun StudyCapsuleProgressBar(
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
        animateFloatAsState(targetValue = clampedProgress, label = "studyCapsuleProgress")
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
 * StudyStatusCapsule - Versatile CircleShape capsule status indicator with colored dot or icon
 * and frosted border highlight.
 */
@ValueScore(
    score = 60,
    importance = Importance.MEDIUM,
    description = "Versatile CircleShape capsule status pill with colored dot and frosted border highlight",
    category = "Study"
)
@Composable
fun StudyStatusCapsule(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    icon: ImageVector? = null,
    showDot: Boolean = true
) {
    Surface(
        shape = CircleShape,
        color = color.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.22f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = color
                )
            } else if (showDot) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(color, CircleShape)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

