import re

with open("app/src/main/java/lumia/tracker/ui/screens/settings/StreakSettingsScreen.kt", "r") as f:
    content = f.read()

pattern = re.compile(r"val isGlass = LocalGlassMode\.current\s+Scaffold\([^)]+\)\s+\{\s*paddingValues ->", re.MULTILINE | re.DOTALL)

new_scaffold = """val betaEnhancedHeader by viewModel.betaEnhancedHeader.collectAsStateWithLifecycle()
    val isGlass = LocalGlassMode.current
    
    Scaffold(
        containerColor = if (isGlass) Color.Transparent else MaterialTheme.colorScheme.background,
        topBar = {
            androidx.compose.foundation.layout.Box {
                if (betaEnhancedHeader || isGlass) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .matchParentSize()
                            .lumia.tracker.ui.theme.glassBar(shape = RoundedCornerShape(0.dp))
                    )
                    HorizontalDivider(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                }
                @OptIn(ExperimentalMaterial3Api::class)
                CenterAlignedTopAppBar(
                    title = { Text("Streak Settings", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = if (betaEnhancedHeader || isGlass) Color.Transparent else MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = if (betaEnhancedHeader || isGlass) Color.Transparent else MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                    )
                )
            }
        }
    ) { paddingValues ->"""

content, count = pattern.subn(new_scaffold, content)

with open("app/src/main/java/lumia/tracker/ui/screens/settings/StreakSettingsScreen.kt", "w") as f:
    f.write(content)
print(f"Replaced topbar in StreakSettingsScreen! {count} replacements.")

