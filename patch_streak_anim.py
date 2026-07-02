import re

with open("app/src/main/java/lumia/tracker/ui/components/StreakWidget.kt", "r") as f:
    content = f.read()

content = content.replace('''val animationMode = LocalAppAnimationMode.current
    val isGlass = LocalGlassMode.current''',
'''val animationMode = LocalAppAnimationMode.current
    val isGlass = LocalGlassMode.current
    val animOverride by viewModel.streakAnimationOverride.collectAsStateWithLifecycle()
    
    val applyGlass = animOverride == "Glass Liquid" || (animOverride == "Default" && isGlass)
    val applyBouncy = animOverride == "Bouncy" || (animOverride == "Default" && animationMode == "Bouncy")''')

content = content.replace('''if (isGlass || animationMode == "Bouncy") {''', '''if (applyGlass || applyBouncy) {''')
content = content.replace('''if (isGlass) {
                                    drawRect(
                                        color = Color.White.copy(alpha = 0.4f),
                                        blendMode = BlendMode.ColorDodge
                                    )
                                }''', '''if (applyGlass) {
                                    drawRect(
                                        color = Color.White.copy(alpha = 0.4f),
                                        blendMode = BlendMode.ColorDodge
                                    )
                                }''')
content = content.replace('''if (isGlass) Modifier.liquidGlass(CircleShape, tintAlpha = 0.15f)''', '''if (applyGlass) Modifier.liquidGlass(CircleShape, tintAlpha = 0.15f)''')
content = content.replace('''val animOverride by viewModel.streakAnimationOverride.collectAsStateWithLifecycle()''', '') # remove duplicate since it's already there

with open("app/src/main/java/lumia/tracker/ui/components/StreakWidget.kt", "w") as f:
    f.write(content)
