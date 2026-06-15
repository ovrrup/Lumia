package ovrrup.lumia.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import ovrrup.lumia.ui.components.BouncyButton
import ovrrup.lumia.ui.components.BouncyIconButton
import ovrrup.lumia.ui.components.BouncyTextButton
import ovrrup.lumia.ui.components.GlassCard
import ovrrup.lumia.ui.theme.glassBar
import ovrrup.lumia.viewmodel.ScholarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataManagementScreen(navController: NavController, viewModel: ScholarViewModel) {
    val status by viewModel.importExportStatus.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    // Custom Theme & Font Management States
    var showColorEditDialog by remember { mutableStateOf<String?>(null) } // "primary", "primary_container", "background", "surface", "text"
    var editColorHexValue by remember { mutableStateOf("") }
    var showAddFontDialog by remember { mutableStateOf(false) }
    var addFontTitle by remember { mutableStateOf("") }
    var addGoogleFontName by remember { mutableStateOf("") }
    var addFontThemeCondition by remember { mutableStateOf("Any") }
    var addFontModeCondition by remember { mutableStateOf("Any") }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.exportData(uri)
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

    val isGlass = ovrrup.lumia.ui.theme.LocalGlassMode.current
    Scaffold(
        containerColor = if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.background,
        topBar = {
            androidx.compose.foundation.layout.Box {
                if (isGlass) {
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
                    title = { Text("Data Management", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        scrolledContainerColor = androidx.compose.ui.graphics.Color.Transparent
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

            // Custom Schemes, Fonts, and Active Features Switchboards
            val customFonts by viewModel.customFonts.collectAsStateWithLifecycle()
            val customPrimary by viewModel.customPrimary.collectAsStateWithLifecycle()
            val customPrimaryContainer by viewModel.customPrimaryContainer.collectAsStateWithLifecycle()
            val customBackground by viewModel.customBackground.collectAsStateWithLifecycle()
            val customSurface by viewModel.customSurface.collectAsStateWithLifecycle()
            val customText by viewModel.customText.collectAsStateWithLifecycle()

            val betaGlassUi by viewModel.betaGlassUi.collectAsStateWithLifecycle()
            val betaFloatingNav by viewModel.betaFloatingNav.collectAsStateWithLifecycle()
            val betaNotes by viewModel.betaNotes.collectAsStateWithLifecycle()
            val betaEnhancedHeader by viewModel.betaEnhancedHeader.collectAsStateWithLifecycle()
            val betaDynamicBackground by viewModel.betaDynamicBackground.collectAsStateWithLifecycle()
            val betaBetterTexts by viewModel.betaBetterTexts.collectAsStateWithLifecycle()
            val betaBetterTextsPalette by viewModel.betaBetterTextsPalette.collectAsStateWithLifecycle()
            val betaDynamicTypography by viewModel.betaDynamicTypography.collectAsStateWithLifecycle()
            val betaNavBarSizeControls by viewModel.betaNavBarSizeControls.collectAsStateWithLifecycle()

            // LogDog Card
            var showLogDogDialog by remember { mutableStateOf(false) }
            val isSentinelActive by viewModel.isSentinelActive.collectAsStateWithLifecycle()
            GlassCard(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("LogDog Diagnostic Handler", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (!isSentinelActive) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Lumia's advanced on-demand diagnostics require the Lumia Sentinel plugin. Activate the companion Micro-APK to query code analyzers and log telemetries.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        BouncyButton(
                            onClick = { navController.navigate("settings/plugins") },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Rounded.Extension, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Get Sentinel Plugin", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black)
                        }
                    } else {
                        SettingsActionItemInCard(
                            title = "Trigger Analysis (Woof!)",
                            subtitle = "Run code analysis and view last captured crash error data with a funny twist.",
                            icon = Icons.Rounded.RecordVoiceOver,
                            onClick = { 
                                showLogDogDialog = true
                            }
                        )
                    }
                }
            }

            if (showLogDogDialog) {
                var isScanning by remember { mutableStateOf(true) }
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(1200)
                    isScanning = false
                }
                var crashesList by remember { mutableStateOf(ovrrup.lumia.util.LogDog.getCrashes(context)) }
                val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                
                androidx.compose.ui.window.Dialog(
                    onDismissRequest = { showLogDogDialog = false },
                    properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    GlassCard(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 24.dp),
                        shape = RoundedCornerShape(32.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.RecordVoiceOver,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            "Diagnostic Logs",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            "LogDog Core Analyzer v1.0",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                BouncyIconButton(onClick = { showLogDogDialog = false }) {
                                    Icon(Icons.Rounded.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                if (isScanning) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(300.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            CircularProgressIndicator(
                                                strokeWidth = 4.dp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(Modifier.height(24.dp))
                                            Text(
                                                "Woof! Sniffing logs...",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                "Parsing captured stack traces",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                } else if (crashesList.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(300.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Box(
                                                modifier = Modifier
                                                    .size(80.dp)
                                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Rounded.CheckCircle,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    modifier = Modifier.size(40.dp)
                                                )
                                            }
                                            Spacer(Modifier.height(24.dp))
                                            Text(
                                                "Lumia is Healthy",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Black,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                "Zero fatal faults detected in the kennel.",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 16.dp, bottom = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "CAPTURED TELEMETRY (${crashesList.size})",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        BouncyTextButton(
                                            onClick = {
                                                ovrrup.lumia.util.LogDog.clearCrashes(context)
                                                crashesList = emptyList()
                                                Toast.makeText(context, "LogDog purged all telemetry.", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                        ) {
                                            Icon(Icons.Rounded.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text("Purge Kernel", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    
                                    crashesList.forEachIndexed { index, crash ->
                                        val parsed = remember(crash) { ovrrup.lumia.util.LogDog.analyzeCrash(crash) }
                                        
                                        GlassCard(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            shape = RoundedCornerShape(20.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    val sevColor = when (parsed.severityLevel) {
                                                        "Critical" -> MaterialTheme.colorScheme.error
                                                        "High" -> MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                                        else -> MaterialTheme.colorScheme.primary
                                                    }
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(8.dp)
                                                                .background(sevColor, CircleShape)
                                                        )
                                                        Spacer(Modifier.width(10.dp))
                                                        Text(
                                                            "ERROR DUMP ${index + 1} • ${parsed.severityLevel.uppercase()}",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Black,
                                                            color = sevColor
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(crash))
                                                            Toast.makeText(context, "Copied trace dump to clipboard!", Toast.LENGTH_SHORT).show()
                                                        },
                                                        modifier = Modifier.size(32.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Rounded.ContentCopy,
                                                            contentDescription = "Copy raw log",
                                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                                
                                                Spacer(modifier = Modifier.height(12.dp))
                                                Text(
                                                    "Module: ${parsed.likelyComponent}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    parsed.exceptionType,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Black,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Surface(
                                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Column(modifier = Modifier.padding(12.dp)) {
                                                        Text(
                                                            "MESSAGE:",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                        Text(
                                                            parsed.errorMessage,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                        
                                                        Spacer(modifier = Modifier.height(12.dp))
                                                        Text(
                                                            "STACK POINTER:",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                        Text(
                                                            parsed.crashLocation,
                                                            style = androidx.compose.ui.text.TextStyle(
                                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                                fontSize = 11.sp,
                                                                color = MaterialTheme.colorScheme.primary
                                                            )
                                                        )
                                                    }
                                                }
                                                
                                                if (parsed.isFrameworkBug) {
                                                    Spacer(modifier = Modifier.height(12.dp))
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                                            .padding(8.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Rounded.Warning,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.error,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                        Spacer(Modifier.width(8.dp))
                                                        Text(
                                                            "Framework component fault detected.",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.error,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                }
                                                
                                                Spacer(modifier = Modifier.height(16.dp))
                                                Surface(
                                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                                                        Icon(
                                                            Icons.Rounded.Info, 
                                                            contentDescription = null, 
                                                            tint = MaterialTheme.colorScheme.primary, 
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Spacer(Modifier.width(10.dp))
                                                        Text(
                                                            parsed.suggestion, 
                                                            style = MaterialTheme.typography.bodySmall, 
                                                            color = MaterialTheme.colorScheme.onSurface,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            GlassCard(
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

            // Storage and Resource Optimizer
            val dbSize by viewModel.dbSize.collectAsStateWithLifecycle()
            val cacheSize by viewModel.cacheSize.collectAsStateWithLifecycle()
            val isOptimizing by viewModel.isOptimizing.collectAsStateWithLifecycle()
            val optimizationResult by viewModel.optimizationResult.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                viewModel.updateStorageMetrics()
            }

            LaunchedEffect(optimizationResult) {
                optimizationResult?.let {
                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                    viewModel.clearOptimizationResult()
                }
            }

            GlassCard(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
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
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Speed,
                                contentDescription = "Storage Status",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Storage & Cache Optimizer",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Compact local database journals and purge temporary caches",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Stats Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Database Size",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = dbSize,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Index & logs",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Temp Caches",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = cacheSize,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "System buffers",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    BouncyButton(
                        onClick = { viewModel.optimizeStorage() },
                        enabled = !isOptimizing,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        if (isOptimizing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Optimizing & checkpointing...",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Speed,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Clean Caches & Compact DB",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            SettingsGroupCard(title = "Engine Customisation & Active Controls", icon = Icons.Rounded.Palette) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Theme Palette Overrides",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Direct live editing of your custom theme hex properties.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                            .padding(8.dp)
                    ) {
                        ColorConfigRow("Primary Accent Color", customPrimary) {
                            editColorHexValue = customPrimary
                            showColorEditDialog = "primary"
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
                        ColorConfigRow("Primary Container", customPrimaryContainer) {
                            editColorHexValue = customPrimaryContainer
                            showColorEditDialog = "primary_container"
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
                        ColorConfigRow("Background Canvas", customBackground) {
                            editColorHexValue = customBackground
                            showColorEditDialog = "background"
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
                        ColorConfigRow("Surface Panels Base", customSurface) {
                            editColorHexValue = customSurface
                            showColorEditDialog = "surface"
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
                        ColorConfigRow("Global Primary Text", customText) {
                            editColorHexValue = customText
                            showColorEditDialog = "text"
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BouncyButton(
                            onClick = {
                                viewModel.generatePaletteFromPrimaryHex(customPrimary)
                                Toast.makeText(context, "Completed smart palette propagation (Hue/Sat HSV offsets calculated)!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            Text("Auto-Generate Rest of Palette", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }

                        BouncyTextButton(
                            onClick = {
                                viewModel.updateCustomColor("primary", "#3197D6")
                                viewModel.updateCustomColor("primary_container", "#DAF1FF")
                                viewModel.updateCustomColor("background", "#FAFAFA")
                                viewModel.updateCustomColor("surface", "#FFFFFF")
                                viewModel.updateCustomColor("text", "#1A1C1A")
                                Toast.makeText(context, "Reset theme slots back to default Blue Slate.", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                            modifier = Modifier.align(Alignment.CenterVertically)
                        ) {
                            Text("Reset Palette", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Custom Fonts Overrides (${customFonts.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Manage Google Fonts triggers bound directly to dynamic matching rules.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (customFonts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No custom font rules defined yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            customFonts.forEach { font ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(font.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                            Text("Font: ${font.fontName} | Rules: [Theme: ${font.conditionTheme}, Mode: ${font.conditionMode}]", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        IconButton(
                                            onClick = {
                                                viewModel.deleteCustomFont(font)
                                                Toast.makeText(context, "Deleted font pairing override", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Rounded.Delete, contentDescription = "Delete font config", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                        }
                                    }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    BouncyButton(
                        onClick = { showAddFontDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        contentPadding = PaddingValues(10.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Custom Font Pairing Override", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Active Components & Features Swapboard",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Instantly regulate visual features and backend micro-compartments.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        FeatureSwitchItem("Dynamic Aero Glass Engine", "Toggle translucent frosted card panels", betaGlassUi) {
                            viewModel.updateBetaGlassUi(it)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
                        FeatureSwitchItem("Responsive Ambient Lights Background", "Renders gradient light sources in standard canvas", betaDynamicBackground) {
                            viewModel.updateBetaDynamicBackground(it)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
                        FeatureSwitchItem("Adaptive Heads-Up Headers", "Injects stylized banners in top menus", betaEnhancedHeader) {
                            viewModel.updateBetaEnhancedHeader(it)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
                        FeatureSwitchItem("Floating Dynamic Bottom Bar", "Transforms bottom navigation into floating capsule look", betaFloatingNav) {
                            viewModel.updateBetaFloatingNav(it)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
                        FeatureSwitchItem("Aesthetic Typography pairing", "Pair elegant title styling layout rules", betaBetterTexts) {
                            viewModel.updateBetaBetterTexts(it)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
                        FeatureSwitchItem("Dynamic Adaptive Sizing", "Optimize text scale for visual hierarchy dynamic sizes", betaDynamicTypography) {
                            viewModel.updateBetaDynamicTypography(it)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
                        FeatureSwitchItem("Experimental Study Notes Module", "Activates floating sticky scratchpad inside details panel", betaNotes) {
                            viewModel.updateBetaNotes(it)
                        }
                    }
                }
            }

            SettingsGroupCard(title = "Backup & Erasure Hub", icon = Icons.Rounded.Storage) {
                SettingsActionItemInCard(
                    title = "Export Secure Data Profile",
                    subtitle = "Back up all settings, customisations, courses and assignments into a portable file",
                    icon = Icons.Rounded.Upload,
                    onClick = { showExportDialog = true }
                )
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                
                SettingsActionItemInCard(
                    title = "Import Secure Data Profile",
                    subtitle = "Restore binary backup. Warning: Completely overwrites current active assets",
                    icon = Icons.Rounded.Download,
                    isDestructive = true,
                    onClick = { openDocumentLauncher.launch(arrayOf("application/octet-stream", "*/*")) }
                )
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                
                SettingsActionItemInCard(
                    title = "Full Environment Factory Erase",
                    subtitle = "Permanently delete all customisations, courses, subjects, assignments, and local history reports",
                    icon = Icons.Rounded.DeleteForever,
                    isDestructive = true,
                    onClick = { showResetDialog = true }
                )
            }
        }
    }

    if (showExportDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Data & Settings") },
            text = { Text("Are you sure you want to export a binary backup of all your data and settings?") },
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
            title = { Text("Erase All Data & Settings?") },
            text = { Text("This action cannot be undone. All your progress, custom themes, subjects, and settings will be permanently removed.", color = MaterialTheme.colorScheme.error) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
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

    if (showColorEditDialog != null) {
        val key = showColorEditDialog!!
        val label = when(key) {
            "primary" -> "Primary Accent"
            "primary_container" -> "Primary Container"
            "background" -> "Background Canvas"
            "surface" -> "Surface Panels"
            "text" -> "Main Text Color"
            else -> key
        }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showColorEditDialog = null },
            title = { Text("Edit Theme Color: $label") },
            text = {
                Column {
                    Text("Specify a standard hex color code (e.g. #3197D6 or 3197D6) to override this custom palette asset.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editColorHexValue,
                        onValueChange = { editColorHexValue = it },
                        placeholder = { Text("#3197D6") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val clean = editColorHexValue.trim()
                        if (clean.isNotEmpty()) {
                            val formated = if (clean.startsWith("#")) clean else "#$clean"
                            try {
                                android.graphics.Color.parseColor(formated) // Validate
                                viewModel.updateCustomColor(key, formated)
                                showColorEditDialog = null
                            } catch (e: Exception) {
                                Toast.makeText(context, "Invalid Hex Color definition", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            showColorEditDialog = null
                        }
                    }
                ) { Text("Apply Change", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showColorEditDialog = null }) { Text("Cancel") }
            }
        )
    }

    if (showAddFontDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAddFontDialog = false },
            title = { Text("Create System Font Override") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Define dynamic font pairing rules injected straight into the Material 3 typography scheduler.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = addFontTitle,
                        onValueChange = { addFontTitle = it },
                        label = { Text("Rule Label / Name") },
                        placeholder = { Text("My Elegant Serif Override") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = addGoogleFontName,
                        onValueChange = { addGoogleFontName = it },
                        label = { Text("Google Font Name") },
                        placeholder = { Text("Playfair Display") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text("Trigger Condition: Theme", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Any", "Ocean", "Emerald", "Rose", "Custom").forEach { theme ->
                            FilterChip(
                                selected = addFontThemeCondition == theme,
                                onClick = { addFontThemeCondition = theme },
                                label = { Text(theme, fontSize = 11.sp) }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Trigger Condition: System Mode", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Any", "Light", "Dark").forEach { mode ->
                            FilterChip(
                                selected = addFontModeCondition == mode,
                                onClick = { addFontModeCondition = mode },
                                label = { Text(mode, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (addFontTitle.trim().isNotEmpty() && addGoogleFontName.trim().isNotEmpty()) {
                            viewModel.addCustomFont(
                                addFontTitle.trim(),
                                addGoogleFontName.trim(),
                                addFontThemeCondition,
                                addFontModeCondition
                            )
                            showAddFontDialog = false
                            addFontTitle = ""
                            addGoogleFontName = ""
                            addFontThemeCondition = "Any"
                            addFontModeCondition = "Any"
                            Toast.makeText(context, "System Font rule saved automatically", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Title and Google Font are mandatory", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) { Text("Save Rule", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showAddFontDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun ColorConfigRow(label: String, hexValue: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = try {
                            androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(hexValue))
                        } catch (e: Exception) {
                            MaterialTheme.colorScheme.primary
                        },
                        shape = CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("Value: $hexValue", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(
            imageVector = Icons.Rounded.Edit,
            contentDescription = "Edit color",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun FeatureSwitchItem(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val isLongDesc = subtitle.split(" ").size > 6

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                if (isLongDesc && subtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = "Info",
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .clickable { expanded = !expanded },
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            if (!isLongDesc || expanded) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}
