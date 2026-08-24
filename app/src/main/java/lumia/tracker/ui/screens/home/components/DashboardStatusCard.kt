package lumia.tracker.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.viewmodel.ScholarViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * DashboardStatusCard - Focuses strictly on weekly consistency and study activity
 * without repeating top header elements (greeting, streak flame, focus launch).
 */
@Composable
fun DashboardStatusCard(
    userName: String,
    streakDays: Int,
    viewModel: ScholarViewModel,
    onStartFocus: () -> Unit = {}
) {
    val dateFull = remember { SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date()) }
    val streakColor = MaterialTheme.colorScheme.primary

    ScholarCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Weekly Activity Matrix",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = dateFull,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Calendar Dots Week Matrix
            DashboardWeekMatrix(
                prefs = viewModel.prefs,
                streakDays = streakDays,
                streakColor = streakColor
            )
        }
    }
}
