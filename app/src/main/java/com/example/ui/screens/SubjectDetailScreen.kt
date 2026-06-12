package com.example.ui.screens

import com.example.ui.theme.liquidGlass
import android.app.DatePickerDialog
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.LibraryAddCheck
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.viewmodel.ScholarViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.graphics.graphicsLayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(navController: NavController, viewModel: ScholarViewModel, subjectId: Int) {
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val subject = subjects.find { it.id == subjectId }
    val topics by viewModel.getTopicsForSubject(subjectId).collectAsStateWithLifecycle()
    var showAddTopic by remember { mutableStateOf(false) }
    
    val context = LocalContext.current

    if (subject == null) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val isGlass = com.example.ui.theme.LocalGlassMode.current
    val betaEnhancedHeader by viewModel.betaEnhancedHeader.collectAsStateWithLifecycle()
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    Scaffold(
        containerColor = if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.background,
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            androidx.compose.foundation.layout.Box {
                if (betaEnhancedHeader) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .matchParentSize()
                            .liquidGlass(
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp),
                                tintAlpha = if (isDark) 0.18f else 0.45f,
                                blurRadius = 15f,
                                isDark = isDark,
                                tintColor = MaterialTheme.colorScheme.surface
                            )
                    )
                    // Sleek divider line for clean separation and anchoring
                    androidx.compose.material3.HorizontalDivider(
                        modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                    )
                }
                LargeTopAppBar(
                    title = { Text(subject.name, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = if (betaEnhancedHeader) androidx.compose.ui.graphics.Color.Transparent else if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = if (betaEnhancedHeader) androidx.compose.ui.graphics.Color.Transparent else if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                    )
                )
            }
        },
        floatingActionButton = {
            androidx.compose.material3.FloatingActionButton(
                onClick = { showAddTopic = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                val rotation by androidx.compose.animation.core.animateFloatAsState(targetValue = if (showAddTopic) 45f else 0f)
                Icon(Icons.Rounded.Add, contentDescription = "Add Topic", modifier = Modifier.graphicsLayer { rotationZ = rotation })
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            androidx.compose.animation.AnimatedVisibility(
                visible = topics.isEmpty(),
                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(),
                exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut()
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LibraryAddCheck,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("No topics yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            androidx.compose.animation.AnimatedVisibility(
                visible = topics.isNotEmpty(),
                enter = androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.fadeOut()
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(topics, key = { "t_${it.id}" }) { topic ->
                        val cardColor by androidx.compose.animation.animateColorAsState(
                            if (topic.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                        )
                        com.example.ui.components.GlassCard(
                            modifier = Modifier.animateItem().fillMaxWidth().animateContentSize(),
                            shape = RoundedCornerShape(24.dp),
                            containerColor = cardColor
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                                Checkbox(
                                    checked = topic.isCompleted,
                                    onCheckedChange = { viewModel.toggleTopicCompleted(topic) }
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    topic.title,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (topic.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                                    textDecoration = if (topic.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                )
                                IconButton(onClick = { viewModel.deleteTopic(topic) }) {
                                    Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddTopic) {
        var title by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddTopic = false },
            title = { Text("Add Topic") },
            text = {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Topic Title") })
            },
            confirmButton = {
                TextButton(onClick = { if (title.isNotBlank()) { viewModel.addTopic(subjectId, title); showAddTopic = false } }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAddTopic = false }) { Text("Cancel") } }
        )
    }
}
