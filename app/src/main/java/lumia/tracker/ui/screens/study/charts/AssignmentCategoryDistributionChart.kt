package lumia.tracker.ui.screens


import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.setValue

@Composable
fun AssignmentCategoryDistributionChart(
    modifier: Modifier = Modifier,
    assignments: List<lumia.tracker.model.PracticeAssignment>
) {
    lumia.tracker.ui.components.GlassCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            Text(
                "Assessment Distribution",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Proportion of work types across all subjects",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 20.dp)
            )

            if (assignments.isEmpty()) {
                Text(
                    "No assignments available.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val countByCategory = assignments
                    .groupBy { it.category }
                    .map { (cat, list) ->
                        val colHex = list.firstOrNull()?.categoryColor?.ifBlank { "#3197D6" } ?: "#3197D6"
                        Triple(cat, list.size, colHex)
                    }
                    .sortedByDescending { it.second }

                val total = assignments.size.toFloat()

                // Segmented Progress bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                ) {
                    countByCategory.forEach { (cat, count, colHex) ->
                        val fraction = count / total
                        val itemColor = try { Color(android.graphics.Color.parseColor(colHex)) } catch (e: Exception) { MaterialTheme.colorScheme.primary }
                        Box(
                            modifier = Modifier
                                .weight(fraction)
                                .fillMaxHeight()
                                .background(itemColor)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Legend Grid (2 columns or simple flow row)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    countByCategory.forEach { (cat, count, colHex) ->
                        val pct = ((count / total) * 100).toInt()
                        val itemColor = try { Color(android.graphics.Color.parseColor(colHex)) } catch (e: Exception) { MaterialTheme.colorScheme.primary }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(itemColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = cat,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "$count ($pct%)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
