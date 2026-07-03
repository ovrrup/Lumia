import re

with open("app/src/main/java/lumia/tracker/ui/components/StreakWidget.kt", "r") as f:
    content = f.read()

# I need to replace from Row( or infiniteTransition down to the end of the file.
idx = content.find("    val infiniteTransition = rememberInfiniteTransition")

if idx != -1:
    top_part = content[:idx]
    
    new_bottom = """    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_offset"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .padding(end = 12.dp)
            .height(40.dp)
            .clip(CircleShape)
            .bouncyClick(onClick = { navController.navigate("settings/streaks") })
            .then(
                if (applyGlass) Modifier.liquidGlass(CircleShape, tintAlpha = 0.15f)
                else Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = streakCurrent.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Icon(
            imageVector = Icons.Rounded.LocalFireDepartment,
            contentDescription = "Streak",
            tint = Color.Black, // Black will be overwritten
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    
                    val progressHeight = 1f - animProgress
                    
                    // Step 1: Fill entire icon with color
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(color.copy(alpha = 0.8f), color)
                        ),
                        blendMode = BlendMode.SrcIn
                    )
                    if (applyGlass) {
                        drawRect(
                            color = Color.White.copy(alpha = 0.4f),
                            blendMode = BlendMode.ColorDodge
                        )
                    }

                    // Step 2: Make the empty part gray
                    if (animProgress < 1f) {
                        val path = androidx.compose.ui.graphics.Path()
                        path.moveTo(0f, 0f)
                        
                        val yOffset = size.height * progressHeight
                        if ((applyGlass || applyBouncy) && animProgress > 0f) {
                            val waveHeight = size.height * 0.08f
                            path.lineTo(0f, yOffset)
                            for (x in 0..size.width.toInt() step 2) {
                                val y = yOffset + kotlin.math.sin((x.toFloat() / size.width) * Math.PI * 2 + waveOffset).toFloat() * waveHeight
                                path.lineTo(x.toFloat(), y)
                            }
                            path.lineTo(size.width, 0f)
                        } else {
                            path.lineTo(0f, yOffset)
                            path.lineTo(size.width, yOffset)
                            path.lineTo(size.width, 0f)
                        }
                        path.close()
                        
                        clipPath(path) {
                            drawRect(
                                color = Color.Gray.copy(alpha = 0.3f),
                                blendMode = BlendMode.SrcIn
                            )
                        }
                    }
                }
        )
    }
}
"""
    with open("app/src/main/java/lumia/tracker/ui/components/StreakWidget.kt", "w") as f:
        f.write(top_part + new_bottom)
