package ru.mesh.expressive.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Subject
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mesh.expressive.data.model.MarkItem
import ru.mesh.expressive.data.model.SubjectSummary
import ru.mesh.expressive.ui.components.ExpressiveChipSegmentedToggle
import ru.mesh.expressive.ui.components.ExpressivePullToRefreshBox
import ru.mesh.expressive.ui.components.M3WavyProgressIndicator
import ru.mesh.expressive.ui.components.expressiveBounceClick
import ru.mesh.expressive.ui.theme.*
import ru.mesh.expressive.ui.viewmodel.MarksViewMode
import ru.mesh.expressive.ui.viewmodel.MeshMainViewModel
import kotlin.math.ceil

@Composable
fun MarksScreen(viewModel: MeshMainViewModel) {
    val subjectSummaries by viewModel.subjectSummaries.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val viewMode by viewModel.marksViewMode.collectAsState()
    val showWeightedGpa by viewModel.showWeightedGpa.collectAsState()
    var showCalculatorDialog by remember { mutableStateOf(false) }
    var selectedSubjectForCalc by remember { mutableStateOf<SubjectSummary?>(null) }

    val periodsSchedules by viewModel.periodsSchedules.collectAsState()

    // Находим актуальную четверть/период из расписания периодов или дефолтное название
    val currentPeriodTitle = remember(periodsSchedules) {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val found = periodsSchedules.firstOrNull { it.date == today && !it.title.isNullOrBlank() }
            ?: periodsSchedules.firstOrNull { !it.title.isNullOrBlank() }
        found?.title ?: "1-я четверть 2026/2027 учебного года"
    }

    // Все оценки, сгруппированные по датам
    val allMarksGroupedByDate = remember(subjectSummaries) {
        subjectSummaries.flatMap { it.marks }
            .groupBy { it.date }
            .toList()
            .sortedByDescending { it.first }
    }

    // Оценки, которые можно исправить в будущем (точки с актуальным сроком сдачи сегодня или позже)
    val fixableMarks = remember(subjectSummaries) {
        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        subjectSummaries.flatMap { it.marks }
            .filter { mark ->
                val hasPoint = mark.isPoint || !mark.pointDate.isNullOrBlank() || mark.rawValue.endsWith(".") || mark.rawValue == "."
                if (!hasPoint) return@filter false
                val pDate = mark.pointDate?.substringBefore("T")?.trim()
                if (pDate.isNullOrBlank()) {
                    true
                } else {
                    pDate >= todayStr
                }
            }
            .distinctBy { if (it.id.isNotBlank()) it.id else "${it.subject}_${it.date}_${it.value}" }
            .sortedWith(
                compareBy<MarkItem> { it.pointDate ?: "9999-99-99" }
                    .thenByDescending { it.date }
            )
    }

    val gpaLabel = if (showWeightedGpa) "Средневзвешенный балл" else "Средний балл (арифм.)"
    val activeGpa by viewModel.activeGpa.collectAsState()

    ExpressivePullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refreshData() },
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
            ) {
                // 1. GPA Hero Card
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
                                        text = gpaLabel,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                    Text(
                                        text = if (activeGpa > 0.0) "Средний балл: ${String.format(java.util.Locale.US, "%.2f", activeGpa)}" else "Средний балл: —",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            if (activeGpa > 0.0) {
                                M3WavyProgressIndicator(
                                    progress = (activeGpa / 5.0).toFloat().coerceIn(0f, 1f),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    amplitude = 4f,
                                    wavelength = 32f
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            Text(
                                text = currentPeriodTitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // 2. Mode Header with Segmented Selector (Предметы / Даты)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = "Оценки",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (viewMode == MarksViewMode.BY_SUBJECT) "${subjectSummaries.size} предметов" else "${allMarksGroupedByDate.sumOf { it.second.size }} оценок",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Expressive Chip Segmented Selector (Чипсины)
                        ExpressiveChipSegmentedToggle(
                            items = listOf(MarksViewMode.BY_SUBJECT, MarksViewMode.BY_DATE),
                            selectedItem = viewMode,
                            onItemSelected = { viewModel.setMarksViewMode(it) },
                            label = { when (it) { MarksViewMode.BY_SUBJECT -> "Предметы"; MarksViewMode.BY_DATE -> "Даты" } },
                            icon = { when (it) { MarksViewMode.BY_SUBJECT -> Icons.AutoMirrored.Filled.Subject; MarksViewMode.BY_DATE -> Icons.Default.CalendarMonth } }
                        )
                    }
                }

                // 3. Content by Mode
                if (subjectSummaries.isEmpty()) {
                    item(key = "marks_empty") {
                        ru.mesh.expressive.ui.components.ExpressiveEmptyState(
                            title = "Здесь ничего нет",
                            subtitle = "В текущем учебном периоде выставленных оценок нет",
                            icon = Icons.Default.Inbox
                        )
                    }
                } else if (viewMode == MarksViewMode.BY_SUBJECT) {
                    items(
                        items = subjectSummaries,
                        key = { "${it.subjectId}_${it.subject}" },
                        contentType = { "subject" }
                    ) { subject ->
                        SubjectMarksCard(
                            subjectSummary = subject,
                            showWeightedGpa = showWeightedGpa,
                            onOpenCalculator = {
                                selectedSubjectForCalc = subject
                                showCalculatorDialog = true
                            },
                            onMarkClick = { viewModel.openMarkDetails(it) }
                        )
                    }
                } else {
                    // 1. Сверху отображаем карточку оценок, которые можно исправить (точки и задолженности)
                    if (fixableMarks.isNotEmpty()) {
                        item(key = "fixable_marks_top") {
                            FixableMarksGroupCard(
                                fixableMarks = fixableMarks,
                                onMarkClick = { viewModel.openMarkDetails(it) }
                            )
                        }
                    }

                    // 2. Grouped by Date (по числу)
                    items(
                        items = allMarksGroupedByDate,
                        key = { it.first },
                        contentType = { "dateGroup" }
                    ) { (date, marks) ->
                        // Внутри дня также поднимаем наверх оценки, требующие исправления
                        val sortedDayMarks = remember(marks) {
                            marks.sortedWith(
                                compareByDescending<MarkItem> { it.isPoint || !it.pointDate.isNullOrBlank() || it.rawValue.endsWith(".") }
                                    .thenByDescending { it.createdAt ?: it.id }
                            )
                        }
                        DateMarksGroupCard(
                            date = date,
                            marks = sortedDayMarks,
                            onMarkClick = { viewModel.openMarkDetails(it) }
                        )
                    }
                }
            }
        }
    }

    if (showCalculatorDialog && selectedSubjectForCalc != null) {
        val subject = selectedSubjectForCalc!!
        var targetGpa by remember { mutableFloatStateOf(4.60f) }
        val neededFives = calculateNeededFives(subject, targetGpa)

        AlertDialog(
            onDismissRequest = { showCalculatorDialog = false },
            icon = { Icon(Icons.Default.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp)) },
            title = { Text("Калькулятор оценок", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Предмет: ${subject.subject}", fontWeight = FontWeight.SemiBold)
                    Text(text = "Текущий средний балл: ${if (subject.averageMark > 0.0) String.format(java.util.Locale.US, "%.2f", subject.averageMark) else "—"}")

                    // Target presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = PillShape,
                            color = if (targetGpa == 3.60f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { targetGpa = 3.60f }
                        ) {
                            Text(
                                text = "На «4» (3.60)",
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (targetGpa == 3.60f) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            shape = PillShape,
                            color = if (targetGpa == 4.60f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { targetGpa = 4.60f }
                        ) {
                            Text(
                                text = "На «5» (4.60)",
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (targetGpa == 4.60f) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Целевой балл:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = String.format(java.util.Locale.US, "%.2f", targetGpa),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Slider(
                        value = targetGpa,
                        onValueChange = { targetGpa = (it * 20).toInt() / 20f },
                        valueRange = 3.50f..4.80f
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = if (subject.averageMark >= targetGpa)
                                    "Цель уже достигнута!"
                                else
                                    "Нужно получить пятёрок (с весом 1.0): $neededFives шт.",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "В МЭШ средний балл от 4.60 округляется в итоговую «5» за четверть",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCalculatorDialog = false },
                    shape = PillShape
                ) {
                    Text("Понятно")
                }
            }
        )
    }
}



@Composable
fun FixableMarksGroupCard(
    fixableMarks: List<MarkItem>,
    onMarkClick: ((MarkItem) -> Unit)? = null
) {
    var isExpanded by remember { mutableStateOf(true) }
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 600f),
        label = "fixable_arrow_rotation"
    )

    val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainerLow
    val itemBg = if (isDark) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surface
    val redContent = if (isDark) Color(0xFFFF7C83) else Color(0xFFC62828)
    val redPillBg = if (isDark) Color(0xFFE94255).copy(alpha = 0.22f) else Color(0xFFFFEBEE)
    val pointBadgeBg = if (isDark) Color(0xFF2C2218) else Color(0xFFFFF3E0)
    val pointBadgeBorder = if (isDark) Color(0xFFFB843D).copy(alpha = 0.35f) else Color(0xFFFFCC80)
    val pointTextColor = if (isDark) Color(0xFFFFB74D) else Color(0xFFE65100)
    val dotColor = if (isDark) Color(0xFFFF5252) else Color(0xFFD32F2F)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ExpressiveCardShape,
        colors = CardDefaults.cardColors(
            containerColor = cardBg
        ),
        border = BorderStroke(1.dp, redContent.copy(alpha = if (isDark) 0.3f else 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PriorityHigh,
                        contentDescription = null,
                        tint = redContent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Можно исправить",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = PillShape,
                        color = redPillBg
                    ) {
                        Text(
                            text = "${fixableMarks.size} ${if (fixableMarks.size == 1) "оценка" else "оценок"}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = redContent
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Свернуть" else "Развернуть",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.rotate(arrowRotation)
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    fixableMarks.forEach { mark ->
                        val formattedPointDate = if (!mark.pointDate.isNullOrBlank()) ru.mesh.expressive.util.DateUtils.formatPointDate(mark.pointDate) else null
                        val markRelativeDate = ru.mesh.expressive.util.DateUtils.formatRelativeDate(mark.date)
                        val markDisplayVal = if (mark.value > 0) "${mark.value}" else "•"

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(itemBg)
                                .then(if (onMarkClick != null) Modifier.clickable { onMarkClick(mark) } else Modifier)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = mark.subject,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                if (mark.topic.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = mark.topic,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = PillShape,
                                        color = redPillBg
                                    ) {
                                        Text(
                                            text = if (!formattedPointDate.isNullOrBlank()) "Точка до $formattedPointDate" else "Точка",
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = redContent
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "за $markRelativeDate",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (mark.weight > 1.0) {
                                    Surface(
                                        shape = PillShape,
                                        color = MaterialTheme.colorScheme.tertiaryContainer,
                                        modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        Text(
                                            text = "Вес ${mark.weight}",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }
                                }

                                Surface(
                                    modifier = Modifier.size(38.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    color = pointBadgeBg,
                                    border = BorderStroke(1.dp, pointBadgeBorder)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = markDisplayVal,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp,
                                            color = pointTextColor
                                        )
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(top = 4.dp, end = 4.dp)
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(dotColor)
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

@Composable
fun DateMarksGroupCard(
    date: String,
    marks: List<MarkItem>,
    onMarkClick: ((MarkItem) -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ExpressiveCardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = ru.mesh.expressive.util.DateUtils.formatRelativeDate(date),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = PillShape,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "${marks.size} ${if (marks.size == 1) "оценка" else "оценки"}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                marks.forEach { mark ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .then(if (onMarkClick != null) Modifier.clickable { onMarkClick(mark) } else Modifier)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val isPoint = mark.isPoint || !mark.pointDate.isNullOrBlank() || mark.rawValue.endsWith(".") || mark.rawValue == "."
                        val formattedPointDate = if (isPoint && !mark.pointDate.isNullOrBlank()) ru.mesh.expressive.util.DateUtils.formatPointDate(mark.pointDate) else null

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = mark.subject,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            if (mark.topic.isNotBlank()) {
                                Text(
                                    text = mark.topic,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isPoint) {
                                Spacer(modifier = Modifier.height(2.dp))
                                val redPillBg = if (isSystemInDarkTheme()) Color(0xFFE94255).copy(alpha = 0.22f) else Color(0xFFFFEBEE)
                                val redContent = if (isSystemInDarkTheme()) Color(0xFFFF7C83) else Color(0xFFC62828)
                                Surface(
                                    shape = PillShape,
                                    color = redPillBg
                                ) {
                                    Text(
                                        text = if (!formattedPointDate.isNullOrBlank()) "Точка до $formattedPointDate" else "Точка",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = redContent
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (mark.weight > 1.0) {
                                Surface(
                                    shape = PillShape,
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = "Вес ${mark.weight}",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }

                            val isDark = isSystemInDarkTheme()
                            val (bgColor, textColor) = when {
                                isPoint -> (if (isDark) Color(0xFF2C2218) else Color(0xFFFFF3E0)) to (if (isDark) Color(0xFFFFB74D) else Color(0xFFE65100))
                                mark.value == 5 -> (if (isDark) ScoreGreenContainerDark else ScoreGreenContainerLight) to (if (isDark) ScoreGreenDark else ScoreGreenLight)
                                mark.value == 4 -> (if (isDark) ScoreBlueContainerDark else ScoreBlueContainerLight) to (if (isDark) ScoreBlueDark else ScoreBlueLight)
                                mark.value == 3 -> (if (isDark) ScoreOrangeContainerDark else ScoreOrangeContainerLight) to (if (isDark) ScoreOrangeDark else ScoreOrangeLight)
                                else -> (if (isDark) ScoreRedContainerDark else ScoreRedContainerLight) to (if (isDark) ScoreRedDark else ScoreRedLight)
                            }

                            val pointBorder = if (isPoint) BorderStroke(1.dp, if (isDark) Color(0xFFFB843D).copy(alpha = 0.35f) else Color(0xFFFFCC80)) else null
                            val markDisplayVal = if (isPoint) (if (mark.value > 0) "${mark.value}" else "•") else "${mark.value}"

                            Surface(
                                modifier = Modifier.size(36.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = bgColor,
                                border = pointBorder
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = markDisplayVal,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = textColor
                                    )
                                    if (isPoint) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(top = 3.dp, end = 3.dp)
                                                .size(5.5.dp)
                                                .clip(CircleShape)
                                                .background(if (isDark) Color(0xFFFF5252) else Color(0xFFD32F2F))
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

@Composable
fun SubjectMarksCard(
    subjectSummary: SubjectSummary,
    showWeightedGpa: Boolean = true,
    onOpenCalculator: () -> Unit,
    onMarkClick: ((MarkItem) -> Unit)? = null
) {
    var isExpanded by remember { mutableStateOf(false) }

    val effAvg = subjectSummary.getEffectiveAverage(showWeightedGpa)

    val markColor = when {
        effAvg <= 0.0 -> MaterialTheme.colorScheme.onSurfaceVariant
        effAvg >= 4.5 -> ScoreGreen
        effAvg >= 3.6 -> ScoreBlue
        effAvg >= 2.7 -> ScoreOrange
        else -> ScoreRed
    }

    val markBgColor = when {
        effAvg <= 0.0 -> MaterialTheme.colorScheme.surfaceVariant
        effAvg >= 4.5 -> ScoreGreenContainer
        effAvg >= 3.6 -> ScoreBlueContainer
        effAvg >= 2.7 -> ScoreOrangeContainer
        else -> ScoreRedContainer
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = ExpressiveCardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subjectSummary.subject,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${subjectSummary.marks.size} оценок в четверти",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = PillShape,
                        color = markBgColor,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = if (effAvg > 0.0) String.format(java.util.Locale.US, "%.2f", effAvg) else "—",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = markColor
                        )
                    }

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "История оценок",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )

                        TextButton(
                            onClick = onOpenCalculator,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Калькулятор")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val sortedSubjectMarks = remember(subjectSummary.marks) {
                            subjectSummary.marks.sortedWith(
                                compareByDescending<MarkItem> { it.isPoint || !it.pointDate.isNullOrBlank() || it.rawValue.endsWith(".") }
                                    .thenByDescending { it.createdAt ?: it.date }
                            )
                        }
                        sortedSubjectMarks.forEach { mark ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainer)
                                    .then(if (onMarkClick != null) Modifier.clickable { onMarkClick(mark) } else Modifier)
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val isPoint = mark.isPoint || !mark.pointDate.isNullOrBlank() || mark.rawValue.endsWith(".") || mark.rawValue == "."
                                val formattedPointDate = if (isPoint && !mark.pointDate.isNullOrBlank()) ru.mesh.expressive.util.DateUtils.formatPointDate(mark.pointDate) else null

                                val isDark = isSystemInDarkTheme()
                                val redPillBg = if (isDark) Color(0xFFE94255).copy(alpha = 0.22f) else Color(0xFFFFEBEE)
                                val redContent = if (isDark) Color(0xFFFF7C83) else Color(0xFFC62828)

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = mark.topic,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    val markTimeStr = if (!mark.createdAt.isNullOrBlank()) ru.mesh.expressive.util.DateUtils.formatRelativeDateTime(mark.createdAt) else ru.mesh.expressive.util.DateUtils.formatRelativeDate(mark.date)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isPoint) {
                                            Surface(
                                                shape = PillShape,
                                                color = redPillBg,
                                                modifier = Modifier.padding(end = 6.dp)
                                            ) {
                                                Text(
                                                    text = if (!formattedPointDate.isNullOrBlank()) "Точка до $formattedPointDate" else "Точка",
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = redContent
                                                )
                                            }
                                        }
                                        Text(
                                            text = "$markTimeStr${if (mark.weight > 1.0) " • Вес ${mark.weight}" else ""}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                val (bg, fg) = when {
                                    isPoint -> (if (isDark) Color(0xFF2C2218) else Color(0xFFFFF3E0)) to (if (isDark) Color(0xFFFFB74D) else Color(0xFFE65100))
                                    mark.value == 5 -> (if (isDark) ScoreGreenContainerDark else ScoreGreenContainerLight) to (if (isDark) ScoreGreenDark else ScoreGreenLight)
                                    mark.value == 4 -> (if (isDark) ScoreBlueContainerDark else ScoreBlueContainerLight) to (if (isDark) ScoreBlueDark else ScoreBlueLight)
                                    mark.value == 3 -> (if (isDark) ScoreOrangeContainerDark else ScoreOrangeContainerLight) to (if (isDark) ScoreOrangeDark else ScoreOrangeLight)
                                    else -> (if (isDark) ScoreRedContainerDark else ScoreRedContainerLight) to (if (isDark) ScoreRedDark else ScoreRedLight)
                                }

                                val pointBorder = if (isPoint) BorderStroke(1.dp, if (isDark) Color(0xFFFB843D).copy(alpha = 0.35f) else Color(0xFFFFCC80)) else null
                                val markDisplayVal = if (isPoint) (if (mark.value > 0) "${mark.value}" else "•") else "${mark.value}"

                                Surface(
                                    modifier = Modifier.size(34.dp),
                                    shape = RoundedCornerShape(9.dp),
                                    color = bg,
                                    border = pointBorder
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = markDisplayVal,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = fg
                                        )
                                        if (isPoint) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(top = 2.5.dp, end = 2.5.dp)
                                                    .size(5.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isDark) Color(0xFFFF5252) else Color(0xFFD32F2F))
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

private fun calculateNeededFives(subject: SubjectSummary, targetGpa: Float): Int {
    if (subject.averageMark >= targetGpa) return 0

    val (weightedSum, totalWeight) = if (subject.marks.isNotEmpty()) {
        val sum = subject.marks.sumOf { it.value * it.weight }
        val weight = subject.marks.sumOf { it.weight }
        sum to weight
    } else {
        (subject.averageMark * 1.0) to 1.0
    }

    val effectiveTarget = targetGpa.coerceAtMost(4.85f).toDouble()
    val denominator = 5.0 - effectiveTarget
    if (denominator <= 0.001) return 0

    val needed = ceil((effectiveTarget * totalWeight - weightedSum) / denominator).toInt()
    return needed.coerceIn(0, 99)
}
