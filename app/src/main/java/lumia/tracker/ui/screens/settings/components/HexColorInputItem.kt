package lumia.tracker.ui.screens.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HexColorInputItem(label: String, value: String, onValueChange: (String) -> Unit) {
    var textValue by remember { mutableStateOf(value) }

    LaunchedEffect(value) {
        if (textValue != value && textValue != value.replace("#", "")) {
            textValue = value
        }
    }

    val colorPreview = remember(textValue) {
        try {
            val formatted = if (textValue.isNotEmpty() && !textValue.startsWith("#")) "#$textValue" else textValue
            Color(android.graphics.Color.parseColor(formatted))
        } catch (e: Exception) {
            Color.Transparent
        }
    }

    val isError = remember(textValue) {
        try {
            val formatted = if (textValue.isNotEmpty() && !textValue.startsWith("#")) "#$textValue" else textValue
            formatted.length != 7 && formatted.length != 9
        } catch (e: Exception) {
            true
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(colorPreview, shape = RoundedCornerShape(10.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = textValue,
            onValueChange = {
                textValue = it
                val formatted = if (it.isNotEmpty() && !it.startsWith("#")) "#$it" else it
                try {
                    if (formatted.length == 7 || formatted.length == 9) {
                        android.graphics.Color.parseColor(formatted)
                        onValueChange(formatted)
                    }
                } catch (e: Exception) {
                    // Safe ignore during typing
                }
            },
            singleLine = true,
            isError = isError,
            shape = RoundedCornerShape(10.dp),
            textStyle = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.width(115.dp)
        )
    }
}

