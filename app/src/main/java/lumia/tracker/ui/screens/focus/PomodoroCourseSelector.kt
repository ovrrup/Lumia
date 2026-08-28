package lumia.tracker.ui.screens.focus

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lumia.tracker.model.Course
import lumia.tracker.model.Subject
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.theme.bouncyClick

/**
 * PomodoroCourseSelector - Clean contextual linking selector for Courses and Subjects.
 * Displays current study context with compact quick chips and a searchable selection sheet.
 */
@ValueScore(
    score = 88,
    importance = Importance.HIGH,
    description = "Academic study context selector with clean quick chips and searchable bottom sheet",
    category = "Focus"
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroCourseSelector(
    courses: List<Course>,
    selectedCourse: Course?,
    onSelectCourse: (Course?) -> Unit,
    modifier: Modifier = Modifier,
    subjects: List<Subject> = emptyList(),
    selectedSubject: Subject? = null,
    onSelectSubject: ((Subject?) -> Unit)? = null
) {
    var showSheet by remember { mutableStateOf(false) }

    val defaultColor = MaterialTheme.colorScheme.primary
    val activeColor = remember(selectedCourse, selectedSubject, defaultColor) {
        selectedCourse?.colorHex?.let {
            try { Color(android.graphics.Color.parseColor(it)) } catch (e: Exception) { null }
        } ?: defaultColor
    }

    val isLinked = selectedCourse != null || selectedSubject != null
    val contextTitle = when {
        selectedCourse != null -> selectedCourse.name
        selectedSubject != null -> selectedSubject.name
        else -> "General Focus"
    }

    val contextSubtitle = when {
        selectedCourse != null -> if (selectedCourse.code.isNotBlank()) selectedCourse.code else "Course"
        selectedSubject != null -> "Subject"
        else -> "No course linked"
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.School,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Study Context",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isLinked) {
                Text(
                    text = "Clear",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .bouncyClick {
                            onSelectCourse(null)
                            onSelectSubject?.invoke(null)
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        // Active Context Card Trigger
        ScholarCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            containerColor = if (isLinked) activeColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(
                width = 1.dp,
                color = if (isLinked) activeColor.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
            ),
            onClick = { showSheet = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(
                                color = if (isLinked) activeColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                selectedCourse != null -> Icons.Rounded.School
                                selectedSubject != null -> Icons.Rounded.AutoStories
                                else -> Icons.Rounded.Psychology
                            },
                            contentDescription = null,
                            tint = if (isLinked) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = contextTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = contextSubtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = "Select context",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }

        // Horizontal Quick-Access Chips
        if (courses.isNotEmpty() || subjects.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                // General (Unlinked) Option Chip
                item {
                    val isGeneralSelected = selectedCourse == null && selectedSubject == null
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isGeneralSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.bouncyClick {
                            onSelectCourse(null)
                            onSelectSubject?.invoke(null)
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "General",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isGeneralSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isGeneralSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Course Quick Chips
                items(courses, key = { "course_${it.id}" }) { course ->
                    val isSelected = selectedCourse?.id == course.id
                    val courseColor = remember(course.colorHex) {
                        try { Color(android.graphics.Color.parseColor(course.colorHex)) } catch (e: Exception) { null }
                    } ?: MaterialTheme.colorScheme.primary

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) courseColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainer,
                        border = if (isSelected) BorderStroke(1.dp, courseColor) else null,
                        modifier = Modifier.bouncyClick {
                            if (isSelected) {
                                onSelectCourse(null)
                            } else {
                                onSelectSubject?.invoke(null)
                                onSelectCourse(course)
                            }
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(courseColor)
                            )
                            Text(
                                text = course.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Subject Quick Chips
                items(subjects, key = { "subject_${it.id}" }) { subject ->
                    val isSelected = selectedSubject?.id == subject.id
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                        border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.secondary) else null,
                        modifier = Modifier.bouncyClick {
                            if (isSelected) {
                                onSelectSubject?.invoke(null)
                            } else {
                                onSelectCourse(null)
                                onSelectSubject?.invoke(subject)
                            }
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = subject.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }

    // ModalBottomSheet for Comprehensive Context Selection
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            var searchQuery by remember { mutableStateOf("") }
            var selectedTab by remember { mutableIntStateOf(0) }

            val filteredCourses = remember(courses, searchQuery, selectedTab) {
                if (selectedTab == 2) emptyList()
                else courses.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                    it.code.contains(searchQuery, ignoreCase = true)
                }
            }

            val filteredSubjects = remember(subjects, searchQuery, selectedTab) {
                if (selectedTab == 1) emptyList()
                else subjects.filter {
                    it.name.contains(searchQuery, ignoreCase = true)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Select Study Context",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    BouncyIconButton(onClick = { showSheet = false }) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close")
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search course or subject...") },
                    leadingIcon = {
                        Icon(Icons.Rounded.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Category Filter Segmented Tabs
                if (subjects.isNotEmpty() && courses.isNotEmpty()) {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                        ) {
                            Text("All", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
                        }
                        SegmentedButton(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                        ) {
                            Text("Courses (${courses.size})", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
                        }
                        SegmentedButton(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                        ) {
                            Text("Subjects (${subjects.size})", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }

                // Scrollable List of Contexts
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // 1. General Study (Unlink) option
                    item {
                        val isGeneral = selectedCourse == null && selectedSubject == null
                        ContextSelectionRow(
                            title = "General Focus",
                            subtitle = "Untracked study session",
                            icon = Icons.Rounded.Psychology,
                            tintColor = MaterialTheme.colorScheme.primary,
                            isSelected = isGeneral,
                            onClick = {
                                onSelectCourse(null)
                                onSelectSubject?.invoke(null)
                                showSheet = false
                            }
                        )
                    }

                    // 2. Courses Section
                    if (filteredCourses.isNotEmpty()) {
                        item {
                            Text(
                                text = "COURSES",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                            )
                        }

                        items(filteredCourses, key = { "course_${it.id}" }) { course ->
                            val isSelected = selectedCourse?.id == course.id
                            val courseColor = remember(course.colorHex) {
                                try { Color(android.graphics.Color.parseColor(course.colorHex)) } catch (e: Exception) { null }
                            } ?: MaterialTheme.colorScheme.primary

                            ContextSelectionRow(
                                title = course.name,
                                subtitle = if (course.code.isNotBlank()) "Code: ${course.code}" else "Course",
                                icon = Icons.Rounded.School,
                                tintColor = courseColor,
                                isSelected = isSelected,
                                onClick = {
                                    onSelectSubject?.invoke(null)
                                    onSelectCourse(course)
                                    showSheet = false
                                }
                            )
                        }
                    }

                    // 3. Subjects Section
                    if (filteredSubjects.isNotEmpty()) {
                        item {
                            Text(
                                text = "SUBJECTS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                            )
                        }

                        items(filteredSubjects, key = { "subject_${it.id}" }) { subject ->
                            val isSelected = selectedSubject?.id == subject.id
                            ContextSelectionRow(
                                title = subject.name,
                                subtitle = "Subject",
                                icon = Icons.Rounded.AutoStories,
                                tintColor = MaterialTheme.colorScheme.secondary,
                                isSelected = isSelected,
                                onClick = {
                                    onSelectCourse(null)
                                    onSelectSubject?.invoke(subject)
                                    showSheet = false
                                }
                            )
                        }
                    }

                    // Empty state when search yields no matches
                    if (filteredCourses.isEmpty() && filteredSubjects.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Rounded.SearchOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "No matching courses or subjects",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@ValueScore(
    score = 65,
    importance = Importance.MEDIUM,
    description = "Context selection row for courses and subjects within the bottom sheet",
    category = "Focus"
)
@Composable
private fun ContextSelectionRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tintColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) tintColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) tintColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .bouncyClick(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(tintColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tintColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(tintColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

