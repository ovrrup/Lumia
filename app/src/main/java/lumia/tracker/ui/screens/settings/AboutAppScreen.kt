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
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import lumia.tracker.ui.components.GlassCapsule
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.components.ScholarCardDefaults
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
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

            // 1. Hero Brand Card with Live Blur Glow, Capsule Pills, and Circular Action Buttons
            ScholarCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                glassmorphic = true
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // App Logo Container
                    Surface(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(68.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                            border = ScholarCardDefaults.glassBorder(accentColor = MaterialTheme.colorScheme.primary),
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

                    // App Title & Tagline
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Lumia Tracker",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Academic companion for students & researchers",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Capsule Version Pills
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
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                )
                                Text(
                                    text = "v$currentVersion",
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
                                text = "FOSS Edition",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Circular Action Buttons for GitHub and Licenses
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // GitHub Circular Action Button
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                                border = ScholarCardDefaults.glassBorder(),
                                modifier = Modifier.size(50.dp)
                            ) {
                                BouncyIconButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ovrrup/Lumia"))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Code,
                                        contentDescription = "GitHub Repository",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Text(
                                text = "GitHub",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // License Circular Action Button
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                                border = ScholarCardDefaults.glassBorder(),
                                modifier = Modifier.size(50.dp)
                            ) {
                                BouncyIconButton(
                                    onClick = { showLicense = true },
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Gavel,
                                        contentDescription = "Licenses",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Text(
                                text = "License",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 2. Software Updates Card
            ScholarCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                glassmorphic = true
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                                contentAlignment = Alignment.Center
                            ) {
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
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = when (updateState) {
                                        "checking" -> "Checking GitHub..."
                                        "available" -> "Update: $updateTagName"
                                        "latest" -> "Up to date"
                                        "error" -> "Check unavailable"
                                        else -> "Software Updates"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
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
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("Check", style = MaterialTheme.typography.labelMedium)
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

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showLicense) {
        AlertDialog(
            onDismissRequest = { showLicense = false },
            shape = RoundedCornerShape(24.dp),
            icon = {
                Icon(
                    Icons.Rounded.Gavel,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    "MIT License",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Copyright (c) 2026 Lumia Contributors\n\n" +
                               "Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files, to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software.\n\n" +
                               "THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                BouncyButton(
                    onClick = { showLicense = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

