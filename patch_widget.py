import re

with open("app/src/main/java/lumia/tracker/ui/components/StreakWidget.kt", "r") as f:
    content = f.read()

# I want to add `val isCompleteToday by viewModel.streakIsCompleteToday.collectAsStateWithLifecycle()`
# And update the drawing logic

old_code = """    val animProgress by animateFloatAsState(
        targetValue = streakPercentage,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "streak_progress"
    )

    // Wave animation for liquid effect
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
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
    }"""

new_code = """    val isCompleteToday by viewModel.streakIsCompleteToday.collectAsStateWithLifecycle()

    val animProgress by animateFloatAsState(
        targetValue = streakPercentage,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "streak_progress"
    )

    // Wave animation for liquid effect
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_offset"
    )
    
    val completeScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isCompleteToday) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "complete_pulse"
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
                if (applyGlass) Modifier.liquidGlass(CircleShape, tintAlpha = if(isCompleteToday) 0.3f else 0.15f)
                else Modifier.background(if(isCompleteToday) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = streakCurrent.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = if (isCompleteToday) color else color.copy(alpha = 0.9f)
        )
        Icon(
            imageVector = Icons.Rounded.LocalFireDepartment,
            contentDescription = "Streak",
            tint = Color.Black, // Black will be overwritten
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer { 
                    compositingStrategy = CompositingStrategy.Offscreen
                    scaleX = completeScale
                    scaleY = completeScale
                }
                .drawWithContent {
                    drawContent()
                    
                    val progressHeight = 1f - animProgress
                    
                    // Step 1: Fill entire icon with color
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(color.copy(alpha = 0.6f), color)
                        ),
                        blendMode = BlendMode.SrcIn
                    )
                    
                    if (isCompleteToday) {
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White.copy(alpha = 0.5f), Color.Transparent)
                            ),
                            blendMode = BlendMode.ColorDodge
                        )
                    } else if (applyGlass) {
                        drawRect(
                            color = Color.White.copy(alpha = 0.3f),
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
                            
                            var x = 0f
                            val step = size.width / 40f
                            while (x <= size.width) {
                                // Fix looping wave offset issue
                                val y = yOffset + kotlin.math.sin((x / size.width) * Math.PI * 2 + (waveOffset * Math.PI * 2)).toFloat() * waveHeight
                                path.lineTo(x, y)
                                x += step
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
                                color = Color.Gray.copy(alpha = 0.35f),
                                blendMode = BlendMode.SrcIn
                            )
                        }
                    }
                }
        )
    }"""

if old_code in content:
    content = content.replace(old_code, new_code)
    with open("app/src/main/java/lumia/tracker/ui/components/StreakWidget.kt", "w") as f:
        f.write(content)
    print("Replaced!")
else:
    print("Not found.")
