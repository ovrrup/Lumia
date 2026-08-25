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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.screens.settings.components.SettingsActionItemInCard
import lumia.tracker.ui.screens.settings.components.SettingsGroupCard
import lumia.tracker.viewmodel.ScholarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataManagementScreen(navController: NavController, viewModel: ScholarViewModel) {
    val status by viewModel.importExportStatus.collectAsStateWithLifecycle()
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val dbStats by viewModel.dbStatistics.collectAsStateWithLifecycle()
    val defragText by viewModel.defragStatus.collectAsStateWithLifecycle()
    val tagCustomizations by viewModel.allTagCustomizations.collectAsStateWithLifecycle()

    val coursesCount by viewModel.courses.collectAsStateWithLifecycle()
    val assignmentsCount by viewModel.assignments.collectAsStateWithLifecycle()
    val subjectsCount by viewModel.subjects.collectAsStateWithLifecycle()
    val pomodoroSessionsCount by viewModel.pomodoroSessions.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showResetDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportAllMode by remember { mutableStateOf(false) }
    var showSuccessorDialog by remember { mutableStateOf(false) }
    var resetTarget by remember { mutableStateOf("self") }

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

            // 1. Database Integrity & Metrics Hero Card
            ScholarCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
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
                                contentDescription = "Active Database Info",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Database Integrity Status",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "100% offline & local binary storage",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(
                            Triple(coursesCount.size, "Courses", Icons.Rounded.School),
                            Triple(subjectsCount.size, "Subjects", Icons.Rounded.Book),
                            Triple(assignmentsCount.size, "Assignments", Icons.Rounded.Assignment),
                            Triple(pomodoroSessionsCount.size, "Pomodoros", Icons.Rounded.Timer)
                        ).forEach { (count, label, _) ->
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 3.dp),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                ) {
                                    Text(
                                        text = count.toString(),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Multi-Device P2P Sync Banner
            ScholarCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                onClick = { navController.navigate("settings/sync") }
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Autorenew,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Multi-Device P2P Sync",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Sync across phones & tablets over local Wi-Fi with zero-trust encryption",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // 3. Storage Optimization & SQLite VACUUM
            SettingsGroupCard(title = "Storage Optimization", icon = Icons.Rounded.Storage) {
                Column(modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)) {
                    Text(
                        "SQLite Local Schema Metrics",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "View row counts of the physical application databases in real-time",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (dbStats.isEmpty()) {
                        Button(
                            onClick = { viewModel.loadDBStatistics() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Rounded.QueryStats, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Analyze Schema Metrics", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            dbStats.forEach { (table, count) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(table, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                    Text("$count rows", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            TextButton(
                                onClick = { viewModel.loadDBStatistics() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Re-Analyze Database")
                            }
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    Text(
                        "Index Pack Compacting & VACUUM",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        "Rebuild database indices, clean orphaned assignments, and run VACUUM optimization to reduce storage allocations",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { viewModel.defragmentDatabase() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        enabled = defragText.isEmpty() || defragText.startsWith("Optimized!"),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Execute SQLite Defrag", fontWeight = FontWeight.Bold)
                    }

                    if (defragText.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (defragText.startsWith("Optimized!")) Color(0xFF4BC27D).copy(alpha = 0.15f) else MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = defragText,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (defragText.startsWith("Optimized!")) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // 4. Tag Connectivity & Alignment
            SettingsGroupCard(title = "Tag Integrity & Maintenance", icon = Icons.Rounded.LocalOffer) {
                Column(modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)) {
                    Text(
                        "Align Tag Databases",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Synchronize academic entities, clean orphaned tags, and verify database integrity",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    var alignStatus by remember { mutableStateOf("") }
                    var loadingAlign by remember { mutableStateOf(false) }

                    Button(
                        onClick = {
                            scope.launch {
                                loadingAlign = true
                                alignStatus = "Verifying tag mappings..."
                                delay(800)
                                val tagsInDb = mutableSetOf<String>()
                                viewModel.courses.value.forEach { c -> c.tags.split(",").forEach { if (it.isNotBlank()) tagsInDb.add(it.trim().lowercase()) } }
                                viewModel.subjects.value.forEach { s -> s.tags.split(",").forEach { if (it.isNotBlank()) tagsInDb.add(it.trim().lowercase()) } }
                                viewModel.allTopics.value.forEach { t -> t.tags.split(",").forEach { if (it.isNotBlank()) tagsInDb.add(it.trim().lowercase()) } }
                                viewModel.tasks.value.forEach { t -> t.tags.split(",").forEach { if (it.isNotBlank()) tagsInDb.add(it.trim().lowercase()) } }
                                viewModel.allChapters.value.forEach { ch -> ch.tags.split(",").forEach { if (it.isNotBlank()) tagsInDb.add(it.trim().lowercase()) } }
                                viewModel.allTestRecords.value.forEach { tr -> tr.tags.split(",").forEach { if (it.isNotBlank()) tagsInDb.add(it.trim().lowercase()) } }

                                val customizations = tagCustomizations
                                var deletedOrphans = 0
                                customizations.forEach { cust ->
                                    if (!tagsInDb.contains(cust.tagName)) {
                                        viewModel.deleteTagCustomization(cust.tagName)
                                        deletedOrphans++
                                    }
                                }
                                alignStatus = "Aligned! ${tagsInDb.size} active tags mapped. Cleaned $deletedOrphans orphaned customizations."
                                loadingAlign = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        enabled = !loadingAlign,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (loadingAlign) "Aligning..." else "Scan & Align Tag Associations", fontWeight = FontWeight.Bold)
                    }

                    if (alignStatus.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = alignStatus,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                "Clear Custom Colors & Notes",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                "Erase tag metadata (custom colors, notes, favorite states) but keep associations intact",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    tagCustomizations.forEach { viewModel.deleteTagCustomization(it.tagName) }
                                    alignStatus = "Cleared all tag metadata customizations!"
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Reset Metas")
                        }
                    }
                }
            }

            // 5. My Profile Data Management
            SettingsGroupCard(title = "My Profile Data", icon = Icons.Rounded.Person) {
                SettingsActionItemInCard(
                    title = "Export My Data",
                    subtitle = "Back up your profile's settings, tasks, and history",
                    icon = Icons.Rounded.Upload,
                    onClick = {
                        exportAllMode = false
                        showExportDialog = true
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsActionItemInCard(
                    title = "Import Data",
                    subtitle = "Restore your profile's backup (Overwrites current profile)",
                    icon = Icons.Rounded.Download,
                    onClick = { openDocumentLauncher.launch(arrayOf("application/octet-stream", "*/*")) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsActionItemInCard(
                    title = "Erase My Data & Delete Account",
                    subtitle = "Permanently delete your profile and all local data",
                    icon = Icons.Rounded.DeleteForever,
                    isDestructive = true,
                    onClick = {
                        if (activeProfile.isDefault) {
                            showSuccessorDialog = true
                        } else {
                            showResetDialog = true
                            resetTarget = "self"
                        }
                    }
                )
            }

            // 6. Collective Master Management (if default account)
            if (activeProfile.isDefault) {
                SettingsGroupCard(title = "Collective Management (Master)", icon = Icons.Rounded.AdminPanelSettings) {
                    SettingsActionItemInCard(
                        title = "Export All Accounts Data",
                        subtitle = "Back up data for all users in the application",
                        icon = Icons.Rounded.Upload,
                        onClick = {
                            exportAllMode = true
                            showExportDialog = true
                        }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                    SettingsActionItemInCard(
                        title = "Factory Erase (All Accounts)",
                        subtitle = "Permanently delete all data for all accounts",
                        icon = Icons.Rounded.DeleteForever,
                        isDestructive = true,
                        onClick = {
                            showResetDialog = true
                            resetTarget = "all"
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Dialogs
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            shape = RoundedCornerShape(24.dp),
            icon = { Icon(Icons.Rounded.Upload, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Export Data Backup", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    if (exportAllMode) "Export a backup containing ALL user accounts and data?"
                    else "Export a backup containing YOUR account data?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExportDialog = false
                        createDocumentLauncher.launch("scholar_backup.bin")
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Export")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            shape = RoundedCornerShape(24.dp),
            icon = { Icon(Icons.Rounded.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = {
                Text(
                    if (resetTarget == "all") "Erase All App Data?" else "Erase Data & Delete Account?",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    if (resetTarget == "all") "This action cannot be undone. ALL user accounts and their data will be permanently deleted."
                    else "This action cannot be undone. Your profile and all your data will be permanently removed.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (resetTarget == "all") {
                            viewModel.clearAllData()
                        } else {
                            viewModel.eraseMyDataAndAccount()
                        }
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Permanently Erase", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showSuccessorDialog) {
        val allProfiles by viewModel.allProfiles.collectAsStateWithLifecycle()
        var createNew by remember { mutableStateOf(false) }
        var selectedSuccessorId by remember { mutableStateOf("") }
        var newName by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showSuccessorDialog = false },
            shape = RoundedCornerShape(24.dp),
            icon = { Icon(Icons.Rounded.SupervisorAccount, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Main Account Required", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Since you are the main account, you must select a successor to become the new main account before you can delete yourself.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { createNew = false }
                    ) {
                        RadioButton(
                            selected = !createNew,
                            onClick = { createNew = false }
                        )
                        Text("Select existing user", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }

                    if (!createNew) {
                        val otherProfs = allProfiles.filter { it.id != activeProfile.id }
                        if (otherProfs.isEmpty()) {
                            Text(
                                "No other users found.",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 32.dp)
                            )
                        } else {
                            if (selectedSuccessorId.isEmpty() && otherProfs.isNotEmpty()) selectedSuccessorId = otherProfs.first().id
                            Column(modifier = Modifier.padding(start = 32.dp)) {
                                otherProfs.forEach { prof ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedSuccessorId = prof.id }
                                    ) {
                                        RadioButton(
                                            selected = selectedSuccessorId == prof.id,
                                            onClick = { selectedSuccessorId = prof.id }
                                        )
                                        Text(prof.name, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { createNew = true }
                    ) {
                        RadioButton(
                            selected = createNew,
                            onClick = { createNew = true }
                        )
                        Text("Create new account", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }

                    if (createNew) {
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("New Account Name") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .padding(start = 32.dp)
                                .fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                val canSubmit = if (createNew) newName.isNotBlank() else (selectedSuccessorId.isNotEmpty() && allProfiles.any { it.id != activeProfile.id })
                Button(
                    onClick = {
                        viewModel.switchMainAccountAndDeleteCurrent(
                            successorId = selectedSuccessorId,
                            createNew = createNew,
                            newName = newName,
                            newAvatar = "SC"
                        )
                        showSuccessorDialog = false
                    },
                    enabled = canSubmit,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Delete My Account", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSuccessorDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
