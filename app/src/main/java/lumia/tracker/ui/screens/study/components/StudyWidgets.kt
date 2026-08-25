package lumia.tracker.ui.screens.study.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.BouncyTextButton
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore

@ValueScore(
    score = 60,
    importance = Importance.MEDIUM,
    description = "Reusable section header with prominent icon, action button and optional trailing content",
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
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Icon(icon, contentDescription = null, tint = iconTint)
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )
        }
        if (trailing != null) {
            trailing()
        } else if (onAddClick != null) {
            BouncyIconButton(
                onClick = onAddClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Item", tint = iconTint)
            }
        }
    }
}

@ValueScore(
    score = 55,
    importance = Importance.MEDIUM,
    description = "Reusable empty state card with call-to-action button and descriptive prompt",
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
    ScholarCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
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
                        .size(48.dp)
                        .background(accentColor.copy(alpha = 0.1f), CircleShape),
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
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (icon != null) FontWeight.Bold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
            if (buttonText != null && onClick != null) {
                Spacer(modifier = Modifier.height(10.dp))
                BouncyTextButton(onClick = onClick) {
                    Text(buttonText, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@ValueScore(
    score = 45,
    importance = Importance.LOW,
    description = "Compact pill badge for instructor, schedule, and study metadata tags",
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
        color = tint.copy(alpha = 0.12f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = tint
            )
        }
    }
}

@ValueScore(
    score = 45,
    importance = Importance.LOW,
    description = "Attendance breakdown summary counter item with colored status dot",
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
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, CircleShape)
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = color
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@ValueScore(
    score = 50,
    importance = Importance.MEDIUM,
    description = "Subject and course overview statistic metric item",
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
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = tint
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (detail != null) {
            Text(
                text = detail,
                style = MaterialTheme.typography.labelSmall,
                color = tint.copy(alpha = 0.85f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
