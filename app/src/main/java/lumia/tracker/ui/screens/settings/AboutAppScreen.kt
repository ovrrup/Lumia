package lumia.tracker.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
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
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.screens.settings.components.SettingsActionItemInCard
import lumia.tracker.ui.screens.settings.components.SettingsGroupCard
import lumia.tracker.viewmodel.ScholarViewModel
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutAppScreen(navController: NavController, viewModel: ScholarViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val currentVersion = remember {
        try {
            lumia.tracker.BuildConfig.VERSION_NAME
        } catch (e: Exception) {
            "1.0.3"
        }
    }

    // Preferences for Auto Update check
    val profMgr = remember { lumia.tracker.data.ProfileManager(context) }
    val prefs = remember { profMgr.getProfilePrefs() }
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

                                if (lumia.tracker.util.VersionUtils.isUpdateAvailable(currentVersion, tagName)) {
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "About Lumia",
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
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // 1. App Logo & Brand Hero Card
            ScholarCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
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
                            modifier = Modifier.size(38.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        "Lumia",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    ) {
                        Text(
                            "v$currentVersion-foss",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        text = "A beautiful, 100% offline-first academic tracker and focus cockpit built with Material 3 design, local Room databases, LogDog crash safety guard, and True Always-On Display power saving.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }

            // 2. Community & GitHub Listings
            SettingsGroupCard(title = "Community & Sources", icon = Icons.Rounded.Code) {
                SettingsActionItemInCard(
                    title = "Creator GitHub Profile",
                    subtitle = "github.com/ovrrup",
                    icon = Icons.Rounded.Person,
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ovrrup"))
                        try { context.startActivity(intent) } catch (e: Exception) {}
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                SettingsActionItemInCard(
                    title = "GitHub Source Repository",
                    subtitle = "github.com/ovrrup/Lumia",
                    icon = Icons.Rounded.Code,
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ovrrup/Lumia"))
                        try { context.startActivity(intent) } catch (e: Exception) {}
                    }
                )
            }

            // 3. Open Source Version Control & Updates
            SettingsGroupCard(title = "Version Control & Updates", icon = Icons.Rounded.SystemUpdate) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-Check Updates on Startup", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
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

                // Manual Check Panel
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { runUpdateCheck() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = updateState != "checking"
                    ) {
                        if (updateState == "checking") {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Checking Releases...")
                        } else {
                            Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Check for Updates Now", fontWeight = FontWeight.Bold)
                        }
                    }

                    when (updateState) {
                        "available" -> {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.NewReleases, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "New Update Available: $updateTagName",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    Text(
                                        text = "A new formal release is ready. You can download the latest APK asset directly from GitHub Releases.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                    )

                                    if (updateNotes.isNotEmpty()) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = updateNotes,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(10.dp)
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            val uriString = if (updateApkUrl.isNotEmpty()) updateApkUrl else "https://github.com/ovrrup/Lumia/releases/latest"
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString))
                                            try { context.startActivity(intent) } catch (e: Exception) {}
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Download from GitHub", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        "latest" -> {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF4BC27D).copy(alpha = 0.15f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Lumia FOSS is up to date (v$currentVersion)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF2E7D32),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                        "error" -> {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "Offline or server rate-limit. Local functionality remains 100% active.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Legal & Open Source Licensing
            SettingsGroupCard(title = "Legal & Open Source", icon = Icons.Rounded.Gavel) {
                // Terms
                val termsRotation by animateFloatAsState(targetValue = if (showTerms) 90f else 0f, label = "termsRotate")
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTerms = !showTerms }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Terms & Conditions Agreement", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("Read local user accountability parameters", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.rotate(termsRotation)
                        )
                    }
                    AnimatedVisibility(
                        visible = showTerms,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Surface(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
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
                val privacyRotation by animateFloatAsState(targetValue = if (showPrivacy) 90f else 0f, label = "privacyRotate")
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPrivacy = !showPrivacy }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Local Privacy Policy", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("100% serverless, zero telemetry operations", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.rotate(privacyRotation)
                        )
                    }
                    AnimatedVisibility(
                        visible = showPrivacy,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Surface(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ) {
                            Text(
                                text = "1. Zero Telemetry\n" +
                                        "Lumia operates completely offline. No tracking SDKs, no external ads, no user data harvesting.\n\n" +
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
                val licenseRotation by animateFloatAsState(targetValue = if (showLicense) 90f else 0f, label = "licenseRotate")
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLicense = !showLicense }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("GNU GPLv3 Open Source License", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("Verification parameters and open source rights", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.rotate(licenseRotation)
                        )
                    }
                    AnimatedVisibility(
                        visible = showLicense,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Surface(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
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

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
