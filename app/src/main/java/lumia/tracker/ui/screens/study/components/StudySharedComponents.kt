package lumia.tracker.ui.screens.study.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.util.getTagColors

/**
 * Reusable dropdown menu action popup for Study item cards (Courses, Subjects, Tasks, etc.).
 * Encapsulates the 3-dot trigger button, expanded state, and standardized Edit / Delete options.
 */
@ValueScore(
    score = 70,
    importance = Importance.MEDIUM,
    description = "Reusable popup menu providing standard Edit and Delete card actions",
    category = "Study"
)
@Composable
fun StudyItemActionMenu(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    editLabel: String = "Edit",
    deleteLabel: String = "Delete",
    editIcon: ImageVector = Icons.Rounded.Edit,
    deleteIcon: ImageVector = Icons.Rounded.Delete
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        BouncyIconButton(
            onClick = { expanded = true },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = "Options",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(editLabel) },
                onClick = {
                    expanded = false
                    onEdit()
                },
                leadingIcon = {
                    Icon(
                        imageVector = editIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )
            DropdownMenuItem(
                text = { Text(deleteLabel) },
                onClick = {
                    expanded = false
                    onDelete()
                },
                leadingIcon = {
                    Icon(
                        imageVector = deleteIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            )
        }
    }
}

/**
 * Single tag badge chip rendered with dynamic background and contrast text colors derived from tag hash.
 */
@ValueScore(
    score = 60,
    importance = Importance.LOW,
    description = "Color-accented tag chip with optional icon and click callback",
    category = "Study"
)
@Composable
fun StudyTagChip(
    tag: String,
    modifier: Modifier = Modifier,
    showIcon: Boolean = true,
    shape: Shape = RoundedCornerShape(6.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
    onClick: (() -> Unit)? = null
) {
    val (bgColor, textColor) = getTagColors(tag)
    Surface(
        shape = shape,
        color = bgColor,
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable { onClick() } else Modifier
        )
    ) {
        Row(
            modifier = Modifier.padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            if (showIcon) {
                Icon(
                    imageVector = Icons.Rounded.LocalOffer,
                    contentDescription = null,
                    modifier = Modifier.size(10.dp),
                    tint = textColor
                )
            }
            Text(
                text = tag,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * FlowRow displaying a sequence of StudyTagChip elements parsed from a list of strings.
 */
@OptIn(ExperimentalLayoutApi::class)
@ValueScore(
    score = 65,
    importance = Importance.MEDIUM,
    description = "Flow layout displaying multiple StudyTagChips with optional limit and click routing",
    category = "Study"
)
@Composable
fun StudyTagChipsFlow(
    tags: List<String>,
    modifier: Modifier = Modifier,
    horizontalSpacing: Dp = 5.dp,
    verticalSpacing: Dp = 4.dp,
    maxTags: Int? = null,
    showIcon: Boolean = true,
    onTagClick: ((String) -> Unit)? = null
) {
    val displayTags = if (maxTags != null) tags.take(maxTags) else tags
    if (displayTags.isNotEmpty()) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
            verticalArrangement = Arrangement.spacedBy(verticalSpacing),
            modifier = modifier
        ) {
            displayTags.forEach { tag ->
                StudyTagChip(
                    tag = tag,
                    showIcon = showIcon,
                    onClick = if (onTagClick != null) { { onTagClick(tag) } } else null
                )
            }
        }
    }
}

/**
 * Deduplicated animated linear progress indicator with title header and value indicator badge.
 */
@ValueScore(
    score = 75,
    importance = Importance.HIGH,
    description = "Animated linear progress bar with title and value badge",
    category = "Study"
)
@Composable
fun StudyProgressBar(
    progress: Float,
    label: String,
    valueText: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.tertiary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    badgeColor: Color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
    badgeTextColor: Color = MaterialTheme.colorScheme.tertiary,
    height: Dp = 6.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        label = "studyProgress"
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = badgeColor
            ) {
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = badgeTextColor,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(height / 2)),
            color = color,
            trackColor = trackColor
        )
    }
}

/**
 * Metric status badge with icon and label for attendance, assignments, and health states.
 */
@ValueScore(
    score = 65,
    importance = Importance.MEDIUM,
    description = "Colored status indicator badge with icon and text",
    category = "Study"
)
@Composable
fun StudyStatusBadge(
    text: String,
    icon: ImageVector,
    color: Color,
    containerColor: Color,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
    iconSize: Dp = 14.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
) {
    Surface(
        shape = shape,
        color = containerColor,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(iconSize)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Card info or empty state placeholder row (e.g. "No syllabus topics added yet").
 */
@ValueScore(
    score = 55,
    importance = Importance.LOW,
    description = "Information row with icon and text in a muted container",
    category = "Study"
)
@Composable
fun StudyCardInfoRow(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
    shape: Shape = RoundedCornerShape(8.dp)
) {
    Surface(
        shape = shape,
        color = containerColor,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Clickable pill chip representing a linked Course or Subject with indicator dot.
 */
@ValueScore(
    score = 65,
    importance = Importance.MEDIUM,
    description = "Clickable chip representing linked course or subject associations",
    category = "Study"
)
@Composable
fun StudyLinkedEntityChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    shape: Shape = RoundedCornerShape(8.dp)
) {
    Surface(
        shape = shape,
        color = containerColor,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(color, CircleShape)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (containerColor == MaterialTheme.colorScheme.surfaceContainerHigh) MaterialTheme.colorScheme.onSurface else color
            )
        }
    }
}

/**
 * Standard header overview metric card displaying an icon, primary value, and subtitle label.
 */
@ValueScore(
    score = 75,
    importance = Importance.HIGH,
    description = "Metric overview card for tab header summaries",
    category = "Study"
)
@Composable
fun StudyHeaderStatCard(
    value: String,
    label: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    iconBackgroundColor: Color = iconColor.copy(alpha = 0.12f)
) {
    ScholarCard(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(iconBackgroundColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Hero empty state card with icon illustration, title, description, and actionable primary bouncy button.
 */
@ValueScore(
    score = 80,
    importance = Importance.HIGH,
    description = "Hero empty state illustration card with action CTA",
    category = "Study"
)
@Composable
fun StudyEmptyStateCard(
    icon: ImageVector,
    title: String,
    description: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    buttonContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    buttonContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    ScholarCard(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .background(accentColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(38.dp),
                    tint = accentColor
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            BouncyButton(
                onClick = onButtonClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonContainerColor,
                    contentColor = buttonContentColor
                )
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(buttonText, fontWeight = FontWeight.Bold)
            }
        }
    }
}
