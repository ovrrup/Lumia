package lumia.tracker.ui.screens.focus

import androidx.compose.animation.*
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
import lumia.tracker.ui.theme.bouncyClick

/**
 * PomodoroCourseSelector - Redesigned contextual linking selector for Courses and Subjects.
 * Displays current study context with instant quick-switch pill chips and a dedicated ModalBottomSheet
 * with search and filtering for comprehensive academic tracking.
 */
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

    // Active color derived from selected course or subject
    val activeColor = remember(selectedCourse, selectedSubject) {
        selectedCourse?.colorHex?.let {
            try { Color(android.graphics.Color.parseColor(it)) } catch (e: Exception) { null }
        } ?: MaterialTheme.colorScheme.primary
    }

    val contextTitle = when {
        selectedCourse != null -> selectedCourse.name
        selectedSubject != null -> selectedSubject.name
        else -> "General Focus"
    }

    val contextSubtitle = when {
        selectedCourse != null -> if (selectedCourse.code.isNotBlank()) "Course • ${selectedCourse.code}" else "Linked Course"
        selectedSubject != null -> "Linked Subject"
        else -> "No course or subject linked"
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                    imageVector = Icons.Rounded.Link,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "STUDY CONTEXT",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (selectedCourse != null || selectedSubject != null) {
                Text(
                    text = "Clear Link",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
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
            shape = RoundedCornerShape(20.dp),
            containerColor = if (selectedCourse != null || selectedSubject != null) {
                activeColor.copy(alpha = 0.10f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            },
            border = BorderStroke(
                width = 1.dp,
                color = if (selectedCourse != null || selectedSubject != null) {
                    activeColor.copy(alpha = 0.35f)
                } else {
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                }
            ),
            onClick = { showSheet = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                color = if (selectedCourse != null || selectedSubject != null) activeColor.copy(alpha = 0.20f)
                                else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(12.dp)
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
                            tint = if (selectedCourse != null || selectedSubject != null) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = contextTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = contextSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Change",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = Icons.Rounded.UnfoldMore,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Horizontal Quick-Access Pills for Fast 1-Tap Switching
        if (courses.isNotEmpty() || subjects.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                // General (Unlinked) Option Chip
                item {
                    val isGeneralSelected = selectedCourse == null && selectedSubject == null
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isGeneralSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(
                            1.dp,
                            if (isGeneralSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.bouncyClick {
                            onSelectCourse(null)
                            onSelectSubject?.invoke(null)
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Psychology,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (isGeneralSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "General",
                                style = MaterialTheme.typography.labelMedium,
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
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) courseColor.copy(alpha = 0.20f)
                        else MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) courseColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        ),
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
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(courseColor)
                            )
                            Text(
                                text = course.name,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                            if (isSelected) {
                                Icon(
                                    Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = courseColor,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }

                // Subject Quick Chips
                items(subjects, key = { "subject_${it.id}" }) { subject ->
                    val isSelected = selectedSubject?.id == subject.id
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer
                        else MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.secondary
                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        ),
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
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Icon(
                                Icons.Rounded.AutoStories,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = subject.name,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                            if (isSelected) {
                                Icon(
                                    Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
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
            var selectedTab by remember { mutableIntStateOf(0) } // 0: All, 1: Courses, 2: Subjects

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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Link,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "Link Study Context",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black
                        )
                    }

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
                    shape = RoundedCornerShape(16.dp),
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
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. General Study (Unlink) option
                    item {
                        val isGeneral = selectedCourse == null && selectedSubject == null
                        ContextSelectionRow(
                            title = "General Focus (No Context)",
                            subtitle = "Untracked general study session",
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
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }

                        items(filteredCourses, key = { "course_${it.id}" }) { course ->
                            val isSelected = selectedCourse?.id == course.id
                            val courseColor = remember(course.colorHex) {
                                try { Color(android.graphics.Color.parseColor(course.colorHex)) } catch (e: Exception) { null }
                            } ?: MaterialTheme.colorScheme.primary

                            ContextSelectionRow(
                                title = course.name,
                                subtitle = if (course.code.isNotBlank()) "Code: ${course.code}" else "Enrolled Course",
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
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }

                        items(filteredSubjects, key = { "subject_${it.id}" }) { subject ->
                            val isSelected = selectedSubject?.id == subject.id
                            ContextSelectionRow(
                                title = subject.name,
                                subtitle = "Academic Subject",
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
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Rounded.SearchOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(36.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No matching courses or subjects found",
                                        style = MaterialTheme.typography.bodyMedium,
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
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) tintColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) tintColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .bouncyClick(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(tintColor.copy(alpha = 0.20f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tintColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(tintColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
