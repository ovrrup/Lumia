package lumia.tracker.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.screens.settings.components.SettingsGroupCard
import lumia.tracker.ui.screens.settings.components.SettingsToggleItem
import lumia.tracker.viewmodel.ScholarViewModel

@ValueScore(
    score = 82,
    importance = Importance.EXPERIMENTAL,
    description = "Experimental sandbox and beta feature diagnostics",
    category = "Settings"
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BetaFeaturesScreen(navController: NavController, viewModel: ScholarViewModel) {
    val betaNotes by viewModel.betaNotes.collectAsStateWithLifecycle()
    val showActionHistory by viewModel.showActionHistory.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Experimental Features",
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Clean 1-Line Sandbox Note
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Science,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Experimental features in active development",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            // 1. Experimental Workflow
            SettingsGroupCard(title = "Experimental Workflow", icon = Icons.Rounded.EditNote) {
                SettingsToggleItem(
                    title = "Quick Notes Overlay",
                    subtitle = "Floating scratchpad notes overlay",
                    checked = betaNotes,
                    icon = Icons.Rounded.EditNote,
                    onCheckedChange = { viewModel.updateBetaNotes(it) }
                )
            }

            // 2. Telemetry & Diagnostics
            SettingsGroupCard(title = "Display Settings & Hooks", icon = Icons.Rounded.Analytics) {
                SettingsToggleItem(
                    title = "Display Action History",
                    subtitle = "Show audit history in analytics",
                    checked = showActionHistory,
                    icon = Icons.Rounded.HistoryToggleOff,
                    onCheckedChange = { viewModel.updateShowActionHistory(it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

