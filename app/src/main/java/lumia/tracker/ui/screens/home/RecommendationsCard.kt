package lumia.tracker.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.ThumbDown
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.BouncyTextButton
import lumia.tracker.ui.components.GlassCard
import lumia.tracker.viewmodel.ScholarViewModel

data class StudyRecommendation(
    val id: String,
    val title: String,
    val description: String,
    var userRating: Int = 0 // 1 for upvote, -1 for downvote, 0 for neutral
)

@Composable
fun RecommendationsCard(viewModel: ScholarViewModel, modifier: Modifier = Modifier) {
    // Simulated recommendations based on ML insights
    var recommendations by remember {
        mutableStateOf(
            listOf(
                StudyRecommendation("rec1", "Shift Study Time", "You focus 15% better in the mornings. Consider moving your toughest tasks to 9 AM."),
                StudyRecommendation("rec2", "Break Strategy", "Your focus drops after 40 minutes. Try taking a 5-minute break earlier.")
            )
        )
    }

    if (recommendations.isEmpty()) return

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lightbulb,
                    contentDescription = "Insights",
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = "Smart Insights",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            recommendations.forEach { recommendation ->
                RecommendationItem(
                    recommendation = recommendation,
                    onApply = {
                        // In a full implementation, this applies the schedule optimization
                        recommendations = recommendations.filter { it.id != recommendation.id }
                    },
                    onIgnore = {
                        recommendations = recommendations.filter { it.id != recommendation.id }
                    },
                    onRate = { rating ->
                        val updated = recommendations.map {
                            if (it.id == recommendation.id) it.copy(userRating = rating) else it
                        }
                        recommendations = updated
                        viewModel.submitRecommendationFeedback(recommendation.id, rating)
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun RecommendationItem(
    recommendation: StudyRecommendation,
    onApply: () -> Unit,
    onIgnore: () -> Unit,
    onRate: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Text(
            text = recommendation.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = recommendation.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rating actions
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (recommendation.userRating == 1) MaterialTheme.colorScheme.tertiary else Color.Transparent)
                        .clickable { onRate(1) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ThumbUp,
                        contentDescription = "Helpful",
                        modifier = Modifier.size(16.dp),
                        tint = if (recommendation.userRating == 1) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (recommendation.userRating == -1) MaterialTheme.colorScheme.error else Color.Transparent)
                        .clickable { onRate(-1) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ThumbDown,
                        contentDescription = "Not Helpful",
                        modifier = Modifier.size(16.dp),
                        tint = if (recommendation.userRating == -1) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Apply / Ignore Actions
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BouncyTextButton(onClick = onIgnore, modifier = Modifier.height(32.dp)) {
                    Text("Ignore", fontSize = 12.sp)
                }
                Button(
                    onClick = onApply,
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Text("Apply", fontSize = 12.sp, color = MaterialTheme.colorScheme.onTertiary)
                }
            }
        }
    }
}
