package lumia.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import lumia.tracker.ui.theme.LocalGlassMode
import lumia.tracker.ui.theme.LocalMoreRounds
import lumia.tracker.ui.theme.LocalMoreRoundsMode
import lumia.tracker.ui.theme.glassPill

@Composable
fun UniversalCapsuleHeader(
    title: String,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null
) {
    val isGlass = LocalGlassMode.current
    val moreRounds = LocalMoreRounds.current
    val moreRoundsMode = LocalMoreRoundsMode.current
    val isMrGlass = moreRounds && moreRoundsMode == "Glass"
    val useGlassHeader = isGlass || isMrGlass

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(top = 16.dp, end = 16.dp, start = 16.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        Row(
            modifier = Modifier
                .then(
                    if (useGlassHeader) {
                        Modifier.glassPill(shape = RoundedCornerShape(32.dp))
                    } else {
                        Modifier
                            .shadow(
                                elevation = 12.dp,
                                shape = RoundedCornerShape(32.dp),
                                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            )
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(32.dp))
                    }
                )
                .border(
                    width = 1.dp,
                    color = (if (useGlassHeader) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface).copy(alpha = 0.08f),
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(start = if (onBackClick != null) 4.dp else 16.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (onBackClick != null) {
                BouncyIconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = if (useGlassHeader) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // Vertical separator
                Box(
                    modifier = Modifier
                        .height(18.dp)
                        .width(1.dp)
                        .background(
                            color = (if (useGlassHeader) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface).copy(alpha = 0.15f)
                        )
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = if (useGlassHeader) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            if (actions != null) {
                // Vertical separator
                Box(
                    modifier = Modifier
                        .height(18.dp)
                        .width(1.dp)
                        .background(
                            color = (if (useGlassHeader) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface).copy(alpha = 0.15f)
                        )
                )
                actions()
            }
        }
    }
}
