package lumia.tracker.ui.screens.auth.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import lumia.tracker.viewmodel.ScholarViewModel

@Composable
fun ProfileSetupPage(
    isActive: Boolean = true,
    viewModel: ScholarViewModel,
    onSaved: (name: String, alias: String, theme: String, avatar: String) -> Unit = { _, _, _, _ -> }
) {
    var name by remember { mutableStateOf("") }
    var alias by remember { mutableStateOf("") }
    var selectedTheme by remember { mutableStateOf("Ocean") }

    val scale by animateFloatAsState(if (isActive) 1f else 0.85f, tween(600), label = "page_scale")
    val alpha by animateFloatAsState(if (isActive) 1f else 0f, tween(600), label = "page_alpha")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .scale(scale)
            .alpha(alpha)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Person,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Create Your Profile",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Personalize your workspace name and appearance theme.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                onSaved(name, alias, selectedTheme, "")
            },
            label = { Text("Your Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = alias,
            onValueChange = {
                alias = it
                onSaved(name, alias, selectedTheme, "")
            },
            label = { Text("Title / Alias (e.g., Scholar)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
