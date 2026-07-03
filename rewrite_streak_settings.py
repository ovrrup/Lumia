import re

with open("app/src/main/java/lumia/tracker/ui/screens/settings/StreakSettingsScreen.kt", "r") as f:
    content = f.read()

idx = content.find("        LazyColumn(")
if idx != -1:
    new_code = """        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SettingsGroupCard(title = "Streak Goals", icon = Icons.Rounded.List) {
                    Text(
                        "Set minimum daily requirements for a complete streak. Note: If you plan more than these limits, you'll need to complete all planned items to maintain your streak.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Tasks Requirement
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Tasks", modifier = Modifier.weight(1f))
                        Slider(
                            value = reqTasks.toFloat(),
                            onValueChange = { viewModel.updateStreakReqTasks(it.roundToInt()) },
                            valueRange = 0f..20f,
                            steps = 19,
                            modifier = Modifier.weight(2f)
                        )
                        Text("$reqTasks", modifier = Modifier.width(30.dp), textAlign = TextAlign.End)
                    }
                    // Assignments Requirement
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Assignments", modifier = Modifier.weight(1f))
                        Slider(
                            value = reqAssignments.toFloat(),
                            onValueChange = { viewModel.updateStreakReqAssignments(it.roundToInt()) },
                            valueRange = 0f..10f,
                            steps = 9,
                            modifier = Modifier.weight(2f)
                        )
                        Text("$reqAssignments", modifier = Modifier.width(30.dp), textAlign = TextAlign.End)
                    }
                    // Study Mins Requirement
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Study (Mins)", modifier = Modifier.weight(1f))
                        Slider(
                            value = reqStudyMins.toFloat(),
                            onValueChange = { viewModel.updateStreakReqStudyMins(it.roundToInt()) },
                            valueRange = 0f..120f,
                            steps = 23,
                            modifier = Modifier.weight(2f)
                        )
                        Text("$reqStudyMins", modifier = Modifier.width(30.dp), textAlign = TextAlign.End)
                    }
                }
            }
            
            item {
                SettingsGroupCard(title = "Streak Threshold", icon = Icons.Rounded.Speed) {
                    Text(
                        "Set the minimum completion percentage needed for the day to count as a normal streak.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Threshold", modifier = Modifier.weight(1f))
                        Slider(
                            value = partialThreshold,
                            onValueChange = { viewModel.updateStreakPartialThreshold(it) },
                            valueRange = 0.1f..1.0f,
                            steps = 8,
                            modifier = Modifier.weight(2f)
                        )
                        Text("${(partialThreshold * 100).roundToInt()}%", modifier = Modifier.width(40.dp), textAlign = TextAlign.End)
                    }
                }
            }
            
            item {
                SettingsGroupCard(title = "Visuals & Animation", icon = Icons.Rounded.Palette) {
                    Text("Streak Fire Color", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    val colors = listOf("Theme", "#FF5722", "#FF9800", "#4CAF50", "#2196F3", "#9C27B0", "#E91E63", "#F44336")
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        colors.forEach { hex ->
                            val isSelected = hex == colorHex
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (hex == "Theme") MaterialTheme.colorScheme.primary else Color(android.graphics.Color.parseColor(hex)))
                                    .clickable { viewModel.updateStreakProgressColor(hex) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Fire Brightness", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Slider(
                            value = brightness,
                            onValueChange = { viewModel.updateStreakBrightness(it) },
                            valueRange = 0.5f..2.0f,
                            modifier = Modifier.weight(1f)
                        )
                        Text(String.format("%.1f", brightness), modifier = Modifier.width(40.dp), textAlign = TextAlign.End)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Animation Style (Override)", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    val styles = listOf("Default", "Material", "Bouncy", "Glass Liquid")
                    styles.forEach { style ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.updateStreakAnimationOverride(style) }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = animOverride == style,
                                onClick = { viewModel.updateStreakAnimationOverride(style) }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(style, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
"""
    content = content[:idx] + new_code

    if "import androidx.compose.material.icons.rounded.List" not in content:
        content = content.replace("import androidx.compose.material.icons.rounded.Check", "import androidx.compose.material.icons.rounded.Check\nimport androidx.compose.material.icons.rounded.List\nimport androidx.compose.material.icons.rounded.Speed\nimport androidx.compose.material.icons.rounded.Palette")

    if "import androidx.compose.ui.text.style.TextAlign" not in content:
        content = "import androidx.compose.ui.text.style.TextAlign\n" + content
    if "import androidx.compose.foundation.shape.RoundedCornerShape" not in content:
        content = "import androidx.compose.foundation.shape.RoundedCornerShape\n" + content

    with open("app/src/main/java/lumia/tracker/ui/screens/settings/StreakSettingsScreen.kt", "w") as f:
        f.write(content)
    print("Replaced!")
else:
    print("Not found")

