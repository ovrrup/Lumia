package lumia.tracker.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import lumia.tracker.viewmodel.ScholarViewModel

class SystemBarScrollConnection(
    private val viewModel: ScholarViewModel,
    private val threshold: Float = 10f
) : NestedScrollConnection {
    private var accumulatedScroll = 0f

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        val delta = available.y
        if (viewModel.displayLayoutMode.value == "Immersive") {
            accumulatedScroll += delta
            if (accumulatedScroll > threshold) {
                // Scrolled up or pulled down -> show status bar
                viewModel.setSystemBarVisible(true)
                accumulatedScroll = 0f
            } else if (accumulatedScroll < -threshold) {
                // Scrolled down -> hide status bar
                viewModel.setSystemBarVisible(false)
                accumulatedScroll = 0f
            }
        }
        return Offset.Zero
    }
}
