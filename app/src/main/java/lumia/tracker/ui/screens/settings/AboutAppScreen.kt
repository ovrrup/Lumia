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
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.screens.settings.components.SettingsActionItemInCard
import lumia.tracker.ui.screens.settings.components.SettingsGroupCard
import lumia.tracker.viewmodel.ScholarViewModel
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

@ValueScore(
    score = 85,
    importance = Importance.HIGH,
    description = "Application info, GitHub release update checker, license and privacy disclosures",
    category = "Settings"
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutAppScreen(navController: NavController, viewModel: ScholarViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val currentVersion = remember {
        try {
            lumia.tracker.BuildConfig.VERSION_NAME
        } catch (e: Exception) {
            "1.0.7"
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
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // 1. Hero Brand Card
            ScholarCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.School,
                            contentDescription = "Lumia Logo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Lumia Tracker",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = "v$currentVersion",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Academic companion for students and researchers.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 2. GitHub Release Checker Card
            SettingsGroupCard(title = "Software Updates", icon = Icons.Rounded.SystemUpdate) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(
                                imageVector = when (updateState) {
                                    "available" -> Icons.Rounded.NewReleases
                                    "latest" -> Icons.Rounded.CheckCircle
                                    "checking" -> Icons.Rounded.Sync
                                    else -> Icons.Rounded.CloudDownload
                                },
                                contentDescription = null,
                                tint = when (updateState) {
                                    "available" -> MaterialTheme.colorScheme.primary
                                    "latest" -> Color(0xFF34C759)
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = when (updateState) {
                                        "checking" -> "Checking GitHub for updates..."
                                        "available" -> "Update Available: $updateTagName"
                                        "latest" -> "You're on the latest version"
                                        "error" -> "Update check unavailable"
                                        else -> "Check for Updates"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Current: v$currentVersion",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        BouncyButton(
                            onClick = { runUpdateCheck() },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Check Now", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    if (updateState == "available" && updateApkUrl.isNotEmpty()) {
                        BouncyButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateApkUrl))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Download APK ($updateTagName)")
                        }
                    }
                }
            }

            // 3. Open Source & Repositories
            SettingsGroupCard(title = "Community & Open Source", icon = Icons.Rounded.Code) {
                SettingsActionItemInCard(
                    title = "GitHub Repository",
                    subtitle = "Source code, contributions, and issues",
                    icon = Icons.Rounded.Terminal,
                    iconBgColor = Color(0xFF24292E),
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ovrrup/Lumia"))
                        context.startActivity(intent)
                    }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                    modifier = Modifier.padding(start = 56.dp, end = 8.dp)
                )

                SettingsActionItemInCard(
                    title = "MIT License",
                    subtitle = "Free and open-source software license",
                    icon = Icons.Rounded.Gavel,
                    iconBgColor = Color(0xFF5856D6),
                    onClick = { showLicense = !showLicense }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

