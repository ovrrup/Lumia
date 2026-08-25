package lumia.tracker.ui.screens.study.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore

/**
 * MetricSummaryTile - Compact metric summary card showing a prominent headline value and label.
 * Deduplicates repeated metric counters across SelfStudy and Analytics screens.
 */
@ValueScore(
    score = 60,
    importance = Importance.MEDIUM,
    description = "Compact metric summary tile with count/value and subtitle label",
    category = "Study"
)
@Composable
fun MetricSummaryTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.primary,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    shape: Shape = RoundedCornerShape(20.dp),
    onClick: (() -> Unit)? = null
) {
    ScholarCard(
        modifier = modifier,
        shape = shape,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = valueColor
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * PriorityBadge - Standardized priority indicator pill with colored dot indicator and label.
 * Deduplicates priority rendering across TaskItemCard, TagsHubScreen, and SelfStudyTab.
 */
@ValueScore(
    score = 45,
    importance = Importance.MEDIUM,
    description = "Standardized priority indicator pill with color dot and label",
    category = "Study"
)
@Composable
fun PriorityBadge(
    priority: Int,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val (pText, pBg, pTint) = when (priority) {
        2 -> Triple("High", MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f), MaterialTheme.colorScheme.error)
        1 -> Triple(if (compact) "Med" else "Medium", MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f), MaterialTheme.colorScheme.secondary)
        else -> Triple("Low", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = pBg,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
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
 * Deduplicates repeated chart card wrapping logic across AnalyticsTab.
 */
@ValueScore(
    score = 65,
    importance = Importance.MEDIUM,
    description = "Standardized card container with rounded corners and consistent padding for analytic charts",
    category = "Analytics"
)
@Composable
fun ChartContainerCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    ScholarCard(
        modifier = modifier.fillMaxWidth(),
        shape = shape
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            content = content
        )
    }
}

/**
 * StatMetricColumn - Vertical numerical metric column with small header label and large bold value.
 * Used inside analytical cards for streaks, test summaries, and quick metrics.
 */
@ValueScore(
    score = 40,
    importance = Importance.MEDIUM,
    description = "Vertical metric column with header label and bold numerical display",
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
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = valueColor
        )
    }
}
