package lumia.tracker.ui.screens

import lumia.tracker.service.AodAccessibilityService
import lumia.tracker.util.TrueAodManager
import android.content.Intent
import android.provider.Settings
import lumia.tracker.ui.theme.liquidGlass
import lumia.tracker.ui.theme.glassBar
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Close
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.scale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.CropFree
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.MergeType
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.ViewQuilt
import androidx.compose.material.icons.rounded.Accessibility
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.InvertColors
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.viewmodel.ScholarViewModel
import lumia.tracker.model.TagCustomization
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyTextButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataManagementScreen(navController: NavController, viewModel: ScholarViewModel) {
    val status by viewModel.importExportStatus.collectAsStateWithLifecycle()
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val dbStats by viewModel.dbStatistics.collectAsStateWithLifecycle()
    val defragText by viewModel.defragStatus.collectAsStateWithLifecycle()
    val tagCustomizations by viewModel.allTagCustomizations.collectAsStateWithLifecycle()
    

    val context = LocalContext.current
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

    val betaEnhancedHeader by viewModel.betaEnhancedHeader.collectAsStateWithLifecycle()
    val isGlass = lumia.tracker.ui.theme.LocalGlassMode.current
    Scaffold(
        containerColor = if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.background,
        topBar = {
            androidx.compose.foundation.layout.Box {
                if (betaEnhancedHeader || isGlass) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .matchParentSize()
                            .glassBar(shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp))
                    )
                    androidx.compose.material3.HorizontalDivider(
                        modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                }
                CenterAlignedTopAppBar(
                    title = { Text("Data Management", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = if (betaEnhancedHeader || isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = if (betaEnhancedHeader || isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                    )
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            val coursesCount by viewModel.courses.collectAsStateWithLifecycle()
            val assignmentsCount by viewModel.assignments.collectAsStateWithLifecycle()
            val subjectsCount by viewModel.subjects.collectAsStateWithLifecycle()
            val pomodoroSessionsCount by viewModel.pomodoroSessions.collectAsStateWithLifecycle()


            lumia.tracker.ui.components.GlassCard(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Storage,
                                contentDescription = "Active Database Info",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Database Integrity Status",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Secure active binary protection enabled",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(text = coursesCount.size.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            Text(text = "Courses", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(text = subjectsCount.size.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            Text(text = "Subjects", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(text = assignmentsCount.size.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            Text(text = "Assignments", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(text = pomodoroSessionsCount.size.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            Text(text = "Pomodoros", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Advanced Data Management Section (Plus Feature)
            SettingsGroupCard(title = "Advanced Schema & Diagnostics", icon = Icons.Rounded.Storage) {
                
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "SQLite Local Schema Metrics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "View row counts of the physical application databases in real-time.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        if (dbStats.isEmpty()) {
                            Button(
                                onClick = { viewModel.loadDBStatistics() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Analyze Schema Metrics")
                                }
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
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(
                                    onClick = { viewModel.loadDBStatistics() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Re-Analyze Database")
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            "Index Pack compacting & SQLite VACUUM",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            "Rebuild database indices, clean orphaned assignments, and run VACUUM optimization commands to decrease storage allocations.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.defragmentDatabase() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            enabled = defragText.isEmpty() || defragText.startsWith("Optimized!"),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Execute SQLite Defrag")
                            }
                        }

                        if (defragText.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = defragText,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (defragText.startsWith("Optimized!")) Color(0xFF4BC27D) else MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tag Connectivity & Alignment Card
            SettingsGroupCard(title = "Tag Connectivity & Maintenance", icon = Icons.Rounded.LocalOffer) {
                SettingsActionItemInCard(
                    title = "Tag Management (Network Explorer)",
                    subtitle = "View connection graphs and customize tag aesthetics",
                    icon = Icons.Rounded.LocalOffer,
                    onClick = { navController.navigate("tags_hub") }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Align Tag Databases",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Synchronize academic entities, remove orphaned tag customizations, and verify integrity of tag associations.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    var alignStatus by remember { mutableStateOf("") }
                    var loadingAlign by remember { mutableStateOf(false) }
                    val scope = rememberCoroutineScope()

                    Button(
                        onClick = {
                            scope.launch {
                                loadingAlign = true
                                alignStatus = "Verifying tag mappings..."
                                kotlinx.coroutines.delay(1000)
                                // Scan all tags and sync
                                val tagsInDb = mutableSetOf<String>()
                                viewModel.courses.value.forEach { c -> c.tags.split(",").forEach { if(it.isNotBlank()) tagsInDb.add(it.trim().lowercase()) } }
                                viewModel.subjects.value.forEach { s -> s.tags.split(",").forEach { if(it.isNotBlank()) tagsInDb.add(it.trim().lowercase()) } }
                                viewModel.allTopics.value.forEach { t -> t.tags.split(",").forEach { if(it.isNotBlank()) tagsInDb.add(it.trim().lowercase()) } }
                                viewModel.tasks.value.forEach { t -> t.tags.split(",").forEach { if(it.isNotBlank()) tagsInDb.add(it.trim().lowercase()) } }
                                viewModel.allChapters.value.forEach { ch -> ch.tags.split(",").forEach { if(it.isNotBlank()) tagsInDb.add(it.trim().lowercase()) } }
                                viewModel.allTestRecords.value.forEach { tr -> tr.tags.split(",").forEach { if(it.isNotBlank()) tagsInDb.add(it.trim().lowercase()) } }

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
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                        enabled = !loadingAlign,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Scan & Align Tag Associations")
                        }
                    }

                    if (alignStatus.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = alignStatus,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text("Clear Custom Colors & Notes", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            Text("Erase all tag customizations (custom colors, notes, favorite states) but keep associations intact.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(
                            onClick = {
                                scope.launch {
                                    tagCustomizations.forEach { viewModel.deleteTagCustomization(it.tagName) }
                                    alignStatus = "Cleared all tag metadata customizations!"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                            modifier = Modifier.wrapContentSize()
                        ) {
                            Text("Reset Metas")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsGroupCard(title = "My Data Management", icon = Icons.Rounded.Person) {
                SettingsActionItemInCard(
                    title = "Export My Data",
                    subtitle = "Back up your own profile's settings, tasks, and data",
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
                    isDestructive = true,
                    onClick = { openDocumentLauncher.launch(arrayOf("application/octet-stream", "*/*")) }
                )
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                
                SettingsActionItemInCard(
                    title = "Erase My Data & Delete Account",
                    subtitle = "Permanently delete your profile and all your data",
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

            if (activeProfile.isDefault) {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsGroupCard(title = "Collective Data Management", icon = Icons.Rounded.Lock) {
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
        }
    }

    if (showExportDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Data & Settings") },
            text = { Text(if (exportAllMode) "Export a backup of ALL user accounts?" else "Export a backup of YOUR data?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExportDialog = false
                        createDocumentLauncher.launch("scholar_backup.bin")
                    }
                ) { Text("Export") }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showResetDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(if (resetTarget == "all") "Erase All App Data?" else "Erase Data & Delete Account?") },
            text = { Text(if (resetTarget == "all") "This action cannot be undone. ALL user accounts and their data will be permanently removed." else "This action cannot be undone. Your profile and all your data will be permanently removed.", color = MaterialTheme.colorScheme.error) },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (resetTarget == "all") {
                            viewModel.clearAllData()
                        } else {
                            viewModel.eraseMyDataAndAccount()
                        }
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Erase Data", fontWeight = FontWeight.Black)
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

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showSuccessorDialog = false },
            title = { Text("Main Account Required", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Since you are the main account, you must select a successor to become the new main account before you can delete yourself.")
                    Spacer(Modifier.height(16.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { createNew = false }) {
                        androidx.compose.material3.RadioButton(
                            selected = !createNew,
                            onClick = { createNew = false }
                        )
                        Text("Select existing user")
                    }
                    if (!createNew) {
                        val otherProfs = allProfiles.filter { it.id != activeProfile.id }
                        if (otherProfs.isEmpty()) {
                            Text("No other users found.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 32.dp))
                        } else {
                            if (selectedSuccessorId.isEmpty() && otherProfs.isNotEmpty()) selectedSuccessorId = otherProfs.first().id
                            Column(modifier = Modifier.padding(start = 32.dp)) {
                                otherProfs.forEach { prof ->
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { selectedSuccessorId = prof.id }) {
                                        androidx.compose.material3.RadioButton(
                                            selected = selectedSuccessorId == prof.id,
                                            onClick = { selectedSuccessorId = prof.id }
                                        )
                                        Text(prof.name)
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { createNew = true }) {
                        androidx.compose.material3.RadioButton(
                            selected = createNew,
                            onClick = { createNew = true }
                        )
                        Text("Create new account")
                    }
                    if (createNew) {
                        androidx.compose.material3.OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("New Account Name") },
                            modifier = Modifier.padding(start = 32.dp).fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                val canSubmit = if (createNew) newName.isNotBlank() else (selectedSuccessorId.isNotEmpty() && allProfiles.any { it.id != activeProfile.id })
                TextButton(
                    onClick = {
                        viewModel.switchMainAccountAndDeleteCurrent(
                            successorId = selectedSuccessorId,
                            createNew = createNew,
                            newName = newName,
                            newAvatar = "😊"
                        )
                        showSuccessorDialog = false
                    },
                    enabled = canSubmit,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete My Account", fontWeight = FontWeight.Bold)
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
