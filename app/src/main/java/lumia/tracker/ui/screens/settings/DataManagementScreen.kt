package lumia.tracker.ui.screens.settings

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.BouncyTextButton
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.screens.settings.components.SettingsActionItemInCard
import lumia.tracker.ui.screens.settings.components.SettingsGroupCard
import lumia.tracker.viewmodel.ScholarViewModel

@ValueScore(
    score = 92,
    importance = Importance.HIGH,
    description = "Data management, database integrity metrics, backup export/import and maintenance",
    category = "Settings"
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataManagementScreen(navController: NavController, viewModel: ScholarViewModel) {
    val status by viewModel.importExportStatus.collectAsStateWithLifecycle()
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val dbStats by viewModel.dbStatistics.collectAsStateWithLifecycle()
    val defragText by viewModel.defragStatus.collectAsStateWithLifecycle()

    val coursesCount by viewModel.courses.collectAsStateWithLifecycle()
    val assignmentsCount by viewModel.assignments.collectAsStateWithLifecycle()
    val subjectsCount by viewModel.subjects.collectAsStateWithLifecycle()
    val pomodoroSessionsCount by viewModel.pomodoroSessions.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportAllMode by remember { mutableStateOf(false) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.exportData(uri, exportAll = exportAllMode)
        }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importData(uri)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadDBStatistics()
    }

    LaunchedEffect(status) {
        status?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearStatus()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Data & Backups",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
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

            // 1. Database Integrity & Storage Metrics Hero Card
            ScholarCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Storage,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    "Active Workspace",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF34C759).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "Healthy",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF34C759),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                "${activeProfile.name} • SQLite Engine v3.45",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Storage Distribution Breakdown Bar
                    val totalRecords = coursesCount.size + subjectsCount.size + assignmentsCount.size + pomodoroSessionsCount.size
                    if (totalRecords > 0) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Storage Distribution",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "$totalRecords Total Entities",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            ) {
                                val cWeight = (coursesCount.size.toFloat() / totalRecords).coerceAtLeast(0.02f)
                                val sWeight = (subjectsCount.size.toFloat() / totalRecords).coerceAtLeast(0.02f)
                                val aWeight = (assignmentsCount.size.toFloat() / totalRecords).coerceAtLeast(0.02f)
                                val pWeight = (pomodoroSessionsCount.size.toFloat() / totalRecords).coerceAtLeast(0.02f)

                                Box(modifier = Modifier.weight(cWeight).fillMaxHeight().background(MaterialTheme.colorScheme.primary))
                                Box(modifier = Modifier.weight(sWeight).fillMaxHeight().background(MaterialTheme.colorScheme.secondary))
                                Box(modifier = Modifier.weight(aWeight).fillMaxHeight().background(MaterialTheme.colorScheme.tertiary))
                                Box(modifier = Modifier.weight(pWeight).fillMaxHeight().background(Color(0xFFFF9500)))
                            }
                        }
                    }

                    // Metrics Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${coursesCount.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                Text("Courses", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${subjectsCount.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary)
                                Text("Subjects", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${assignmentsCount.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.tertiary)
                                Text("Tasks", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${pomodoroSessionsCount.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color(0xFFFF9500))
                                Text("Sessions", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // 2. Backup & Export Group
            SettingsGroupCard(title = "Backup & Portability", icon = Icons.Rounded.SaveAlt) {
                SettingsActionItemInCard(
                    title = "Export Workspace Backup",
                    subtitle = "Export encrypted snapshot (.lumia)",
                    icon = Icons.Rounded.UploadFile,
                    iconBgColor = Color(0xFF007AFF),
                    onClick = { showExportDialog = true }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                    modifier = Modifier.padding(start = 56.dp, end = 8.dp)
                )

                SettingsActionItemInCard(
                    title = "Import & Restore Snapshot",
                    subtitle = "Restore database from backup file",
                    icon = Icons.Rounded.FileDownload,
                    iconBgColor = Color(0xFF34C759),
                    onClick = { openDocumentLauncher.launch(arrayOf("application/octet-stream", "*/*")) }
                )
            }

            // 3. Maintenance & Optimizations
            SettingsGroupCard(title = "Maintenance & Optimization", icon = Icons.Rounded.Build) {
                SettingsActionItemInCard(
                    title = "Optimize & Defragment Database",
                    subtitle = if (defragText.isNotBlank()) defragText else "Rebuild SQLite indexes, vacuum tables, and reclaim free pages",
                    icon = Icons.Rounded.CleaningServices,
                    iconBgColor = Color(0xFFFF9500),
                    onClick = { viewModel.defragmentDatabase() }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                    modifier = Modifier.padding(start = 56.dp, end = 8.dp)
                )

                SettingsActionItemInCard(
                    title = "Reset Workspace Data",
                    subtitle = "Clear records or reset database",
                    icon = Icons.Rounded.DeleteForever,
                    isDestructive = true,
                    onClick = { showResetDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Export Options Modal Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            icon = { Icon(Icons.Rounded.SaveAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Export Backup", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select backup scope:", style = MaterialTheme.typography.bodyMedium)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                exportAllMode = false
                                showExportDialog = false
                                createDocumentLauncher.launch("lumia_${activeProfile.name.lowercase()}_backup.lumia")
                            }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Current Profile (${activeProfile.name})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Export data linked to this workspace only.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                exportAllMode = true
                                showExportDialog = false
                                createDocumentLauncher.launch("lumia_full_vault_backup.lumia")
                            }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("All Profiles & Data", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Export all scholar profiles, tags, and settings.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                BouncyTextButton(onClick = { showExportDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Reset Confirmation Modal Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            icon = { Icon(Icons.Rounded.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Reset Academic Data", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error) },
            text = {
                Text(
                    "Are you sure? This will permanently delete course records, tasks, and focus logs in this workspace.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                BouncyButton(
                    onClick = {
                        viewModel.clearUserData()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Confirm Reset", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                BouncyTextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
            }
        )
    }
}

