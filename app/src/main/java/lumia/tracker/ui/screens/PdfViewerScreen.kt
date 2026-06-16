package lumia.tracker.ui.screens

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(navController: NavController, filePath: String?, fileName: String?) {
    val context = LocalContext.current
    var currentPageIndex by remember { mutableStateOf(0) }
    var pageCount by remember { mutableStateOf(0) }
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(filePath, currentPageIndex) {
        if (filePath == null) {
            errorMessage = "No file path provided"
            return@LaunchedEffect
        }
        val file = File(filePath)
        if (!file.exists()) {
            errorMessage = "File does not exist: ${file.name}"
            return@LaunchedEffect
        }
        try {
            val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pfd)
            pageCount = renderer.pageCount
            if (pageCount > 0) {
                val index = currentPageIndex.coerceIn(0, pageCount - 1)
                val page = renderer.openPage(index)
                
                val density = context.resources.displayMetrics.density
                val width = (page.width * density * 1.5f).toInt()
                val height = (page.height * density * 1.5f).toInt()
                
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(android.graphics.Color.WHITE)
                
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                currentBitmap = bitmap
                page.close()
            }
            renderer.close()
            pfd.close()
        } catch (e: Exception) {
            errorMessage = e.localizedMessage ?: "Failed to render PDF"
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(fileName ?: "PDF Viewer", fontWeight = FontWeight.Bold, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            if (errorMessage != null) {
                Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
            } else if (currentBitmap != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = currentBitmap!!.asImageBitmap(),
                            contentDescription = "PDF Page ${currentPageIndex + 1}",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { if (currentPageIndex > 0) currentPageIndex-- },
                            enabled = currentPageIndex > 0
                        ) {
                            Icon(Icons.Rounded.ChevronLeft, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Prev")
                        }

                        Text(
                            text = "Page ${currentPageIndex + 1} of $pageCount",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Button(
                            onClick = { if (currentPageIndex < pageCount - 1) currentPageIndex++ },
                            enabled = currentPageIndex < pageCount - 1
                        ) {
                            Text("Next")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Rounded.ChevronRight, contentDescription = null)
                        }
                    }
                }
            } else {
                CircularProgressIndicator()
            }
        }
    }
}
