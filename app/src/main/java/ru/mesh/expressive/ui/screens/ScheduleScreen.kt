package ru.mesh.expressive.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
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
import ru.mesh.expressive.data.model.AttendanceType
import ru.mesh.expressive.data.model.LessonScheduleItem
import ru.mesh.expressive.ui.components.ExpressivePullToRefreshBox
import ru.mesh.expressive.ui.theme.*
import ru.mesh.expressive.ui.viewmodel.MeshMainViewModel

@Composable
fun ScheduleScreen(viewModel: MeshMainViewModel) {
    val weekSchedule by viewModel.weekSchedule.collectAsState()
    val scheduleToday by viewModel.scheduleToday.collectAsState()
    val scheduleTomorrow by viewModel.scheduleTomorrow.collectAsState()
    val isCompactSchedule by viewModel.isCompactSchedule.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val currentMonthYear = remember {
        val cal = java.util.Calendar.getInstance()
        val month = java.text.SimpleDateFormat("LLLL", java.util.Locale("ru")).format(cal.time)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale("ru")) else it.toString() }
        val year = cal.get(java.util.Calendar.YEAR)
        "$month $year"
    }

    val daysDates = remember {
        val cal = java.util.Calendar.getInstance()
        cal.firstDayOfWeek = java.util.Calendar.MONDAY
        cal.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        (0 until 6).map {
            val d = sdf.format(cal.time)
            cal.add(java.util.Calendar.DAY_OF_MONTH, 1)
            d
        }
    }

    val daysOfWeek = remember {
        val cal = java.util.Calendar.getInstance()
        cal.firstDayOfWeek = java.util.Calendar.MONDAY
        cal.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
        val dayNames = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб")
        (0 until 6).map { i ->
            val dayNum = cal.get(java.util.Calendar.DAY_OF_MONTH)
            val str = "${dayNames[i]}, $dayNum"
            cal.add(java.util.Calendar.DAY_OF_MONTH, 1)
            str
        }
    }

    val todayDayOfWeekIndex = remember {
        val cal = java.util.Calendar.getInstance()
        val dow = cal.get(java.util.Calendar.DAY_OF_WEEK)
        when (dow) {
            java.util.Calendar.MONDAY -> 0
            java.util.Calendar.TUESDAY -> 1
            java.util.Calendar.WEDNESDAY -> 2
            java.util.Calendar.THURSDAY -> 3
            java.util.Calendar.FRIDAY -> 4
            java.util.Calendar.SATURDAY -> 5
            else -> 0
        }
    }

    val defaultSelectedDayIndex = remember(scheduleToday) {
        val isTomorrow = viewModel.computeSmartDefaultDay(scheduleToday) == ru.mesh.expressive.ui.viewmodel.DashboardDay.TOMORROW
        val cal = java.util.Calendar.getInstance()
        val dow = cal.get(java.util.Calendar.DAY_OF_WEEK)
        if (dow == java.util.Calendar.SUNDAY) {
            0
        } else if (isTomorrow) {
            if (todayDayOfWeekIndex < 5) todayDayOfWeekIndex + 1 else 0
        } else {
            todayDayOfWeekIndex
        }
    }

    var selectedDayIndex by remember { mutableIntStateOf(defaultSelectedDayIndex) }

    LaunchedEffect(defaultSelectedDayIndex) {
        selectedDayIndex = defaultSelectedDayIndex
    }

    ExpressivePullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refreshData() },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
        ) {
            // Week Navigator Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Расписание уроков",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Surface(
                        shape = PillShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = currentMonthYear,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Horizontal Day Selector
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(daysOfWeek) { index, day ->
                        val isSelected = index == selectedDayIndex
                        val isToday = index == todayDayOfWeekIndex
                        val bgColor by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                            label = "dayBg"
                        )
                        val textColor by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            label = "dayText"
                        )

                        Surface(
                            shape = PillShape,
                            color = bgColor,
                            modifier = Modifier
                                .clip(PillShape)
                                .clickable { selectedDayIndex = index }
                        ) {
                            Text(
                                text = if (isToday) "$day (сегодня)" else day,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                                color = textColor
                            )
                        }
                    }
                }
            }

            // Lessons List for Selected Day
            val selectedDateStr = daysDates.getOrNull(selectedDayIndex) ?: ""
            val lessons = weekSchedule[selectedDateStr]
                ?: when (selectedDayIndex) {
                    todayDayOfWeekIndex -> scheduleToday
                    todayDayOfWeekIndex + 1 -> scheduleTomorrow
                    else -> emptyList()
                }

            if (lessons.isEmpty()) {
                item {
                    ru.mesh.expressive.ui.components.ExpressiveEmptyState(
                        title = "Здесь ничего нет",
                        subtitle = "На выбранный день уроков в расписании нет",
                        icon = Icons.Default.CalendarToday
                    )
                }
            } else {
                items(lessons) { lesson ->
                    DetailedLessonCard(lesson = lesson, isCompact = isCompactSchedule)
                }
            }
        }
    }
}

@Composable
fun DetailedLessonCard(
    lesson: LessonScheduleItem,
    isCompact: Boolean = false
) {
    if (isCompact) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (lesson.isOngoing)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (lesson.isOngoing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${lesson.lessonNumber}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (lesson.isOngoing) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${lesson.startTime}–${lesson.endTime}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (lesson.room.isNotBlank() && lesson.room != "—") {
                            Text(
                                text = " • ${lesson.room}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = lesson.subject,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (lesson.mark != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "${lesson.mark}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                if (!lesson.homework.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ДЗ: ${lesson.homework}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    } else {
        // Standard detailed view: room moved to time line, teacher removed
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = ExpressiveCardShape,
            colors = CardDefaults.cardColors(
                containerColor = if (lesson.isOngoing)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = if (lesson.isOngoing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${lesson.lessonNumber}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (lesson.isOngoing) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        val timeAndRoom = buildString {
                            append("${lesson.startTime} – ${lesson.endTime}")
                            if (lesson.room.isNotBlank() && lesson.room != "—") {
                                append(" • ${lesson.room}")
                            }
                        }
                        Text(
                            text = timeAndRoom,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (lesson.mark != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "Оценка: ${lesson.mark}${if (lesson.markWeight > 1.0) " (вес ${lesson.markWeight.toInt()})" else ""}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = lesson.subject,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (lesson.isOngoing) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = PillShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "ИДЕТ СЕЙЧАС",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                if (!lesson.homework.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ДЗ: ",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = lesson.homework,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
