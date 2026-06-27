package com.lumia.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            DashboardScreen()
        }
    }
}

@Composable
fun DashboardScreen() {
    var automationEnabled by remember { mutableStateOf(true) }
    
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Lumia Offline Tracker",
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Smart Automation",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = automationEnabled,
                        onCheckedChange = { automationEnabled = it }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (automationEnabled) {
                    Text(
                        text = "Automation is active. Schedule will be continuously optimized based on your local metrics and ML predictions.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
