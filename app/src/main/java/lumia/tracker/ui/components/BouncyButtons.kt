package lumia.tracker.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import lumia.tracker.ui.theme.bouncyScale
import lumia.tracker.ui.theme.LocalMoreRounds
import lumia.tracker.ui.theme.LocalMoreRoundsMode
import lumia.tracker.ui.theme.liquidGlass
import lumia.tracker.ui.theme.mix
import androidx.compose.ui.graphics.Color

@Composable
fun BouncyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    contentPadding: androidx.compose.foundation.layout.PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit
) {
    val moreRounds = LocalMoreRounds.current
    val mode = LocalMoreRoundsMode.current
    val isGlass = moreRounds && mode == "Glass"
    
    val finalModifier = if (isGlass) {
        modifier
            .bouncyScale(interactionSource)
            .liquidGlass(shape = shape, tintAlpha = 0.25f)
    } else {
        modifier.bouncyScale(interactionSource)
    }

    val finalColors = if (isGlass) {
        ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary
        )
    } else colors

    Button(
        onClick = onClick,
        modifier = finalModifier,
        enabled = enabled,
        shape = shape,
        colors = finalColors,
        elevation = if (isGlass) null else elevation,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content
    )
}

@Composable
fun BouncyIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    val moreRounds = LocalMoreRounds.current
    val mode = LocalMoreRoundsMode.current
    val isGlass = moreRounds && mode == "Glass"
    
    val finalModifier = if (isGlass) {
        modifier
            .bouncyScale(interactionSource)
            .liquidGlass(shape = CircleShape, tintAlpha = 0.2f)
    } else {
        modifier.bouncyScale(interactionSource)
    }

    IconButton(
        onClick = onClick,
        modifier = finalModifier,
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource,
        content = content
    )
}

@Composable
fun BouncyTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.textShape,
    colors: ButtonColors = ButtonDefaults.textButtonColors(),
    elevation: ButtonElevation? = null,
    contentPadding: androidx.compose.foundation.layout.PaddingValues = ButtonDefaults.TextButtonContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit
) {
    val moreRounds = LocalMoreRounds.current
    val mode = LocalMoreRoundsMode.current
    val isGlass = moreRounds && mode == "Glass"
    
    val finalModifier = if (isGlass) {
        modifier
            .bouncyScale(interactionSource)
            .liquidGlass(shape = shape, tintAlpha = 0.15f)
    } else {
        modifier.bouncyScale(interactionSource)
    }

    TextButton(
        onClick = onClick,
        modifier = finalModifier,
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content
    )
}

@Composable
fun BouncyOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.outlinedShape,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    elevation: ButtonElevation? = null,
    contentPadding: androidx.compose.foundation.layout.PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit
) {
    val moreRounds = LocalMoreRounds.current
    val mode = LocalMoreRoundsMode.current
    val isPastel = moreRounds && mode == "Pastel"
    val isGlass = moreRounds && mode == "Glass"
    
    val finalModifier = if (isGlass) {
        modifier
            .bouncyScale(interactionSource)
            .liquidGlass(shape = shape, tintAlpha = 0.15f)
    } else {
        modifier.bouncyScale(interactionSource)
    }

    val finalColors = if (isPastel) {
        ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            contentColor = MaterialTheme.colorScheme.primary
        )
    } else if (isGlass) {
        ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent)
    } else colors

    OutlinedButton(
        onClick = onClick,
        modifier = finalModifier,
        enabled = enabled,
        shape = shape,
        colors = finalColors,
        elevation = elevation,
        contentPadding = contentPadding,
        border = if (moreRounds) null else ButtonDefaults.outlinedButtonBorder(enabled),
        interactionSource = interactionSource,
        content = content
    )
}

@Composable
fun BouncyFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = FloatingActionButtonDefaults.shape,
    containerColor: androidx.compose.ui.graphics.Color = FloatingActionButtonDefaults.containerColor,
    contentColor: androidx.compose.ui.graphics.Color = contentColorFor(containerColor),
    elevation: FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    val moreRounds = LocalMoreRounds.current
    val mode = LocalMoreRoundsMode.current
    val isGlass = moreRounds && mode == "Glass"
    
    val finalModifier = if (isGlass) {
        modifier
            .bouncyScale(interactionSource)
            .liquidGlass(shape = shape, tintAlpha = 0.3f)
    } else {
        modifier.bouncyScale(interactionSource)
    }

    FloatingActionButton(
        onClick = onClick,
        modifier = finalModifier,
        shape = shape,
        containerColor = if (isGlass) Color.Transparent else containerColor,
        contentColor = contentColor,
        elevation = if (isGlass) FloatingActionButtonDefaults.bottomAppBarFabElevation(0.dp) else elevation,
        interactionSource = interactionSource,
        content = content
    )
}
