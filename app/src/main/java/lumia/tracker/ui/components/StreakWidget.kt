package lumia.tracker.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyOutlinedButton
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.theme.bouncyClick
import lumia.tracker.viewmodel.ScholarViewModel

/**
 * StreakWidget - Sleek, lightweight animated streak flame badge and detail bottom sheet.
 */
@ValueScore(
    score = 86,
    importance = Importance.HIGH,
    description = "Interactive animated streak flame badge with progress ring and detail bottom sheet",
    category = "Widget"
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreakWidget(
    viewModel: ScholarViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val streakCurrent by viewModel.streakCurrent.collectAsStateWithLifecycle()
    val streakPercentage by viewModel.streakPercentage.collectAsStateWithLifecycle()
    val streakColorHex by viewModel.streakProgressColor.collectAsStateWithLifecycle()
    val streakBrightness by viewModel.streakBrightness.collectAsStateWithLifecycle()
    val isCompleteToday by viewModel.streakIsCompleteToday.collectAsStateWithLifecycle()

    val streakLongest by viewModel.streakLongest.collectAsStateWithLifecycle()
    val streakTotalComplete by viewModel.streakTotalComplete.collectAsStateWithLifecycle()
    val reqTasks by viewModel.streakRequirementTasks.collectAsStateWithLifecycle()
    val reqStudyMins by viewModel.streakRequirementStudyMins.collectAsStateWithLifecycle()

    var showStreakSheet by remember { mutableStateOf(false) }

    val primary = MaterialTheme.colorScheme.primary
    val baseColor = if (streakColorHex == "Theme") primary else try {
        Color(android.graphics.Color.parseColor(streakColorHex))
    } catch (e: Exception) {
        primary
    }

    val hsl = FloatArray(3)
    androidx.core.graphics.ColorUtils.colorToHSL(
        android.graphics.Color.argb(
            (baseColor.alpha * 255).toInt(),
            (baseColor.red * 255).toInt(),
            (baseColor.green * 255).toInt(),
            (baseColor.blue * 255).toInt()
        ), hsl
    )
    hsl[2] = (hsl[2] * streakBrightness).coerceIn(0f, 1f)
    val color = Color(androidx.core.graphics.ColorUtils.HSLToColor(hsl))

    // Lightweight animated progress
    val animProgress by animateFloatAsState(
        targetValue = streakPercentage.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "streak_progress"
    )

    val flameScale by animateFloatAsState(
        targetValue = if (isCompleteToday) 1.12f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "flame_scale"
    )

    // Streak Widget Pill
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .height(40.dp)
            .clip(CircleShape)
            .background(
                if (isCompleteToday) color.copy(alpha = 0.16f)
                else MaterialTheme.colorScheme.surfaceContainerHigh
            )
            .border(
                0.8.dp,
                if (isCompleteToday) color.copy(alpha = 0.45f)
                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                CircleShape
            )
            .bouncyClick(onClick = { showStreakSheet = true })
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = streakCurrent.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = if (isCompleteToday) color else MaterialTheme.colorScheme.onSurface
        )

        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(24.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 2.5.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2

                // Background track
                drawCircle(
                    color = color.copy(alpha = 0.15f),
                    radius = radius,
                    style = Stroke(width = strokeWidth)
                )

                // Animated progress arc
                if (animProgress > 0f) {
                    drawArc(
                        color = color,
                        startAngle = -90f,
                        sweepAngle = 360f * animProgress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }

            Icon(
                imageVector = Icons.Rounded.LocalFireDepartment,
                contentDescription = "Streak Flame",
                tint = if (isCompleteToday) color else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                modifier = Modifier
                    .size(15.dp)
                    .graphicsLayer {
                        scaleX = flameScale
                        scaleY = flameScale
                    }
            )
        }
    }

    // Modal Bottom Sheet: Decluttered Streak Details & Milestones
    if (showStreakSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showStreakSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 6.dp,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = {
                BottomSheetDefaults.DragHandle(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Clean Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocalFireDepartment,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = "$streakCurrent Day Streak",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isCompleteToday) "Daily goal completed" else "In progress today",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Today's Progress Card
                ScholarCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Today's Progress",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${(streakPercentage * 100).toInt()}%",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = color
                            )
                        }

                        LinearProgressIndicator(
                            progress = { animProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = color,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        // Simplified requirements
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            StreakReqItem(
                                icon = Icons.Rounded.CheckCircle,
                                label = "Tasks",
                                value = "$reqTasks target"
                            )
                            StreakReqItem(
                                icon = Icons.Rounded.Timer,
                                label = "Study Focus",
                                value = "$reqStudyMins min"
                            )
                        }
                    }
                }

                // Milestone Statistics Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StreakStatCard(
                        modifier = Modifier.weight(1f),
                        title = "Best Streak",
                        value = "$streakLongest Days",
                        icon = Icons.Rounded.EmojiEvents,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    StreakStatCard(
                        modifier = Modifier.weight(1f),
                        title = "Perfect Days",
                        value = "$streakTotalComplete",
                        icon = Icons.Rounded.Star,
                        tint = color
                    )
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BouncyOutlinedButton(
                        onClick = {
                            showStreakSheet = false
                            navController.navigate("settings/streaks")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Icon(Icons.Rounded.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Settings", fontWeight = FontWeight.SemiBold)
                    }

                    BouncyButton(
                        onClick = { showStreakSheet = false },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = color)
                    ) {
                        Text(
                            text = "Done",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StreakReqItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StreakStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color
) {
    ScholarCard(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
