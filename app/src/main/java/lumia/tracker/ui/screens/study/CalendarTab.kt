package lumia.tracker.ui.screens.study

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.model.Course
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.theme.bouncyClick
import lumia.tracker.viewmodel.ScholarViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * CalendarDayItem - Represents a single day in the weekly timetable strip with full date context.
 */
private data class CalendarDayItem(
    val calendar: Calendar,
    val dayOfWeekName: String,
    val shortDayName: String,
    val dayOfMonth: Int,
    val dateMillis: Long,
    val isToday: Boolean
)

/**
 * Safely parses a hex color string with a fallback Color if parsing fails.
 */
private fun parseHexColor(hex: String?, fallback: Color): Color {
    if (hex.isNullOrBlank()) return fallback
    return try {
        val cleanHex = hex.trim().let { if (it.startsWith("#")) it else "#$it" }
        Color(android.graphics.Color.parseColor(cleanHex))
    } catch (_: Exception) {
        fallback
    }
}

/**
 * Parses time string like '10:00 AM' or '14:30' into epoch milliseconds for the target calendar date.
 */
private fun parseTimeToDayMillis(timeStr: String, baseCal: Calendar): Long? {
    if (timeStr.isBlank()) return null
    val trimmed = timeStr.trim().uppercase(Locale.US)
    val formats = listOf("hh:mm a", "h:mm a", "HH:mm", "H:mm")
    for (format in formats) {
        try {
            val sdf = SimpleDateFormat(format, Locale.US)
            sdf.isLenient = true
            val date = sdf.parse(trimmed)
            if (date != null) {
                val timeCal = Calendar.getInstance().apply { time = date }
                return (baseCal.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }
        } catch (_: Exception) {}
    }
    return null
}

/**
 * Accurately determines if a course schedule includes the specified day,
 * supporting full names ('Monday'), abbreviations ('Mon'), and comma-separated schedules.
 */
private fun isCourseScheduledForDay(scheduleDays: String, targetDay: String): Boolean {
    if (scheduleDays.isBlank() || targetDay.isBlank()) return false
    val shortTarget = targetDay.take(3).lowercase(Locale.US)
    return scheduleDays.split(",").any { day ->
        val trimmed = day.trim().lowercase(Locale.US)
        trimmed == targetDay.lowercase(Locale.US) || trimmed.startsWith(shortTarget) || shortTarget.startsWith(trimmed)
    }
}

/**
 * CircularAttendanceButton - Tactile circular toggle for instantaneous 1-tap attendance marking.
 */
@Composable
private fun CircularAttendanceButton(
    selected: Boolean,
    activeColor: Color,
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) activeColor else activeColor.copy(alpha = 0.08f),
        label = "att_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) activeColor else activeColor.copy(alpha = 0.25f),
        label = "att_border"
    )
    val iconColor by animateColorAsState(
        targetValue = if (selected) Color.White else activeColor.copy(alpha = 0.7f),
        label = "att_icon"
    )

    Surface(
        shape = CircleShape,
        color = bgColor,
        border = BorderStroke(0.8.dp, borderColor),
        modifier = modifier
            .size(34.dp)
            .clip(CircleShape)
            .bouncyClick(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * CalendarTab - Modern Academic Timetable & Calendar.
 * Features a 7-day capsule horizontal strip, live selection indicators, subtle glassmorphic backdrop,
 * timeline schedule cards with capsule time badges, and circular attendance toggles.
 */
@ValueScore(
    score = 94,
    importance = Importance.HIGH,
    description = "Modern Academic Timetable & Calendar with capsule day strip, timeline cards, and circular attendance toggles",
    category = "Study"
)
@Composable
fun CalendarTab(
    navController: NavController,
    viewModel: ScholarViewModel,
    bottomPadding: PaddingValues
) {
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val allAttendanceRecords by viewModel.allAttendanceRecords.collectAsStateWithLifecycle()

    var weekOffset by rememberSaveable { mutableIntStateOf(0) }

    val todayCalendar = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
    val todayDayOfWeek = todayCalendar.get(Calendar.DAY_OF_WEEK)
    val todayDayIndex = if (todayDayOfWeek == Calendar.SUNDAY) 6 else todayDayOfWeek - Calendar.MONDAY
    var selectedDayIndex by rememberSaveable { mutableIntStateOf(todayDayIndex) }

    val weekDays = remember(weekOffset) {
        val base = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val dow = base.get(Calendar.DAY_OF_WEEK)
        val diffToMonday = if (dow == Calendar.SUNDAY) 6 else dow - Calendar.MONDAY
        base.add(Calendar.DAY_OF_MONTH, -diffToMonday + (weekOffset * 7))

        val todayMidnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        (0..6).map { offset ->
            val dayCal = (base.clone() as Calendar).apply {
                add(Calendar.DAY_OF_MONTH, offset)
            }
            val isToday = dayCal.get(Calendar.YEAR) == todayMidnight.get(Calendar.YEAR) &&
                    dayCal.get(Calendar.DAY_OF_YEAR) == todayMidnight.get(Calendar.DAY_OF_YEAR)
            CalendarDayItem(
                calendar = dayCal,
                dayOfWeekName = SimpleDateFormat("EEEE", Locale.US).format(dayCal.time),
                shortDayName = SimpleDateFormat("EEE", Locale.US).format(dayCal.time),
                dayOfMonth = dayCal.get(Calendar.DAY_OF_MONTH),
                dateMillis = dayCal.timeInMillis,
                isToday = isToday
            )
        }
    }

    val currentDayItem = weekDays[selectedDayIndex.coerceIn(0, 6)]
    val monthYearFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val currentMonthYear = remember(currentDayItem) {
        monthYearFormat.format(currentDayItem.calendar.time)
    }

    val sortedCoursesForDay = remember(courses, currentDayItem) {
        courses.filter { course ->
            isCourseScheduledForDay(course.scheduleDays, currentDayItem.dayOfWeekName)
        }.sortedBy { it.scheduleStartTime }
    }

    val liveTransition = rememberInfiniteTransition(label = "cal_live_anim")
    val livePulseScale by liveTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottomPadding)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Header with Month/Year, Class count capsule, and Today / Week Navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = currentMonthYear,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "${sortedCoursesForDay.size}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (!currentDayItem.isToday || weekOffset != 0) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .clip(CircleShape)
                            .bouncyClick {
                                weekOffset = 0
                                selectedDayIndex = todayDayIndex
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Today,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Today",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                BouncyIconButton(
                    onClick = { weekOffset-- },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Rounded.ChevronLeft,
                        contentDescription = "Previous week",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                BouncyIconButton(
                    onClick = { weekOffset++ },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Rounded.ChevronRight,
                        contentDescription = "Next week",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 7-Day Horizontal Calendar Strip in Subtle Glassmorphic Backdrop
        ScholarCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            glassmorphic = true
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                weekDays.forEachIndexed { index, dayInfo ->
                    val isSelected = index == selectedDayIndex
                    val coursesForPill = remember(courses, dayInfo.dayOfWeekName) {
                        courses.filter { course ->
                            isCourseScheduledForDay(course.scheduleDays, dayInfo.dayOfWeekName)
                        }
                    }

                    val pillBg by animateColorAsState(
                        targetValue = when {
                            isSelected -> MaterialTheme.colorScheme.primary
                            dayInfo.isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                            else -> Color.Transparent
                        },
                        animationSpec = tween(200),
                        label = "pill_bg_$index"
                    )

                    val pillBorder by animateColorAsState(
                        targetValue = when {
                            isSelected -> MaterialTheme.colorScheme.primary
                            dayInfo.isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                        },
                        animationSpec = tween(200),
                        label = "pill_border_$index"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(pillBg)
                            .border(
                                width = if (isSelected) 1.dp else 0.5.dp,
                                color = pillBorder,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .bouncyClick {
                                selectedDayIndex = index
                            }
                            .padding(vertical = 10.dp)
                    ) {
                        Text(
                            text = dayInfo.shortDayName.take(3),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected || dayInfo.isToday) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else if (dayInfo.isToday) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = "${dayInfo.dayOfMonth}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else if (dayInfo.isToday) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(5.dp))

                        // Active Indicator & Scheduled Course Dots
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.height(6.dp)
                        ) {
                            if (dayInfo.isToday && isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .scale(livePulseScale)
                                        .background(MaterialTheme.colorScheme.onPrimary, CircleShape)
                                )
                            } else if (dayInfo.isToday && !isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .scale(livePulseScale)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                )
                            } else if (coursesForPill.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f)
                                            else Color.Transparent,
                                            CircleShape
                                        )
                                )
                            } else {
                                coursesForPill.take(3).forEach { c ->
                                    val dotColor = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                                    } else {
                                        parseHexColor(c.colorHex, MaterialTheme.colorScheme.primary)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .background(dotColor, CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Schedule Content Area
        if (sortedCoursesForDay.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                ScholarCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    glassmorphic = true
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Rounded.CalendarMonth,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No classes scheduled",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(0.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                itemsIndexed(
                    sortedCoursesForDay,
                    key = { index, course -> "cal_course_${course.id}_${currentDayItem.dateMillis}_$index" }
                ) { index, course ->
                    val defaultPrimaryColor = MaterialTheme.colorScheme.primary
                    val courseColor = remember(course.colorHex, defaultPrimaryColor) {
                        parseHexColor(course.colorHex, defaultPrimaryColor)
                    }

                    val existingAttendance = remember(allAttendanceRecords, course.id, currentDayItem.dateMillis) {
                        allAttendanceRecords.firstOrNull {
                            it.courseId == course.id && it.dateMillis == currentDayItem.dateMillis
                        }
                    }

                    val isSelectedDayToday = currentDayItem.isToday
                    val currentTimeMillis = System.currentTimeMillis()
                    val startMillis = remember(course.scheduleStartTime, isSelectedDayToday, currentDayItem.dateMillis) {
                        if (isSelectedDayToday && course.scheduleStartTime.isNotBlank()) {
                            parseTimeToDayMillis(course.scheduleStartTime, currentDayItem.calendar)
                        } else null
                    }
                    val endMillis = remember(course.scheduleEndTime, isSelectedDayToday, currentDayItem.dateMillis) {
                        if (isSelectedDayToday && course.scheduleEndTime.isNotBlank()) {
                            parseTimeToDayMillis(course.scheduleEndTime, currentDayItem.calendar)
                        } else null
                    }

                    val isLiveNow = isSelectedDayToday && startMillis != null && endMillis != null &&
                            currentTimeMillis in startMillis..endMillis
                    val liveProgress = if (isLiveNow && startMillis != null && endMillis != null && endMillis > startMillis) {
                        ((currentTimeMillis - startMillis).toFloat() / (endMillis - startMillis)).coerceIn(0f, 1f)
                    } else 0f

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Vertical Timeline Rail
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(18.dp)
                        ) {
                            Spacer(modifier = Modifier.height(20.dp))
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(
                                        if (isLiveNow) MaterialTheme.colorScheme.primary else courseColor,
                                        CircleShape
                                    )
                                    .border(
                                        2.dp,
                                        if (isLiveNow) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                        else MaterialTheme.colorScheme.surface,
                                        CircleShape
                                    )
                            )
                            if (index < sortedCoursesForDay.lastIndex) {
                                Box(
                                    modifier = Modifier
                                        .padding(vertical = 4.dp)
                                        .width(2.dp)
                                        .height(88.dp)
                                        .background(
                                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                            CircleShape
                                        )
                                )
                            }
                        }

                        // Frosted Glass Card
                        ScholarCard(
                            modifier = Modifier
                                .weight(1f)
                                .padding(bottom = 12.dp),
                            shape = RoundedCornerShape(20.dp),
                            glassmorphic = true,
                            border = if (isLiveNow) {
                                BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                            } else null,
                            onClick = {
                                navController.navigate("courseDetail/${course.id}")
                            }
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                if (isLiveNow) {
                                    LinearProgressIndicator(
                                        progress = { liveProgress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(3.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    )
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp)
                                ) {
                                    // Top Row: Course color accent, Name, Code badge, and Live badge
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(width = 4.dp, height = 24.dp)
                                                .clip(CircleShape)
                                                .background(courseColor)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))

                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = course.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            if (course.code.isNotBlank()) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
                                                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                                ) {
                                                    Text(
                                                        text = course.code,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }

                                        if (isLiveNow) {
                                            Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .scale(livePulseScale)
                                                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                                                    )
                                                    Text(
                                                        text = "LIVE",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Middle Row: Capsule Time Badge & Instructor
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        val timeStr = if (course.scheduleStartTime.isNotBlank() && course.scheduleEndTime.isNotBlank()) {
                                            "${course.scheduleStartTime} – ${course.scheduleEndTime}"
                                        } else {
                                            course.schedule.ifBlank { "Schedule TBA" }
                                        }
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.55f),
                                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                                            ) {
                                                Icon(
                                                    Icons.Rounded.Schedule,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(13.dp),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = timeStr,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        if (course.instructor.isNotBlank()) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    Icons.Rounded.Person,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp),
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                )
                                                Text(
                                                    text = course.instructor,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Bottom Row: Status badge / label & Circular Attendance Toggles
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        if (existingAttendance != null) {
                                            val statusColor = when (existingAttendance.status.lowercase(Locale.US)) {
                                                "present" -> Color(0xFF10B981)
                                                "late" -> Color(0xFFF59E0B)
                                                "absent" -> Color(0xFFEF4444)
                                                else -> MaterialTheme.colorScheme.primary
                                            }
                                            Surface(
                                                shape = CircleShape,
                                                color = statusColor.copy(alpha = 0.12f),
                                                border = BorderStroke(0.5.dp, statusColor.copy(alpha = 0.35f))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .background(statusColor, CircleShape)
                                                    )
                                                    Text(
                                                        text = existingAttendance.status,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = statusColor
                                                    )
                                                }
                                            }
                                        } else {
                                            Text(
                                                text = "Attendance",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }

                                        // Circular Attendance Toggles: Present, Late, Absent
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val currentStatus = existingAttendance?.status?.lowercase(Locale.US) ?: ""

                                            CircularAttendanceButton(
                                                selected = currentStatus == "present",
                                                activeColor = Color(0xFF10B981),
                                                icon = Icons.Rounded.Check,
                                                contentDescription = "Present",
                                                onClick = {
                                                    if (currentStatus == "present") {
                                                        existingAttendance?.let { viewModel.deleteAttendanceRecord(it) }
                                                    } else {
                                                        if (existingAttendance != null) {
                                                            viewModel.updateAttendanceRecord(existingAttendance.copy(status = "Present"))
                                                        } else {
                                                            viewModel.addAttendanceRecord(course.id, currentDayItem.dateMillis, "Present")
                                                        }
                                                    }
                                                }
                                            )

                                            CircularAttendanceButton(
                                                selected = currentStatus == "late",
                                                activeColor = Color(0xFFF59E0B),
                                                icon = Icons.Rounded.Schedule,
                                                contentDescription = "Late",
                                                onClick = {
                                                    if (currentStatus == "late") {
                                                        existingAttendance?.let { viewModel.deleteAttendanceRecord(it) }
                                                    } else {
                                                        if (existingAttendance != null) {
                                                            viewModel.updateAttendanceRecord(existingAttendance.copy(status = "Late"))
                                                        } else {
                                                            viewModel.addAttendanceRecord(course.id, currentDayItem.dateMillis, "Late")
                                                        }
                                                    }
                                                }
                                            )

                                            CircularAttendanceButton(
                                                selected = currentStatus == "absent",
                                                activeColor = Color(0xFFEF4444),
                                                icon = Icons.Rounded.Close,
                                                contentDescription = "Absent",
                                                onClick = {
                                                    if (currentStatus == "absent") {
                                                        existingAttendance?.let { viewModel.deleteAttendanceRecord(it) }
                                                    } else {
                                                        if (existingAttendance != null) {
                                                            viewModel.updateAttendanceRecord(existingAttendance.copy(status = "Absent"))
                                                        } else {
                                                            viewModel.addAttendanceRecord(course.id, currentDayItem.dateMillis, "Absent")
                                                        }
                                                    }
                                                }
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
    }
}
