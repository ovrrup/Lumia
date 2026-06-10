package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.viewmodel.ScholarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolboxScreen(navController: NavController, viewModel: ScholarViewModel) {
    val betaPomodoro by viewModel.betaPomodoro.collectAsStateWithLifecycle()
    val betaCgpa by viewModel.betaCgpa.collectAsStateWithLifecycle()
    val betaNotes by viewModel.betaNotes.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Toolbox", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        if (!betaPomodoro && !betaCgpa && !betaNotes) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(
                    "No tools enabled. Enable them in Settings.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize().padding(paddingValues)
            ) {
                if (betaPomodoro) {
                    item(key = "pomodoro") {
                        ToolboxItem(
                            icon = Icons.Default.AccessTime,
                            title = "Pomodoro Timer",
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            onColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            onClick = { navController.navigate("pomodoro") },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
                if (betaCgpa) {
                    item(key = "cgpa") {
                        ToolboxItem(
                            icon = Icons.Default.Calculate,
                            title = "CGPA Calc",
                            color = MaterialTheme.colorScheme.primaryContainer,
                            onColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            onClick = { navController.navigate("cgpa") },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
                if (betaNotes) {
                    item(key = "notes") {
                        ToolboxItem(
                            icon = Icons.AutoMirrored.Filled.Notes,
                            title = "Quick Notes",
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            onColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            onClick = { navController.navigate("notes") },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolboxItem(
    icon: ImageVector,
    title: String,
    color: Color,
    onColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = onColor,
                modifier = Modifier
                    .size(48.dp)
                    .background(onColor.copy(alpha = 0.1f), CircleShape)
                    .padding(10.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = onColor,
                maxLines = 1
            )
        }
    }
}
