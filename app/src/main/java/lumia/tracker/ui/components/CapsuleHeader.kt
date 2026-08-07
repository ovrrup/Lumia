package lumia.tracker.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Shape
import androidx.navigation.NavController
import lumia.tracker.ui.components.header.HeaderPushPullMode
import lumia.tracker.ui.components.header.glassHeaderCapsule
import lumia.tracker.viewmodel.ScholarViewModel

typealias HeaderPushPullMode = HeaderPushPullMode

@Composable
fun Modifier.glassHeaderCapsule(
    useGlass: Boolean,
    shape: Shape = androidx.compose.foundation.shape.RoundedCornerShape(32)
): Modifier {
    val mod = this
    return mod.then(
        Modifier.composed {
            this.glassHeaderCapsule(useGlass = useGlass, shape = shape)
        }
    )
}

@Composable
fun InteractivePushPullHeader(
    title: String,
    viewModel: ScholarViewModel? = null,
    navController: NavController? = null,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null
) {
    lumia.tracker.ui.components.header.InteractivePushPullHeader(
        title = title,
        viewModel = viewModel,
        navController = navController,
        onBackClick = onBackClick,
        actions = actions
    )
}

@Composable
fun UniversalCapsuleHeader(
    title: String,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null
) {
    lumia.tracker.ui.components.header.InteractivePushPullHeader(
        title = title,
        onBackClick = onBackClick,
        actions = actions
    )
}
