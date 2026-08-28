package lumia.tracker.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlin.math.roundToInt
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.StreakWidget
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.screens.settings.components.SettingsGroupCard
import lumia.tracker.viewmodel.ScholarViewModel

@ValueScore(
    score = 90,
    importance = Importance.HIGH,
    description = "Streak targets, partial threshold, and visual calibration",
    category = "Settings"
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreakSettingsScreen(navController: NavController, viewModel: ScholarViewModel) {
    val reqTasks by viewModel.streakRequirementTasks.collectAsStateWithLifecycle()
    val reqStudyMins by viewModel.streakRequirementStudyMins.collectAsStateWithLifecycle()
    val partialThreshold by viewModel.streakPartialThreshold.collectAsStateWithLifecycle()
    val brightness by viewModel.streakBrightness.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Streak Goals",
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    BouncyIconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 1. Live Preview
            item {
                SettingsGroupCard(title = "Preview", icon = Icons.Rounded.LocalFireDepartment) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        StreakWidget(viewModel, navController)
                    }
                }
            }

            // 2. Daily Streak Goals
            item {
                SettingsGroupCard(title = "Daily Targets", icon = Icons.Rounded.TrackChanges) {
                    Column(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Tasks Requirement
                        StreakSliderItem(
                            title = "Tasks Required",
                            valueLabel = "$reqTasks tasks",
                            value = reqTasks.toFloat(),
                            onValueChange = { viewModel.updateStreakRequirementTasks(it.roundToInt()) },
                            valueRange = 0f..10f,
                            steps = 9
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                        // Study Minutes Requirement
                        StreakSliderItem(
                            title = "Study Minutes",
                            valueLabel = "$reqStudyMins min",
                            value = reqStudyMins.toFloat(),
                            onValueChange = { viewModel.updateStreakRequirementStudyMins(it.roundToInt()) },
                            valueRange = 0f..180f,
                            steps = 17
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                        // Partial Threshold
                        StreakSliderItem(
                            title = "Partial Threshold",
                            valueLabel = "${(partialThreshold * 100).roundToInt()}%",
                            value = partialThreshold,
                            onValueChange = { viewModel.updateStreakPartialThreshold(it) },
                            valueRange = 0.1f..0.9f,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // 3. Visuals
            item {
                SettingsGroupCard(title = "Visuals", icon = Icons.Rounded.Flare) {
                    Column(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StreakSliderItem(
                            title = "Glow Brightness",
                            valueLabel = "${(brightness * 100).roundToInt()}%",
                            value = brightness,
                            onValueChange = { viewModel.updateStreakBrightness(it) },
                            valueRange = 0.2f..1.5f
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StreakSliderItem(
    title: String,
    valueLabel: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = containerColor
            ) {
                Text(
                    text = valueLabel,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
