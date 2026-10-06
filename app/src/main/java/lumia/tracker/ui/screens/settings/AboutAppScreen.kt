package lumia.tracker.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.BouncyOutlinedButton
import lumia.tracker.ui.components.BouncyTextButton
import lumia.tracker.ui.components.GlassCapsule
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.components.ScholarCardDefaults
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.viewmodel.ScholarViewModel
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale

private data class ReleaseAssetInfo(
    val name: String,
    val downloadUrl: String,
    val size: Long
)

@ValueScore(
    score = 90,
    importance = Importance.HIGH,
    description = "Application specifications, dual-channel update checker (Stable/Nightly), and legal disclosures",
    category = "Settings"
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutAppScreen(navController: NavController, viewModel: ScholarViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val packageInfo = remember {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
        } catch (e: Exception) {
            null
        }
    }

    val currentVersion = remember {
        try {
            lumia.tracker.BuildConfig.VERSION_NAME
        } catch (e: Exception) {
            packageInfo?.versionName ?: "1.0.7"
        }
    }

    val currentVersionCode = remember {
        try {
            lumia.tracker.BuildConfig.VERSION_CODE
        } catch (e: Exception) {
            packageInfo?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    it.longVersionCode.toInt()
                } else {
                    @Suppress("DEPRECATION")
                    it.versionCode
                }
            } ?: 7
        }
    }

    val lastUpdateTime = remember {
        packageInfo?.lastUpdateTime ?: 0L
    }

    val primaryAbi = remember {
        Build.SUPPORTED_ABIS?.firstOrNull() ?: "Universal"
    }

    val profMgr = remember { lumia.tracker.data.ProfileManager(context) }
    val prefs = remember { profMgr.getProfilePrefs() }

    // Channel preference: "stable" vs "nightly" (defaults to "nightly" on nightly branch)
    var selectedChannel by remember {
        mutableStateOf(prefs.getString("update_channel", "nightly") ?: "nightly")
    }

    var autoCheckEnabled by remember {
        mutableStateOf(prefs.getBoolean("auto_check_updates", true))
    }

    // Update Checker Status: "idle", "checking", "available", "latest", "error"
    var updateState by remember { mutableStateOf("idle") }
    var updateTagName by remember { mutableStateOf("") }
    var updateReleaseName by remember { mutableStateOf("") }
    var updateNotes by remember { mutableStateOf("") }
    var updatePublishedAt by remember { mutableStateOf("") }
    var updateApkUrl by remember { mutableStateOf("") }
    var updateApkName by remember { mutableStateOf("") }
    var updateApkSize by remember { mutableLongStateOf(0L) }
    var updateHtmlUrl by remember { mutableStateOf("") }
    var updateError by remember { mutableStateOf("") }

    // Dialog state
    var showLicense by remember { mutableStateOf(false) }
    var showPrivacy by remember { mutableStateOf(false) }
    var showTerms by remember { mutableStateOf(false) }
    var showNotesDialog by remember { mutableStateOf(false) }

    fun selectBestApk(assets: List<ReleaseAssetInfo>): ReleaseAssetInfo? {
        if (assets.isEmpty()) return null
        val supportedAbis = Build.SUPPORTED_ABIS ?: emptyArray()
        val primary = supportedAbis.firstOrNull()?.lowercase() ?: "arm64-v8a"

        // 1. Primary ABI non-technical
        val abiMatch = assets.firstOrNull {
            it.name.lowercase().contains(primary) && !it.name.lowercase().contains("technical")
        }
        if (abiMatch != null) return abiMatch

        // 2. Universal non-technical
        val universalMatch = assets.firstOrNull {
            (it.name.lowercase().contains("universal") ||
             it.name.equals("lumia.apk", ignoreCase = true) ||
             it.name.equals("lumia-nightly.apk", ignoreCase = true)) &&
            !it.name.lowercase().contains("technical")
        }
        if (universalMatch != null) return universalMatch

        // 3. Any non-technical
        val anyNonTech = assets.firstOrNull { !it.name.lowercase().contains("technical") }
        if (anyNonTech != null) return anyNonTech

        // 4. Fallback
        return assets.firstOrNull()
    }

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0L) return ""
        val mb = bytes / (1024.0 * 1024.0)
        return String.format(Locale.US, "%.1f MB", mb)
    }

    fun formatPublishedDate(iso: String): String {
        if (iso.isBlank()) return ""
        return try {
            val millis = lumia.tracker.util.VersionUtils.parseIsoTimestamp(iso)
            if (millis <= 0L) return iso.take(10)
            val sdf = SimpleDateFormat("MMM d, yyyy", Locale.US)
            sdf.format(java.util.Date(millis))
        } catch (e: Exception) {
            iso.take(10)
        }
    }

    fun runUpdateCheck(channel: String = selectedChannel) {
        updateState = "checking"
        updateError = ""
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val endpoint = if (channel == "nightly") {
                    "https://api.github.com/repos/ovrrup/Lumia/releases/tags/nightly"
                } else {
                    "https://api.github.com/repos/ovrrup/Lumia/releases/latest"
                }

                val url = URL(endpoint)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", "Lumia-FOSS-App")
                    setRequestProperty("Accept", "application/vnd.github.v3+json")
                    connectTimeout = 8000
                    readTimeout = 8000
                }

                val responseCode = connection.responseCode
                if (responseCode == 200) {
                    val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(responseText)

                    val tagName = json.optString("tag_name", "")
                    val releaseName = json.optString("name", tagName)
                    val body = json.optString("body", "No release notes provided.")
                    val publishedAt = json.optString("published_at", "")
                    val htmlUrl = json.optString("html_url", "https://github.com/ovrrup/Lumia/releases")

                    val assetList = mutableListOf<ReleaseAssetInfo>()
                    val assets = json.optJSONArray("assets")
                    if (assets != null) {
                        for (i in 0 until assets.length()) {
                            val asset = assets.optJSONObject(i) ?: continue
                            val name = asset.optString("name", "")
                            val downloadUrl = asset.optString("browser_download_url", "")
                            val size = asset.optLong("size", 0L)
                            if (name.endsWith(".apk")) {
                                assetList.add(ReleaseAssetInfo(name, downloadUrl, size))
                            }
                        }
                    }

                    val bestApk = selectBestApk(assetList)

                    withContext(Dispatchers.Main) {
                        updateTagName = tagName
                        updateReleaseName = releaseName
                        updateNotes = body
                        updatePublishedAt = publishedAt
                        updateHtmlUrl = htmlUrl
                        updateApkUrl = bestApk?.downloadUrl ?: ""
                        updateApkName = bestApk?.name ?: ""
                        updateApkSize = bestApk?.size ?: 0L

                        if (channel == "nightly") {
                            val isNewer = lumia.tracker.util.VersionUtils.isNightlyNewer(lastUpdateTime, publishedAt)
                            updateState = if (isNewer) "available" else "latest"
                        } else {
                            val isNewer = lumia.tracker.util.VersionUtils.isUpdateAvailable(currentVersion, tagName)
                            updateState = if (isNewer) "available" else "latest"
                        }
                    }
                } else if (responseCode == 404 && channel == "nightly") {
                    // Fallback to releases list for nightly tag or pre-release
                    val fallbackUrl = URL("https://api.github.com/repos/ovrrup/Lumia/releases")
                    val fallbackConn = (fallbackUrl.openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        setRequestProperty("User-Agent", "Lumia-FOSS-App")
                        connectTimeout = 8000
                        readTimeout = 8000
                    }
                    if (fallbackConn.responseCode == 200) {
                        val fallbackText = fallbackConn.inputStream.bufferedReader().use { it.readText() }
                        val array = org.json.JSONArray(fallbackText)
                        var foundRelease: JSONObject? = null
                        for (i in 0 until array.length()) {
                            val item = array.optJSONObject(i) ?: continue
                            if (item.optString("tag_name") == "nightly" || item.optBoolean("prerelease", false)) {
                                foundRelease = item
                                break
                            }
                        }
                        if (foundRelease != null) {
                            val tagName = foundRelease.optString("tag_name", "")
                            val releaseName = foundRelease.optString("name", tagName)
                            val body = foundRelease.optString("body", "No release notes provided.")
                            val publishedAt = foundRelease.optString("published_at", "")
                            val htmlUrl = foundRelease.optString("html_url", "")

                            val assetList = mutableListOf<ReleaseAssetInfo>()
                            val assets = foundRelease.optJSONArray("assets")
                            if (assets != null) {
                                for (i in 0 until assets.length()) {
                                    val asset = assets.optJSONObject(i) ?: continue
                                    val name = asset.optString("name", "")
                                    val downloadUrl = asset.optString("browser_download_url", "")
                                    val size = asset.optLong("size", 0L)
                                    if (name.endsWith(".apk")) {
                                        assetList.add(ReleaseAssetInfo(name, downloadUrl, size))
                                    }
                                }
                            }

                            val bestApk = selectBestApk(assetList)
                            withContext(Dispatchers.Main) {
                                updateTagName = tagName
                                updateReleaseName = releaseName
                                updateNotes = body
                                updatePublishedAt = publishedAt
                                updateHtmlUrl = htmlUrl
                                updateApkUrl = bestApk?.downloadUrl ?: ""
                                updateApkName = bestApk?.name ?: ""
                                updateApkSize = bestApk?.size ?: 0L
                                val isNewer = lumia.tracker.util.VersionUtils.isNightlyNewer(lastUpdateTime, publishedAt)
                                updateState = if (isNewer) "available" else "latest"
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                updateState = "error"
                                updateError = "No nightly release found on GitHub."
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            updateState = "error"
                            updateError = "GitHub returned status $responseCode."
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        updateState = "error"
                        updateError = "GitHub returned status $responseCode."
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    updateState = "error"
                    updateError = e.localizedMessage ?: "Network connection error"
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (autoCheckEnabled) {
            runUpdateCheck(selectedChannel)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "About Lumia",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
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

            // 1. HERO IDENTITY CARD
            ScholarCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(68.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        border = ScholarCardDefaults.border(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                        shadowElevation = 0.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.School,
                                contentDescription = "Lumia Logo",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Lumia Tracker",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Open-source academic companion & focus terminal",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Pills Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlassCapsule(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                )
                                Text(
                                    text = "v$currentVersion (Build $currentVersionCode)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        GlassCapsule(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "GNU GPLv3",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Quick Action Circular Buttons
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AboutActionButton(
                            icon = Icons.Rounded.Code,
                            label = "Source",
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ovrrup/Lumia"))
                                context.startActivity(intent)
                            }
                        )

                        AboutActionButton(
                            icon = Icons.Rounded.BugReport,
                            label = "Issues",
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ovrrup/Lumia/issues"))
                                context.startActivity(intent)
                            }
                        )

                        AboutActionButton(
                            icon = Icons.Rounded.History,
                            label = "Releases",
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ovrrup/Lumia/releases"))
                                context.startActivity(intent)
                            }
                        )

                        AboutActionButton(
                            icon = Icons.Rounded.Gavel,
                            label = "License",
                            onClick = { showLicense = true }
                        )
                    }
                }
            }

            // 2. SOFTWARE UPDATES CARD (WITH SELECTIVE CHANNEL PANEL)
            ScholarCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.SystemUpdate,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "Software Updates",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "OTA distribution from GitHub",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        BouncyButton(
                            onClick = { runUpdateCheck(selectedChannel) },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("Check", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }

                    // SELECTIVE CHANNEL PANEL
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Release Channel",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            ChannelTab(
                                title = "Stable",
                                subtitle = "Vetted releases",
                                isSelected = selectedChannel == "stable",
                                onClick = {
                                    if (selectedChannel != "stable") {
                                        selectedChannel = "stable"
                                        prefs.edit().putString("update_channel", "stable").apply()
                                        runUpdateCheck("stable")
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )

                            ChannelTab(
                                title = "Nightly",
                                subtitle = "Bleeding-edge",
                                isSelected = selectedChannel == "nightly",
                                onClick = {
                                    if (selectedChannel != "nightly") {
                                        selectedChannel = "nightly"
                                        prefs.edit().putString("update_channel", "nightly").apply()
                                        runUpdateCheck("nightly")
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // STATUS SECTION
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                        border = ScholarCardDefaults.border()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (updateState) {
                                            "available" -> MaterialTheme.colorScheme.primaryContainer
                                            "latest" -> Color(0xFF34C759).copy(alpha = 0.18f)
                                            "error" -> MaterialTheme.colorScheme.errorContainer
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (updateState) {
                                        "checking" -> Icons.Rounded.Sync
                                        "available" -> Icons.Rounded.NewReleases
                                        "latest" -> Icons.Rounded.CheckCircle
                                        "error" -> Icons.Rounded.WarningAmber
                                        else -> Icons.Rounded.CloudDownload
                                    },
                                    contentDescription = null,
                                    tint = when (updateState) {
                                        "available" -> MaterialTheme.colorScheme.primary
                                        "latest" -> Color(0xFF34C759)
                                        "error" -> MaterialTheme.colorScheme.error
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = when (updateState) {
                                        "checking" -> "Checking GitHub ($selectedChannel)..."
                                        "available" -> "Update Available"
                                        "latest" -> "Up to date"
                                        "error" -> "Update check unavailable"
                                        else -> "Channel: ${if (selectedChannel == "nightly") "Nightly" else "Stable"}"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Text(
                                    text = when (updateState) {
                                        "checking" -> "Connecting to repository API..."
                                        "available" -> updateReleaseName.ifBlank { updateTagName }
                                        "latest" -> if (selectedChannel == "nightly") {
                                            "Running latest nightly build"
                                        } else {
                                            "Installed: v$currentVersion"
                                        }
                                        "error" -> updateError
                                        else -> "Tap Check to search for newer builds"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (updatePublishedAt.isNotBlank() && (updateState == "available" || updateState == "latest")) {
                                    Text(
                                        text = "Published: ${formatPublishedDate(updatePublishedAt)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    // DOWNLOAD APK & CHANGELOG BUTTONS
                    if (updateApkUrl.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            val apkSizeText = formatFileSize(updateApkSize)
                            val downloadLabel = if (updateState == "available") {
                                "Download APK ($apkSizeText)".trim()
                            } else {
                                "Re-download APK ($apkSizeText)".trim()
                            }

                            BouncyButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateApkUrl))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(downloadLabel, fontWeight = FontWeight.Bold)
                            }

                            if (updateNotes.isNotBlank()) {
                                BouncyOutlinedButton(
                                    onClick = { showNotesDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(Icons.Rounded.Notes, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("View Release Notes", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    // AUTO CHECK SWITCH
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-check on startup",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Silently check for new builds when opening app",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = autoCheckEnabled,
                            onCheckedChange = {
                                autoCheckEnabled = it
                                prefs.edit().putBoolean("auto_check_updates", it).apply()
                            }
                        )
                    }
                }
            }

            // 3. TECHNICAL SPECIFICATIONS CARD
            ScholarCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Terminal,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "Technical Specifications",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Verified runtime environment parameters",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SpecRow(label = "Application ID", value = context.packageName)
                        SpecRow(label = "Version Name", value = currentVersion)
                        SpecRow(label = "Version Code", value = currentVersionCode.toString())
                        SpecRow(label = "Target SDK", value = "${context.applicationInfo.targetSdkVersion} (Android 15)")
                        SpecRow(label = "Minimum SDK", value = "24 (Android 7.0)")
                        SpecRow(label = "Primary ABI", value = primaryAbi)
                        SpecRow(label = "Database Engine", value = "Local SQLite Room")
                        SpecRow(label = "Telemetry", value = "Zero Remote Analytics")
                        SpecRow(label = "License", value = "GNU GPL-3.0")
                    }
                }
            }

            // 4. OPEN SOURCE & LEGAL DISCLOSURES CARD
            ScholarCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Security,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "Legal & Privacy Disclosures",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Factual terms and local privacy guarantees",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        LegalDisclosureItem(
                            title = "GNU General Public License v3.0",
                            subtitle = "Guaranteed freedom to use, study, share and modify",
                            icon = Icons.Rounded.Gavel,
                            onClick = { showLicense = true }
                        )

                        LegalDisclosureItem(
                            title = "Local Privacy Policy",
                            subtitle = "100% offline, zero-tracking analytical architecture",
                            icon = Icons.Rounded.Lock,
                            onClick = { showPrivacy = true }
                        )

                        LegalDisclosureItem(
                            title = "Terms & Conditions",
                            subtitle = "Local storage accountability and software terms",
                            icon = Icons.Rounded.Description,
                            onClick = { showTerms = true }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // DIALOG: GNU GPLv3 LICENSE
    if (showLicense) {
        AlertDialog(
            onDismissRequest = { showLicense = false },
            shape = RoundedCornerShape(32.dp),
            icon = {
                Icon(
                    Icons.Rounded.Gavel,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    "GNU General Public License v3.0",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Lumia Tracker is free and open-source software, licensed under the GNU General Public License Version 3 (GPLv3).\n\n" +
                               "Guaranteed User Freedoms:\n" +
                               "• Use: You are free to run the software for any purpose (academic, research, or commercial).\n" +
                               "• Study: You are free to study how the program works and modify it to suit your needs.\n" +
                               "• Share: You are free to redistribute copies of the software to help others.\n" +
                               "• Improve: You are free to distribute modified versions, provided changes remain open-source under the same GPLv3 copyleft terms.\n\n" +
                               "No Warranty Disclaimer:\n" +
                               "THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.\n\n" +
                               "Copyright (C) 2026 Lumia Contributors.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                BouncyButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ovrrup/Lumia/blob/nightly/LICENSE"))
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("View Full License")
                }
            },
            dismissButton = {
                BouncyTextButton(onClick = { showLicense = false }) {
                    Text("Close")
                }
            }
        )
    }

    // DIALOG: PRIVACY POLICY
    if (showPrivacy) {
        AlertDialog(
            onDismissRequest = { showPrivacy = false },
            shape = RoundedCornerShape(32.dp),
            icon = {
                Icon(
                    Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    "Local Privacy Policy",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "1. Zero Telemetry & Analytics\n" +
                               "Lumia does not contain analytical trackers, advertisement SDKs, or user-harvesting libraries. No analytics data is ever transmitted.\n\n" +
                               "2. 100% Offline Database\n" +
                               "All course nodes, study durations, notes, assignments, streaks, and attendance records are stored exclusively in your local SQLite database on your device.\n\n" +
                               "3. Focused Permission Usage\n" +
                               "All system permissions (Accessibility, System Alert Window, Exact Alarms) are utilized entirely locally on your processor to manage True AOD study timers.\n\n" +
                               "4. Network Utilization\n" +
                               "Network access is strictly limited to user-requested GitHub OTA update checks.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                BouncyButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ovrrup/Lumia/blob/nightly/PRIVACY.md"))
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("GitHub Policy")
                }
            },
            dismissButton = {
                BouncyTextButton(onClick = { showPrivacy = false }) {
                    Text("Close")
                }
            }
        )
    }

    // DIALOG: TERMS & CONDITIONS
    if (showTerms) {
        AlertDialog(
            onDismissRequest = { showTerms = false },
            shape = RoundedCornerShape(32.dp),
            icon = {
                Icon(
                    Icons.Rounded.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    "Terms & Conditions",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "1. Open Source Framework\n" +
                               "Lumia is free software under the GNU GPLv3. You may modify and redistribute the code under the same copyleft terms.\n\n" +
                               "2. Data Responsibility\n" +
                               "Since Lumia operates completely offline without central servers, you are responsible for maintaining your own device backups via the Data Management settings panel.\n\n" +
                               "3. As-Is Grounding\n" +
                               "The software is provided strictly 'as-is' without warranties of any kind regarding reliability or performance.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                BouncyButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ovrrup/Lumia/blob/nightly/TERMS.md"))
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("GitHub Terms")
                }
            },
            dismissButton = {
                BouncyTextButton(onClick = { showTerms = false }) {
                    Text("Close")
                }
            }
        )
    }

    // DIALOG: RELEASE NOTES
    if (showNotesDialog) {
        AlertDialog(
            onDismissRequest = { showNotesDialog = false },
            shape = RoundedCornerShape(32.dp),
            icon = {
                Icon(
                    Icons.Rounded.Notes,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    updateReleaseName.ifBlank { "Release Notes" },
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = updateNotes.ifBlank { "No changelog notes provided." },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                if (updateApkUrl.isNotEmpty()) {
                    BouncyButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateApkUrl))
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Download APK")
                    }
                } else {
                    BouncyButton(
                        onClick = { showNotesDialog = false },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Close")
                    }
                }
            },
            dismissButton = {
                if (updateApkUrl.isNotEmpty()) {
                    BouncyTextButton(onClick = { showNotesDialog = false }) {
                        Text("Close")
                    }
                }
            }
        )
    }
}

@Composable
private fun ChannelTab(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
        border = if (isSelected) ScholarCardDefaults.border() else null,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun AboutActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
            border = ScholarCardDefaults.border(),
            modifier = Modifier.size(50.dp)
        ) {
            BouncyIconButton(
                onClick = onClick,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SpecRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun LegalDisclosureItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.4f),
        border = ScholarCardDefaults.border(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
