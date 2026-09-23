package ru.mesh.expressive.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.mesh.expressive.data.model.HomeworkItem
import ru.mesh.expressive.ui.components.ExpressiveEmptyState
import ru.mesh.expressive.ui.components.ExpressivePullToRefreshBox
import ru.mesh.expressive.ui.components.M3WavyProgressIndicator
import ru.mesh.expressive.ui.components.expressiveBounceClick
import ru.mesh.expressive.ui.theme.*
import ru.mesh.expressive.ui.viewmodel.MeshMainViewModel

@Composable
fun HomeworkScreen(viewModel: MeshMainViewModel) {
    val homeworkList by viewModel.homeworkList.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    var filter by remember { mutableStateOf("На завтра") }
    val filters = listOf("На завтра", "Все", "Невыполненные", "С тестами ЦДЗ")

    val tomorrowDateFormatted = remember {
        java.text.SimpleDateFormat("d MMMM", java.util.Locale("ru")).format(java.util.Date(System.currentTimeMillis() + 86400000L))
    }

    val nextSchoolDateInfo = remember {
        val cal = java.util.Calendar.getInstance()
        val dow = cal.get(java.util.Calendar.DAY_OF_WEEK)
        val daysToAdd = when (dow) {
            java.util.Calendar.FRIDAY -> 3
            java.util.Calendar.SATURDAY -> 2
            java.util.Calendar.SUNDAY -> 1
            else -> 1
        }
        val targetCal = (cal.clone() as java.util.Calendar).apply {
            add(java.util.Calendar.DAY_OF_MONTH, daysToAdd)
        }
        val sdfIso = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val sdfReadable = java.text.SimpleDateFormat("d MMMM", java.util.Locale("ru"))
        val sdfTomorrow = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(
            java.util.Date(System.currentTimeMillis() + 86400000L)
        )
        Triple(sdfIso.format(targetCal.time), sdfReadable.format(targetCal.time), sdfTomorrow)
    }

    val hideCompletedHomework by viewModel.hideCompletedHomework.collectAsState()

    val filteredList = remember(homeworkList, filter, tomorrowDateFormatted, nextSchoolDateInfo, hideCompletedHomework) {
        val baseList = if (hideCompletedHomework) homeworkList.filter { !it.isDone } else homeworkList
        when (filter) {
            "На завтра" -> {
                val (targetIso, targetReadable, tomorrowIso) = nextSchoolDateInfo
                baseList.filter {
                    // 1. Задано к уроку на целевой день (на завтра / понедельник)
                    (it.rawTargetDate.isNotBlank() && (it.rawTargetDate == targetIso || it.rawTargetDate == tomorrowIso)) ||
                    it.targetDate == "Завтра" || it.targetDate.equals(targetReadable, ignoreCase = true) ||
                    // 2. Либо дедлайн сдачи истекает на целевой день
                    (it.rawDueDate.isNotBlank() && (it.rawDueDate == targetIso || it.rawDueDate == tomorrowIso)) ||
                    it.dueDate == "Завтра" || it.dueDate.equals(targetReadable, ignoreCase = true) ||
                    it.dueDate.equals(tomorrowDateFormatted, ignoreCase = true)
                }
            }
            "Невыполненные" -> baseList.filter { !it.isDone }
            "С тестами ЦДЗ" -> baseList.filter { it.hasDigitalTest }
            else -> baseList
        }
    }

    val totalCount = homeworkList.size

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
            // Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ExpressiveHeroShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Домашние задания",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                val taskText = when {
                                    totalCount == 0 -> "Нет активных заданий"
                                    totalCount % 10 == 1 && totalCount % 100 != 11 -> "$totalCount задание"
                                    totalCount % 10 in 2..4 && totalCount % 100 !in 12..14 -> "$totalCount задания"
                                    else -> "$totalCount заданий"
                                }
                                Text(
                                    text = taskText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }

                            if (totalCount > 0) {
                                Surface(
                                    shape = PillShape,
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = "$totalCount",
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Filters LazyRow
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filters) { f ->
                        val isSelected = f == filter
                        val bgColor by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                            label = "filterBg"
                        )
                        val textColor by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            label = "filterText"
                        )

                        Surface(
                            shape = PillShape,
                            color = bgColor,
                            modifier = Modifier
                                .clip(PillShape)
                                .clickable { filter = f }
                        ) {
                            Text(
                                text = f,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = textColor
                            )
                        }
                    }
                }
            }

            // List of Homework Items
            if (filteredList.isEmpty()) {
                item {
                    val emptySubtitle = when (filter) {
                        "На завтра" -> "На ближайший учебный день заданий не найдено"
                        "Невыполненные" -> "Все домашние задания выполнены!"
                        "С тестами ЦДЗ" -> "Заданий с тестами ЦДЗ не найдено"
                        else -> "Домашние задания отсутствуют"
                    }
                    ExpressiveEmptyState(
                        title = "Здесь ничего нет",
                        subtitle = emptySubtitle,
                        icon = if (filter == "Невыполненные") Icons.Default.CheckCircleOutline else Icons.Default.Inbox
                    )
                }
            } else {
                items(filteredList) { hw ->
                    val hwColor = if (hw.isDone)
                        MaterialTheme.colorScheme.surfaceContainerLowest
                    else
                        MaterialTheme.colorScheme.surfaceContainerLow
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .expressiveBounceClick { viewModel.openHomeworkDetails(hw) },
                        shape = ExpressiveCardShape,
                        colors = CardDefaults.cardColors(
                            containerColor = hwColor
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier
                                        .weight(1f, fill = false)
                                        .padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = hw.subject,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                val hasDifferentDates = hw.rawTargetDate.isNotBlank() && hw.rawDueDate.isNotBlank() && hw.rawTargetDate != hw.rawDueDate
                                val dateBadgeText = if (hasDifferentDates) {
                                    "На ${hw.targetDate} • До ${hw.dueDate}"
                                } else {
                                    val displayDate = if (hw.targetDate.isNotBlank()) hw.targetDate else hw.dueDate
                                    "Срок: $displayDate"
                                }
                                val isUrgent = hw.dueDate == "Завтра" || hw.targetDate == "Завтра" || hw.dueDate == "Сегодня"
                                Surface(
                                    shape = PillShape,
                                    color = if (isUrgent) ScoreOrangeContainer else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = dateBadgeText,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isUrgent) ScoreOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                                        softWrap = false,
                                        maxLines = 1
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = hw.description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (hw.isDone) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (hw.hasDigitalTest) {
                                    Surface(
                                        shape = PillShape,
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.OpenInNew,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Тест в библиотеке МЭШ",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.width(1.dp))
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { viewModel.toggleHomework(hw.id) }
                                ) {
                                    Icon(
                                        imageVector = if (hw.isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (hw.isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (hw.isDone) "Готово" else "Отметить",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (hw.isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
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
