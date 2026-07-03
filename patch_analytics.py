import re

with open("app/src/main/java/lumia/tracker/ui/screens/study/AnalyticsTab.kt", "r") as f:
    content = f.read()

new_code = """
                val streakTotalNormal by viewModel.streakTotalNormal.collectAsStateWithLifecycle()
                val streakTotalComplete by viewModel.streakTotalComplete.collectAsStateWithLifecycle()
                val streakLongest by viewModel.streakLongest.collectAsStateWithLifecycle()
                val streakCurrent by viewModel.streakCurrent.collectAsStateWithLifecycle()

                // Streaks Analytics Card
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Streaks Analytics",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                lumia.tracker.ui.components.GlassCard(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    shape = RoundedCornerShape(32.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Current", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(streakCurrent.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Longest", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(streakLongest.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Normal", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(streakTotalNormal.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Complete", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(streakTotalComplete.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFFE67E22))
                            }
                        }
                    }
                }
"""

idx = content.find("val allProfiles by viewModel.allProfiles.collectAsStateWithLifecycle()")
if idx != -1:
    content = content[:idx] + new_code + "\n                " + content[idx:]
    with open("app/src/main/java/lumia/tracker/ui/screens/study/AnalyticsTab.kt", "w") as f:
        f.write(content)
    print("Replaced!")
else:
    print("Not found.")
