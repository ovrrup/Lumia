package lumia.tracker.ui.screens

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

@Composable
fun HexColorInputItem(label: String, value: String, onValueChange: (String) -> Unit) {
    var textValue by remember { mutableStateOf(value) }
    
    // Sync external changes properly
    LaunchedEffect(value) {
        if (textValue != value && textValue != value.replace("#","")) {
            textValue = value
        }
    }
    
    val colorPreview = remember(textValue) {
        try {
            val formatted = if (textValue.isNotEmpty() && !textValue.startsWith("#")) "#$textValue" else textValue
            androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(formatted))
        } catch(e: Exception) {
            androidx.compose.ui.graphics.Color.Transparent
        }
    }
    
    val isError = remember(textValue) {
        try {
            val formatted = if (textValue.isNotEmpty() && !textValue.startsWith("#")) "#$textValue" else textValue
            if (formatted.length == 7 || formatted.length == 9) {
                false
            } else {
                true
            }
        } catch(e: Exception) {
            true
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
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
                    } catch(e: Exception) {
                        // Safe ignore during half-typed strings
                    }
                },
                singleLine = true,
                isError = isError,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, end = 16.dp)
            )
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(colorPreview, shape = RoundedCornerShape(8.dp))
                .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
        )
    }
}
