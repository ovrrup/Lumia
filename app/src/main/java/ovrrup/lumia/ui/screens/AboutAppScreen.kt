package ovrrup.lumia.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ovrrup.lumia.ui.theme.glassBar
import ovrrup.lumia.viewmodel.ScholarViewModel
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutAppScreen(navController: NavController, viewModel: ScholarViewModel) {
    val isGlass = ovrrup.lumia.ui.theme.LocalGlassMode.current
    val betaEnhancedHeader by viewModel.betaEnhancedHeader.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val currentVersion = remember {
        try {
            ovrrup.lumia.BuildConfig.VERSION_NAME
        } catch (e: Exception) {
            "1.0.5"
        }
    }

    // Preferences for Auto Update check
    val prefs = remember { context.getSharedPreferences("lumia_prefs", Context.MODE_PRIVATE) }
    var autoCheckEnabled by remember { mutableStateOf(prefs.getBoolean("auto_check_updates", true)) }

    // Update Checker Status States: "idle", "checking", "available", "latest", "error"
    var updateState by remember { mutableStateOf("idle") }
    var updateTagName by remember { mutableStateOf("") }
    var updateNotes by remember { mutableStateOf("") }
    var updateApkUrl by remember { mutableStateOf("") }
    var updateError by remember { mutableStateOf("") }

    // Expandable details states
    var showTerms by remember { mutableStateOf(false) }
    var showPrivacy by remember { mutableStateOf(false) }
    var showLicense by remember { mutableStateOf(false) }

    fun runUpdateCheck() {
        updateState = "checking"
        updateError = ""
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val url = URL("https://api.github.com/repos/ovrrup/Lumia/releases/latest")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("User-Agent", "Lumia-FOSS-App")
                connection.connectTimeout = 8000
                connection.readTimeout = 8000
                
                if (connection.responseCode == 200) {
                    val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                    
                    try {
                        val json = JSONObject(responseText)
                        val tagName = json.optString("tag_name", "")
                        val body = json.optString("body", "Check GitHub for release details.")
                        
                        var apkUrl = ""
                        val assets = json.optJSONArray("assets")
                        if (assets != null) {
                            for (i in 0 until assets.length()) {
                                val asset = assets.optJSONObject(i)
                                val name = asset?.optString("name", "") ?: ""
                                if (name.endsWith(".apk")) {
                                    apkUrl = asset?.optString("browser_download_url", "") ?: ""
                                    break
                                }
                            }
                        }
                        
                        withContext(Dispatchers.Main) {
                            if (tagName.isNotEmpty()) {
                                updateTagName = tagName
                                updateNotes = body
                                updateApkUrl = apkUrl
                                
                                if (ovrrup.lumia.util.VersionUtils.isUpdateAvailable(currentVersion, tagName)) {
                                    updateState = "available"
                                } else {
                                    updateState = "latest"
                                }
                            } else {
                                updateState = "error"
                                updateError = "Invalid release format."
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            updateState = "error"
                            updateError = "Failed to parse release info."
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        updateState = "error"
                        updateError = "GitHub server status: ${connection.responseCode}"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    updateState = "error"
                    updateError = e.localizedMessage ?: "No connection"
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (autoCheckEnabled) {
            runUpdateCheck()
        }
    }

    Scaffold(
        containerColor = if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.background,
        topBar = {
            Box {
                if (betaEnhancedHeader || isGlass) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .glassBar(shape = RoundedCornerShape(0.dp))
                    )
                    HorizontalDivider(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                }
                CenterAlignedTopAppBar(
                    title = { Text("About Lumia", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        scrolledContainerColor = androidx.compose.ui.graphics.Color.Transparent
                    ),
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // App Logo & Brand Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.School,
                            contentDescription = "Lumia Logo",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Lumia", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    Text("v$currentVersion-foss", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "A beautiful, 100% offline-first academic tracker and focus cockpit built with Material 3 design, local Room databases, LogDog diagnostic, and the True Always-On Display saving option.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }

            // GitHub & Community Listings
            SettingsGroupCard(title = "Community & Sources", icon = Icons.Rounded.Star) {
                SettingsActionItemInCard(
                    title = "Creator GitHub Profile",
                    subtitle = "github.com/ovrrup",
                    icon = Icons.Rounded.Edit,
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ovrrup"))
                        try { context.startActivity(intent) } catch (e: Exception) {}
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                SettingsActionItemInCard(
                    title = "GitHub Source Repository",
                    subtitle = "Join our development base or clone the code",
                    icon = Icons.Rounded.School,
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ovrrup/Lumia"))
                        try { context.startActivity(intent) } catch (e: Exception) {}
                    }
                )
            }

            // Updates Manager
            SettingsGroupCard(title = "Open Source Version Control", icon = Icons.Rounded.Download) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-Check Updates on Startup", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text("Verify latest releases from public channels", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = autoCheckEnabled,
                        onCheckedChange = {
                            autoCheckEnabled = it
                            prefs.edit().putBoolean("auto_check_updates", it).apply()
                        }
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                // Manual Check panel
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { runUpdateCheck() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = updateState != "checking"
                    ) {
                        Text(if (updateState == "checking") "Checking Repository..." else "Check for Updates Now")
                    }

                    when (updateState) {
                        "checking" -> {
                            Text("Querying GitHub releases API...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        "available" -> {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "Update Available! ($updateTagName)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "A new formal release is available. You can download the latest Lumia.apk directly from the GitHub Releases assets.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                    
                                    if (updateNotes.isNotEmpty()) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = updateNotes,
                                                modifier = Modifier.padding(12.dp),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                    Button(
                                        onClick = {
                                            val uriString = if (updateApkUrl.isNotEmpty()) updateApkUrl else "https://github.com/ovrrup/Lumia/releases/latest"
                                            val intent = Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse(uriString)
                                            )
                                            try { context.startActivity(intent) } catch (e: Exception) {}
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Download,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Go to GitHub Releases")
                                    }
                                }
                            }
                        }
                        "latest" -> {
                            Text("✨ Lumia FOSS is up-to-date (v$currentVersion) with latest release branch.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                        }
                        "error" -> {
                            Text("⚠️ Code status: Offline or DNS limitation. Standard local database remains highly optimized.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            // Terms & Privacy policies
            SettingsGroupCard(title = "Legal terms", icon = Icons.Rounded.Lock) {
                // Terms
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTerms = !showTerms }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Terms & Conditions Agreement", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("Read our local user accountability parameters", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = "Expand",
                            modifier = Modifier.scale(1f, if (showTerms) -1f else 1f)
                        )
                    }
                    if (showTerms) {
                        Card(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "1. Open Source Framework & License\n" +
                                       "Lumia is free software distributed under the GNU GPLv3 terms. You can modify and share code as long as your changes remain public under the same copyleft license.\n\n" +
                                       "2. No Warranty\n" +
                                       "Lumia is provided entirely 'as-is' without warranties of any kind. You are solely responsible for local storage backups.",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                // Privacy
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPrivacy = !showPrivacy }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Local Privacy Policy", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("100% serverless, zero telemetry operations", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = "Expand",
                            modifier = Modifier.scale(1f, if (showPrivacy) -1f else 1f)
                        )
                    }
                    if (showPrivacy) {
                        Card(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "1. Zero Telemetry\n" +
                                       "Lumia operates completely offline. No tracking SDKs, no external ads, no user harvesting.\n\n" +
                                       "2. Safe Permission Utilization\n" +
                                       "All accessibility and overlay requests are completely handled locally on your own CPU to manage focus screen timers.",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                // License
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLicense = !showLicense }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("GNU GPLv3 Open Source License", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("Verification parameters of other distributions", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = "Expand",
                            modifier = Modifier.scale(1f, if (showLicense) -1f else 1f)
                        )
                    }
                    if (showLicense) {
                        Card(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "Lumia is powered by the GNU General Public License v3.0.\n\n" +
                                       "This license grants you the freedom to run, study, share, and modify Lumia. Any public distribution of derivative versions must be open-source and preserve original contributor attributions.",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
