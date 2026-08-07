package lumia.tracker.ui.components.header

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.ui.theme.LocalGlassMode
import lumia.tracker.ui.theme.bouncyClick
import lumia.tracker.viewmodel.ScholarViewModel

@Composable
fun InteractivePushPullHeader(
    title: String,
    viewModel: ScholarViewModel? = null,
    navController: NavController? = null,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null
) {
    var headerMode by remember { mutableStateOf(HeaderPushPullMode.Standard) }
    val isGlass = LocalGlassMode.current
    val pureBlackMode = viewModel?.pureBlackMode?.collectAsStateWithLifecycle()?.value ?: false
    val actualUseGlass = if (pureBlackMode) false else isGlass

    val cornerRadius = when (headerMode) {
        HeaderPushPullMode.Expanded -> 28.dp
        HeaderPushPullMode.Standard -> 32.dp
        HeaderPushPullMode.Compact -> 24.dp
    }

    var dragAccumulated by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .animateContentSize(
                animationSpec = spring(
                    stiffness = Spring.StiffnessMediumLow,
                    dampingRatio = Spring.DampingRatioLowBouncy
                )
            )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .glassHeaderCapsule(useGlass = actualUseGlass, shape = RoundedCornerShape(cornerRadius)),
            shape = RoundedCornerShape(cornerRadius),
            color = Color.Transparent
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                // Dedicated Drag Handle Bar with vertical gesture detector
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .pointerInput(headerMode) {
                            detectVerticalDragGestures(
                                onDragEnd = { dragAccumulated = 0f },
                                onDragCancel = { dragAccumulated = 0f },
                                onVerticalDrag = { change, dragAmount ->
                                    change.consume()
                                    dragAccumulated += dragAmount
                                    if (dragAccumulated > 35f) {
                                        headerMode = when (headerMode) {
                                            HeaderPushPullMode.Compact -> HeaderPushPullMode.Standard
                                            HeaderPushPullMode.Standard -> HeaderPushPullMode.Expanded
                                            HeaderPushPullMode.Expanded -> HeaderPushPullMode.Expanded
                                        }
                                        dragAccumulated = 0f
                                    } else if (dragAccumulated < -35f) {
                                        headerMode = when (headerMode) {
                                            HeaderPushPullMode.Expanded -> HeaderPushPullMode.Standard
                                            HeaderPushPullMode.Standard -> HeaderPushPullMode.Compact
                                            HeaderPushPullMode.Compact -> HeaderPushPullMode.Compact
                                        }
                                        dragAccumulated = 0f
                                    }
                                }
                            )
                        }
                        .bouncyClick {
                            headerMode = when (headerMode) {
                                HeaderPushPullMode.Compact -> HeaderPushPullMode.Standard
                                HeaderPushPullMode.Standard -> HeaderPushPullMode.Expanded
                                HeaderPushPullMode.Expanded -> HeaderPushPullMode.Standard
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(5.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                CircleShape
                            )
                    )
                }

                when (headerMode) {
                    HeaderPushPullMode.Compact -> {
                        HeaderCompactRow(
                            title = title,
                            onExpand = { headerMode = HeaderPushPullMode.Standard }
                        )
                    }

                    HeaderPushPullMode.Standard -> {
                        HeaderStandardRow(
                            title = title,
                            actualUseGlass = actualUseGlass,
                            viewModel = viewModel,
                            navController = navController,
                            onBackClick = onBackClick,
                            onExpand = { headerMode = HeaderPushPullMode.Expanded },
                            actions = actions
                        )
                    }

                    HeaderPushPullMode.Expanded -> {
                        HeaderExpandedQuickHub(
                            title = title,
                            viewModel = viewModel,
                            navController = navController,
                            onCollapse = { headerMode = HeaderPushPullMode.Standard }
                        )
                    }
                }
            }
        }
    }
}
