package com.example.ui.theme

import android.os.Build
import android.graphics.RenderEffect
import android.graphics.Shader
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Applies the full "thick liquid glass" treatment to any composable.
 *
 * @param shape         The shape of the glass surface (default: 32dp rounded)
 * @param tintColor     Base color to tint the glass body (usually theme primary or surface)
 * @param tintAlpha     Opacity of the glass body fill (0.15–0.30 recommended)
 * @param blurRadius    Backdrop blur radius in pixels (only works on API 31+)
 * @param glossHeight   Height in dp of the top specular highlight strip
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(32.dp),
    tintColor: Color = Color.White,
    tintAlpha: Float = 0.18f,
    blurRadius: Float = 22f,
    glossHeight: Dp = 12.dp
): Modifier = this
    .clip(shape)
    // 1. Backdrop blur — API 31+ only
    .then(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Modifier.graphicsLayer {
                renderEffect = RenderEffect
                    .createBlurEffect(blurRadius, blurRadius, Shader.TileMode.CLAMP)
                    .asComposeRenderEffect()
            }
        } else Modifier
    )
    // 2. Glass body fill (refraction illusion)
    .background(
        brush = Brush.verticalGradient(
            colors = listOf(
                tintColor.copy(alpha = tintAlpha + 0.08f),  // slightly denser at top
                tintColor.copy(alpha = tintAlpha),
                tintColor.copy(alpha = tintAlpha - 0.04f)   // slightly lighter at bottom
            )
        ),
        shape = shape
    )
    // 3. Glass border (edge definition)
    .border(
        width = 1.dp,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.65f),
                Color.White.copy(alpha = 0.08f)
            )
        ),
        shape = shape
    )
    // 4. Top specular highlight + bottom glow drawn on top
    .drawWithContent {
        drawContent()
        // Top gloss streak
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.55f),
                    Color.Transparent
                ),
                endY = glossHeight.toPx()
            )
        )
        // Bottom edge bounce light
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.18f)
                ),
                startY = size.height - glossHeight.toPx()
            )
        )
    }

/** Glass card variant — primary tint, standard depth */
fun Modifier.glassCard(shape: Shape = RoundedCornerShape(32.dp)): Modifier =
    liquidGlass(shape = shape, tintAlpha = 0.20f)

/** Elevated glass — thicker/denser glass for hero cards */
fun Modifier.glassHero(shape: Shape = RoundedCornerShape(32.dp)): Modifier =
    liquidGlass(shape = shape, tintAlpha = 0.28f, blurRadius = 30f, glossHeight = 16.dp)

/** Subtle glass — for nav bars and app bars */
fun Modifier.glassBar(shape: Shape = RoundedCornerShape(0.dp)): Modifier =
    liquidGlass(shape = shape, tintAlpha = 0.12f, blurRadius = 18f, glossHeight = 8.dp)

/** Glass pill — for chips, badges, small elements */
fun Modifier.glassPill(shape: Shape = RoundedCornerShape(50.dp)): Modifier =
    liquidGlass(shape = shape, tintAlpha = 0.22f, blurRadius = 16f, glossHeight = 8.dp)
