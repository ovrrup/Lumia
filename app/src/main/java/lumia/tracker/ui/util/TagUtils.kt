package lumia.tracker.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun getTagColors(tagName: String): Pair<Color, Color> {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val index = Math.abs(tagName.hashCode()) % 10
    return if (isDark) {
        val darkPairs = listOf(
            Pair(Color(0xFF1A237E).copy(alpha = 0.45f), Color(0xFF8C9EFF)), 
            Pair(Color(0xFF1B5E20).copy(alpha = 0.45f), Color(0xFFB9F6CA)), 
            Pair(Color(0xFFB71C1C).copy(alpha = 0.45f), Color(0xFFFF8A80)), 
            Pair(Color(0xFFE65100).copy(alpha = 0.45f), Color(0xFFFFD180)), 
            Pair(Color(0xFF4A148C).copy(alpha = 0.45f), Color(0xFFEA80FC)), 
            Pair(Color(0xFF004D40).copy(alpha = 0.45f), Color(0xFFA7FFEB)), 
            Pair(Color(0xFF0D47A1).copy(alpha = 0.45f), Color(0xFF80D8FF)), 
            Pair(Color(0xFF3E2723).copy(alpha = 0.45f), Color(0xFFFFD180)), 
            Pair(Color(0xFF263238).copy(alpha = 0.45f), Color(0xFFECEFF1)), 
            Pair(Color(0xFF827717).copy(alpha = 0.45f), Color(0xFFE6EE9C))  
        )
        darkPairs[index]
    } else {
        val lightPairs = listOf(
            Pair(Color(0xFFE8EAF6), Color(0xFF1A237E)), 
            Pair(Color(0xFFE8F5E9), Color(0xFF1B5E20)), 
            Pair(Color(0xFFFFEBEE), Color(0xFFC62828)), 
            Pair(Color(0xFFFFF3E0), Color(0xFFE65100)), 
            Pair(Color(0xFFF3E5F5), Color(0xFF4A148C)), 
            Pair(Color(0xFFE0F2F1), Color(0xFF004D40)), 
            Pair(Color(0xFFE3F2FD), Color(0xFF0D47A1)), 
            Pair(Color(0xFFEFEBE9), Color(0xFF4E342E)), 
            Pair(Color(0xFFECEFF1), Color(0xFF37474F)), 
            Pair(Color(0xFFF9FBE7), Color(0xFF827717))  
        )
        lightPairs[index]
    }
}
