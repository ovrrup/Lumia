package lumia.tracker.ui.components.header

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import lumia.tracker.ui.theme.LocalDarkTheme
import lumia.tracker.ui.theme.LocalGlassDynamic
import lumia.tracker.ui.theme.LocalGlassTint
import lumia.tracker.ui.theme.LocalPureBlackMode
import lumia.tracker.ui.theme.liquidGlass
import lumia.tracker.ui.theme.mix

@Composable
fun Modifier.glassHeaderCapsule(
    useGlass: Boolean,
    shape: Shape = RoundedCornerShape(32.dp)
): Modifier = composed {
    this
        .shadow(
            elevation = 8.dp,
            shape = shape,
            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        )
        .background(MaterialTheme.colorScheme.surface, shape)
        .border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            shape = shape
        )
}
