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
import lumia.tracker.ui.components.GlassCapsule
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.components.ScholarCardDefaults
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.screens.settings.components.SettingsActionItemInCard
import lumia.tracker.ui.screens.settings.components.SettingsGroupCard
import lumia.tracker.viewmodel.ScholarViewModel

@ValueScore(
    score = 96,
    importance = Importance.CRITICAL,
    description = "Modern data management, zero-data-loss database migrations, safety snapshots, and vault export/import",
    category = "Settings"
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataManagementScreen(navController: NavController, viewModel: ScholarViewModel) {
    val status by viewModel.importExportStatus.collectAsStateWithLifecycle()
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val defragText by viewModel.defragStatus.collectAsStateWithLifecycle()
    val safetyInfo by viewModel.safetySnapshotInfo.collectAsStateWithLifecycle()
    val hasSnapshot by viewModel.hasSafetySnapshot.collectAsStateWithLifecycle()

    val coursesCount by viewModel.courses.collectAsStateWithLifecycle()
    val assignmentsCount by viewModel.assignments.collectAsStateWithLifecycle()
    val subjectsCount by viewModel.subjects.collectAsStateWithLifecycle()
    val pomodoroSessionsCount by viewModel.pomodoroSessions.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showRestoreSnapshotDialog by remember { mutableStateOf(false) }
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
        viewModel.checkSafetySnapshot()
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

            // 1. Hero Storage & Database Integrity Card
            ScholarCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = ScholarCardDefaults.shape,
                glassmorphic = true
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Storage,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    "Your Data",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                GlassCapsule(
                                    containerColor = Color(0xFF34C759).copy(alpha = 0.15f),
                                    border = BorderStroke(0.5.dp, Color(0xFF34C759).copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = "Protected",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF34C759),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                "${activeProfile.name} • Local SQLite Storage",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Storage distribution bar
                    val totalRecords = coursesCount.size + subjectsCount.size + assignmentsCount.size + pomodoroSessionsCount.size
                    if (totalRecords > 0) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Saved Items",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "$totalRecords total records",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape)
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

                    // Modern Capsule Metrics Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricPill(
                            count = coursesCount.size,
                            label = "Courses",
                            accentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        MetricPill(
                            count = subjectsCount.size,
                            label = "Subjects",
                            accentColor = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        )
                        MetricPill(
                            count = assignmentsCount.size,
                            label = "Tasks",
                            accentColor = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f)
                        )
                        MetricPill(
                            count = pomodoroSessionsCount.size,
                            label = "Focus",
                            accentColor = Color(0xFFFF9500),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 2. Data Safety & Migration Vault (Zero data loss guarantee across stable releases)
            SettingsGroupCard(title = "Safety & Rollback Protection", icon = Icons.Rounded.Security) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.VerifiedUser,
                            contentDescription = null,
                            tint = Color(0xFF34C759),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Release Migration Guard Active",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "Continuous schema migration protects your data when upgrading from main branch releases. Automatic safety backups are created before any database upgrade or import.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    if (!safetyInfo.isNullOrBlank()) {
                        GlassCapsule(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = safetyInfo ?: "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Action buttons in capsule format
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BouncyButton(
                            onClick = { viewModel.createManualSafetySnapshot() },
                            modifier = Modifier.weight(1f),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(Icons.Rounded.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create Snapshot", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }

                        if (hasSnapshot) {
                            BouncyButton(
                                onClick = { showRestoreSnapshotDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Icon(Icons.Rounded.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Restore Snapshot", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 3. Save & Restore Group
            SettingsGroupCard(title = "Save & Restore", icon = Icons.Rounded.SaveAlt) {
                SettingsActionItemInCard(
                    title = "Save a Backup File",
                    subtitle = "Export all classes, tasks, and notes to a secure backup file",
                    icon = Icons.Rounded.UploadFile,
                    iconBgColor = Color(0xFF007AFF),
                    onClick = { showExportDialog = true }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                    modifier = Modifier.padding(start = 56.dp, end = 8.dp)
                )

                SettingsActionItemInCard(
                    title = "Restore from Backup",
                    subtitle = "Load saved classes and tasks from a backup file with auto-rollback protection",
                    icon = Icons.Rounded.FileDownload,
                    iconBgColor = Color(0xFF34C759),
                    onClick = { openDocumentLauncher.launch(arrayOf("application/octet-stream", "*/*")) }
                )
            }

            // 4. Storage & Performance
            SettingsGroupCard(title = "Storage & Maintenance", icon = Icons.Rounded.CleaningServices) {
                SettingsActionItemInCard(
                    title = "Clean Up App Storage",
                    subtitle = if (defragText.isNotBlank()) defragText else "Defragment SQLite database and reclaim unused disk space",
                    icon = Icons.Rounded.CleaningServices,
                    iconBgColor = Color(0xFFFF9500),
                    onClick = { viewModel.defragmentDatabase() }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                    modifier = Modifier.padding(start = 56.dp, end = 8.dp)
                )

                SettingsActionItemInCard(
                    title = "Delete All Data",
                    subtitle = "Permanently erase classes, tasks, and history (Safety snapshot created first)",
                    icon = Icons.Rounded.DeleteForever,
                    isDestructive = true,
                    onClick = { showResetDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Export Options Modal Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            icon = { Icon(Icons.Rounded.SaveAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Save Backup Package", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select what you would like to export:", style = MaterialTheme.typography.bodyMedium)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                exportAllMode = false
                                showExportDialog = false
                                createDocumentLauncher.launch("lumia_${activeProfile.name.lowercase().replace(" ", "_")}_backup.lumia")
                            }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Current Profile (${activeProfile.name})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Saves courses, syllabus, tasks, attendance, and tags for this workspace.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(16.dp),
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
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Complete Vault (All Profiles)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Saves everything across all tenant profiles and global settings.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

    // Restore Snapshot Confirmation Dialog
    if (showRestoreSnapshotDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreSnapshotDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            icon = { Icon(Icons.Rounded.Restore, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Restore Safety Snapshot", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) },
            text = {
                Text(
                    "This will restore your database to the last verified safe state ($safetyInfo). Current modifications made since the snapshot will be replaced.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                BouncyButton(
                    onClick = {
                        viewModel.restoreLatestSafetySnapshot()
                        showRestoreSnapshotDialog = false
                    },
                    shape = CircleShape
                ) {
                    Text("Restore State", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                BouncyTextButton(onClick = { showRestoreSnapshotDialog = false }) { Text("Cancel") }
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
            title = { Text("Delete All Data", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error) },
            text = {
                Text(
                    "Are you sure? This will permanently delete your classes, syllabus, tasks, and focus history. A recovery snapshot will be saved in case you need to revert.",
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
                    shape = CircleShape
                ) {
                    Text("Delete Everything", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                BouncyTextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun MetricPill(
    count: Int,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = accentColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
