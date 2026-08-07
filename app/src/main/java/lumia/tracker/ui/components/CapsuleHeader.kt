package lumia.tracker.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import lumia.tracker.viewmodel.ScholarViewModel

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
