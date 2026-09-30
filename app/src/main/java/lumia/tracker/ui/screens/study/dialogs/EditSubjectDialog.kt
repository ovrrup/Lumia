package lumia.tracker.ui.screens.study.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lumia.tracker.model.Subject
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.theme.bouncyClick
import lumia.tracker.viewmodel.ScholarViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@ValueScore(
    score = 92,
    importance = Importance.HIGH,
    description = "Modern gesture-driven modal bottom sheet for editing academic subjects with capsule pills, tags, and course linkages",
    category = "Dialog"
)
@Composable
fun EditSubjectDialog(
    subject: Subject,
    viewModel: ScholarViewModel,
    onDismiss: () -> Unit
) {
    var name by remember(subject) { mutableStateOf(subject.name) }
    var tags by remember(subject) { mutableStateOf(subject.tags) }
    var nameTouched by remember { mutableStateOf(false) }

    val courses by viewModel.courses.collectAsStateWithLifecycle()

    // Compute initially linked courses
    val initiallyLinkedCourseIds = remember(courses, subject.id) {
        courses.filter { course ->
            course.subjectId == subject.id ||
                course.subjectIds.split(",").mapNotNull { s -> s.trim().toIntOrNull() }.contains(subject.id)
        }.map { it.id }.toSet()
    }

    var selectedCourseIds by remember { mutableStateOf(initiallyLinkedCourseIds) }
    var coursesInitialized by remember { mutableStateOf(false) }
    if (!coursesInitialized && courses.isNotEmpty()) {
        selectedCourseIds = initiallyLinkedCourseIds
        coursesInitialized = true
    }

    val initialColorHex = remember(courses, subject.id) {
        courses.firstOrNull {
            it.subjectId == subject.id ||
                it.subjectIds.split(",").mapNotNull { s -> s.trim().toIntOrNull() }.contains(subject.id)
        }?.colorHex ?: "#3197D6"
    }
    var selectedColor by remember(subject) { mutableStateOf(initialColorHex) }

    val accentColor = remember(selectedColor) {
        try {
            Color(android.graphics.Color.parseColor(selectedColor))
        } catch (e: Exception) {
            Color(0xFF3197D6)
        }
    }

    val popularPresetTags = remember {
        listOf("Core", "Major", "Theory", "Elective", "Lab", "Practical", "Seminar")
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 6.dp)
        ) {
            // Header Row: Icon + Title/Subtitle + Circular Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = accentColor.copy(alpha = 0.16f),
                        border = BorderStroke(0.8.dp, accentColor.copy(alpha = 0.35f)),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Edit Subject",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Update details, tags, and linkages",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .bouncyClick(onClick = onDismiss),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Scrollable Form Content
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Subject Name
                StudyDialogTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameTouched = true
                    },
                    label = "Subject Name *",
                    placeholder = "e.g. Physics",
                    leadingIcon = Icons.Rounded.AutoStories,
                    isError = nameTouched && name.trim().isBlank(),
                    errorMessage = "Subject name is required"
                )

                // Theme Color Presets
                ColorPickerPalette(
                    selectedColor = selectedColor,
                    onColorSelected = { selectedColor = it },
                    title = "Theme Color",
                    colors = STUDY_COLOR_PALETTE
                )

                // Tag Selector Capsule Pills
                Column(modifier = Modifier.fillMaxWidth()) {
                    StudyDialogSectionHeader(
                        title = "Tags",
                        icon = Icons.Rounded.Tag,
                        color = accentColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val currentTags = remember(tags) {
                        tags.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        popularPresetTags.forEach { preset ->
                            val isSelected = currentTags.any { it.equals(preset, ignoreCase = true) }
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) accentColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = BorderStroke(
                                    width = if (isSelected) 1.2.dp else 0.8.dp,
                                    color = if (isSelected) accentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                ),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .bouncyClick {
                                        val updated = if (isSelected) {
                                            currentTags.filter { !it.equals(preset, ignoreCase = true) }
                                        } else {
                                            currentTags + preset
                                        }
                                        tags = updated.joinToString(", ")
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = null,
                                            tint = accentColor,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                    Text(
                                        text = preset,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    StudyDialogTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = "Custom Tags (Optional)",
                        placeholder = "e.g. Core, Major",
                        leadingIcon = Icons.Rounded.Tag
                    )
                }

                // Course Linker Capsule Pills
                if (courses.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        StudyDialogSectionHeader(
                            title = "Link to Course",
                            icon = Icons.Rounded.School,
                            badgeText = if (selectedCourseIds.isNotEmpty()) "${selectedCourseIds.size} Linked" else null,
                            color = accentColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val isNone = selectedCourseIds.isEmpty()
                            Surface(
                                shape = CircleShape,
                                color = if (isNone) accentColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = BorderStroke(
                                    width = if (isNone) 1.2.dp else 0.8.dp,
                                    color = if (isNone) accentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                ),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .bouncyClick { selectedCourseIds = emptySet() }
                            ) {
                                Text(
                                    text = "None",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isNone) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isNone) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                )
                            }

                            courses.forEach { course ->
                                val isCourseSelected = selectedCourseIds.contains(course.id)
                                val courseColor = try {
                                    Color(android.graphics.Color.parseColor(course.colorHex))
                                } catch (e: Exception) {
                                    accentColor
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = if (isCourseSelected) courseColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                                    border = BorderStroke(
                                        width = if (isCourseSelected) 1.2.dp else 0.8.dp,
                                        color = if (isCourseSelected) courseColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                    ),
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .bouncyClick {
                                            selectedCourseIds = if (isCourseSelected) {
                                                selectedCourseIds - course.id
                                            } else {
                                                selectedCourseIds + course.id
                                            }
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(courseColor, CircleShape)
                                        )
                                        Text(
                                            text = course.code.ifBlank { course.name },
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isCourseSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isCourseSelected) courseColor else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (isCourseSelected) {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = null,
                                                tint = courseColor,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Bottom Action Button
            BouncyButton(
                onClick = {
                    if (name.trim().isNotBlank()) {
                        val trimmedName = name.trim()
                        val trimmedTags = tags.trim()

                        viewModel.updateSubject(subject.copy(name = trimmedName, tags = trimmedTags))

                        // Update course linkages
                        courses.forEach { course ->
                            val currentSubjIds = course.subjectIds.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
                            val isLinked = selectedCourseIds.contains(course.id)
                            if (isLinked && !currentSubjIds.contains(subject.id)) {
                                val updatedIds = (currentSubjIds + subject.id).joinToString(",")
                                val firstId = course.subjectId ?: subject.id
                                viewModel.updateCourse(course.copy(subjectIds = updatedIds, subjectId = firstId))
                            } else if (!isLinked && currentSubjIds.contains(subject.id)) {
                                val remaining = currentSubjIds - subject.id
                                val updatedIds = remaining.joinToString(",")
                                val firstId = if (course.subjectId == subject.id) remaining.firstOrNull() else course.subjectId
                                viewModel.updateCourse(course.copy(subjectIds = updatedIds, subjectId = firstId))
                            }
                        }

                        if (trimmedTags.isNotBlank() && selectedColor.isNotBlank()) {
                            val primaryTag = trimmedTags.split(",").firstOrNull()?.trim()
                            if (!primaryTag.isNullOrBlank()) {
                                viewModel.insertTagCustomization(
                                    tagName = primaryTag,
                                    colorHex = selectedColor,
                                    description = "Subject: $trimmedName",
                                    isFavorite = false
                                )
                            }
                        }

                        onDismiss()
                    } else {
                        nameTouched = true
                    }
                },
                enabled = name.trim().isNotBlank(),
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                    Text("Save Changes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
