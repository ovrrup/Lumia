package ovrrup.lumia.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import ovrrup.lumia.ui.components.*
import ovrrup.lumia.viewmodel.ScholarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: ScholarViewModel
) {
    var step by remember { mutableStateOf(0) }
    val themeColor by viewModel.themeColor.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Step Indicator dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .size(if (step == index) 16.dp else 8.dp, 8.dp)
                                .background(
                                    if (step == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                    RoundedCornerShape(4.dp)
                                )
                        )
                    }
                }

                when (step) {
                    0 -> { // Step 0: Warm Welcome
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(50.dp)
                            )
                        }
                        Spacer(Modifier.height(24.dp))
                        Text(
                            "Welcome to Lumia Academic",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Your offline-first sanctuary to manage syllabus topics, track course notes, logs sessions, and study with clean glass aesthetics.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(Modifier.height(48.dp))
                        BouncyButton(
                            onClick = { step = 1 },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Begin Configuration")
                        }
                    }
                    1 -> { // Step 1: Color Palette Customization
                        Icon(
                            Icons.Rounded.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Select Theme Color",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Configure your visual theme. This adapts throughout the entire layout system dynamically.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(24.dp))

                        val themeOptions = listOf("Ocean", "Emerald", "Rose", "Amber", "Amethyst")
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            themeOptions.forEach { option ->
                                val isSelected = themeColor == option
                                GlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.updateThemeColor(option) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(option, fontWeight = FontWeight.SemiBold)
                                        if (isSelected) {
                                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(32.dp))
                        BouncyButton(
                            onClick = { step = 2 },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Next Step")
                        }
                    }
                    2 -> { // Step 2: Confirmation & Launch
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.VerifiedUser,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        Spacer(Modifier.height(24.dp))
                        Text(
                            "All Set for Studies!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Lumia stands ready to monitor study streaks, catalog courses, store local notes secure, check diagnostics telemetry, and elevate offline workflow.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(Modifier.height(48.dp))
                        BouncyButton(
                            onClick = {
                                viewModel.completeOnboarding()
                                navController.navigate("dashboard") {
                                    popUpTo("onboarding") { inclusive = true }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Launch Study Cockpit")
                        }
                    }
                }
            }
        }
    }
}
