package lumia.tracker.ui.screens.focus

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lumia.tracker.model.Course
import lumia.tracker.model.Subject
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.GlassCapsule
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.components.ScholarCardDefaults
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.theme.bouncyClick

/**
 * PomodoroCourseSelector - Modern contextual linking selector for Courses and Subjects.
 * Displays current study context with sleek capsule pills, glass borders, active glowing indicators,
 * and a searchable selection sheet.
 */
@ValueScore(
    score = 92,
    importance = Importance.HIGH,
    description = "Academic study context selector with sleek capsule pills, glass borders, and glowing indicators",
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
    val isDark = isSystemInDarkTheme()

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
                GlassCapsule(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
                    border = ScholarCardDefaults.glassBorder(
                        isDark = isDark,
                        accentColor = MaterialTheme.colorScheme.error,
                        width = 0.8.dp
                    ),
                    onClick = {
                        onSelectCourse(null)
                        onSelectSubject?.invoke(null)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = null,
                            modifier = Modifier.size(11.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Clear",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // Active Context Card Trigger
        ScholarCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            glassmorphic = true,
            containerColor = if (isLinked) activeColor.copy(alpha = 0.08f) else ScholarCardDefaults.glassContainerColor(isDark, alpha = 0.55f),
            border = ScholarCardDefaults.glassBorder(
                isDark = isDark,
                accentColor = if (isLinked) activeColor else null,
                width = 1.dp
            ),
            onClick = { showSheet = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 11.dp),
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
                            .size(36.dp)
                            .background(
                                color = if (isLinked) activeColor.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                                shape = RoundedCornerShape(11.dp)
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
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = contextTitle,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (isLinked) {
                                ActiveGlowingIndicator(color = activeColor, size = 5.dp)
                            }
                        }
                        Text(
                            text = contextSubtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isLinked) {
                        GlassCapsule(
                            containerColor = activeColor.copy(alpha = 0.14f),
                            border = ScholarCardDefaults.glassBorder(isDark = isDark, accentColor = activeColor, width = 0.8.dp)
                        ) {
                            Text(
                                text = "Active",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = activeColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
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
        }

        // Horizontal Quick-Access Sleek Capsule Pills
        if (courses.isNotEmpty() || subjects.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 3.dp)
            ) {
                // General (Unlinked) Option Capsule Pill
                item {
                    val isGeneralSelected = selectedCourse == null && selectedSubject == null
                    val generalBorder = ScholarCardDefaults.glassBorder(
                        isDark = isDark,
                        accentColor = if (isGeneralSelected) MaterialTheme.colorScheme.primary else null,
                        width = if (isGeneralSelected) 1.2.dp else 0.75.dp
                    )
                    val generalContainerColor = if (isGeneralSelected) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                    } else {
                        ScholarCardDefaults.glassContainerColor(isDark, alpha = 0.5f)
                    }

                    Surface(
                        shape = CircleShape,
                        color = generalContainerColor,
                        border = generalBorder,
                        modifier = Modifier
                            .clip(CircleShape)
                            .bouncyClick {
                                onSelectCourse(null)
                                onSelectSubject?.invoke(null)
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            if (isGeneralSelected) {
                                ActiveGlowingIndicator(color = MaterialTheme.colorScheme.primary, size = 6.dp)
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), CircleShape)
                                )
                            }
                            Text(
                                text = "General",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isGeneralSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isGeneralSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.2.sp
                            )
                        }
                    }
                }

                // Course Quick Capsule Pills
                items(courses, key = { "course_${it.id}" }) { course ->
                    val isSelected = selectedCourse?.id == course.id
                    val courseColor = remember(course.colorHex) {
                        try { Color(android.graphics.Color.parseColor(course.colorHex)) } catch (e: Exception) { null }
                    } ?: MaterialTheme.colorScheme.primary

                    val courseBorder = ScholarCardDefaults.glassBorder(
                        isDark = isDark,
                        accentColor = if (isSelected) courseColor else null,
                        width = if (isSelected) 1.2.dp else 0.75.dp
                    )
                    val courseContainerColor = if (isSelected) {
                        courseColor.copy(alpha = 0.18f)
                    } else {
                        ScholarCardDefaults.glassContainerColor(isDark, alpha = 0.5f)
                    }

                    Surface(
                        shape = CircleShape,
                        color = courseContainerColor,
                        border = courseBorder,
                        modifier = Modifier
                            .clip(CircleShape)
                            .bouncyClick {
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
                            if (isSelected) {
                                ActiveGlowingIndicator(color = courseColor, size = 6.dp)
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(courseColor.copy(alpha = 0.7f), CircleShape)
                                )
                            }
                            Text(
                                text = course.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.2.sp,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Subject Quick Capsule Pills
                items(subjects, key = { "subject_${it.id}" }) { subject ->
                    val isSelected = selectedSubject?.id == subject.id
                    val subjectColor = MaterialTheme.colorScheme.secondary

                    val subjectBorder = ScholarCardDefaults.glassBorder(
                        isDark = isDark,
                        accentColor = if (isSelected) subjectColor else null,
                        width = if (isSelected) 1.2.dp else 0.75.dp
                    )
                    val subjectContainerColor = if (isSelected) {
                        subjectColor.copy(alpha = 0.18f)
                    } else {
                        ScholarCardDefaults.glassContainerColor(isDark, alpha = 0.5f)
                    }

                    Surface(
                        shape = CircleShape,
                        color = subjectContainerColor,
                        border = subjectBorder,
                        modifier = Modifier
                            .clip(CircleShape)
                            .bouncyClick {
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
                            if (isSelected) {
                                ActiveGlowingIndicator(color = subjectColor, size = 6.dp)
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(subjectColor.copy(alpha = 0.7f), CircleShape)
                                )
                            }
                            Text(
                                text = subject.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.2.sp,
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

                // Sleek Capsule Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { 
                        Text("Search course or subject...", style = MaterialTheme.typography.bodyMedium) 
                    },
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
                    shape = CircleShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
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

                // Scrollable List of Sleek Capsule Context Items
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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

/**
 * ActiveGlowingIndicator - Pulsing glow indicator for active course and context selections.
 */
@Composable
private fun ActiveGlowingIndicator(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 7.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "active_glow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = modifier.size(size * 1.8f),
        contentAlignment = Alignment.Center
    ) {
        // Outer pulsing glow halo
        Box(
            modifier = Modifier
                .size(size * 1.8f)
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                    alpha = pulseAlpha * 0.45f
                }
                .background(color, CircleShape)
        )
        // Solid core dot
        Box(
            modifier = Modifier
                .size(size)
                .background(color, CircleShape)
        )
    }
}

@ValueScore(
    score = 75,
    importance = Importance.MEDIUM,
    description = "Sleek capsule pill context selection row with glass border, glowing check badge, and clean typography",
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
    val isDark = isSystemInDarkTheme()
    val rowBorder = ScholarCardDefaults.glassBorder(
        isDark = isDark,
        accentColor = if (isSelected) tintColor else null,
        width = if (isSelected) 1.2.dp else 0.75.dp
    )
    val rowContainerColor = if (isSelected) {
        tintColor.copy(alpha = 0.15f)
    } else {
        ScholarCardDefaults.glassContainerColor(isDark, alpha = 0.5f)
    }

    Surface(
        shape = CircleShape,
        color = rowContainerColor,
        border = rowBorder,
        modifier = Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .bouncyClick(onClick = onClick)
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(tintColor.copy(alpha = if (isSelected) 0.22f else 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tintColor,
                        modifier = Modifier.size(17.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 0.15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        letterSpacing = 0.1.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (isSelected) {
                Box(
                    modifier = Modifier.size(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "row_glow_${title}")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 0.85f,
                        targetValue = 1.35f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulse_scale"
                    )
                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 0.75f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulse_alpha"
                    )

                    // Glowing halo ring
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .graphicsLayer {
                                scaleX = pulseScale
                                scaleY = pulseScale
                                alpha = pulseAlpha
                            }
                            .background(tintColor.copy(alpha = 0.3f), CircleShape)
                    )

                    // Inner checkmark badge
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .background(tintColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

