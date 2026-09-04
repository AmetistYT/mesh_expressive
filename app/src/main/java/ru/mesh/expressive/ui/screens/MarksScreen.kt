package ru.mesh.expressive.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
    val profile by viewModel.studentProfile.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val viewMode by viewModel.marksViewMode.collectAsState()
    val showWeightedGpa by viewModel.showWeightedGpa.collectAsState()
    var showCalculatorDialog by remember { mutableStateOf(false) }
    var selectedSubjectForCalc by remember { mutableStateOf<SubjectSummary?>(null) }

    // Flatten all marks grouped by date
    val allMarksGroupedByDate = remember(subjectSummaries) {
        subjectSummaries.flatMap { it.marks }
            .groupBy { it.date }
            .toList()
            .sortedByDescending { it.first }
    }

    val gpaLabel = if (showWeightedGpa) "Средневзвешенный балл" else "Средний балл (арифм.)"
    val activeGpa = remember(subjectSummaries, profile.gpa, showWeightedGpa) {
        val allMarks = subjectSummaries.flatMap { it.marks }
        if (allMarks.isNotEmpty()) {
            if (showWeightedGpa) {
                val totalWeight = allMarks.sumOf { it.weight }
                if (totalWeight > 0.0) {
                    allMarks.sumOf { it.value * it.weight } / totalWeight
                } else {
                    allMarks.map { it.value }.average()
                }
            } else {
                allMarks.map { it.value }.average()
            }
        } else if (subjectSummaries.isNotEmpty()) {
            val avgs = subjectSummaries.map { it.getEffectiveAverage(showWeightedGpa) }.filter { it > 0.0 }
            if (avgs.isNotEmpty()) avgs.average() else profile.gpa
        } else profile.gpa
    }

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

                            if (profile.gpa > 0.0) {
                                M3WavyProgressIndicator(
                                    progress = (profile.gpa / 5.0).toFloat().coerceIn(0f, 1f),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    amplitude = 4f,
                                    wavelength = 32f
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            Text(
                                text = "1-я четверть 2026/2027 учебного года",
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
                    item {
                        ru.mesh.expressive.ui.components.ExpressiveEmptyState(
                            title = "Здесь ничего нет",
                            subtitle = "В текущем учебном периоде выставленных оценок нет",
                            icon = Icons.Default.Inbox
                        )
                    }
                } else if (viewMode == MarksViewMode.BY_SUBJECT) {
                    items(subjectSummaries) { subject ->
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
                    // Grouped by Date (по числу)
                    items(allMarksGroupedByDate) { (date, marks) ->
                        DateMarksGroupCard(
                            date = date,
                            marks = marks,
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = mark.subject,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = mark.topic,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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

                            val (bgColor, textColor) = when (mark.value) {
                                5 -> ScoreGreenContainer to ScoreGreen
                                4 -> ScoreBlueContainer to ScoreBlue
                                3 -> ScoreOrangeContainer to ScoreOrange
                                else -> ScoreRedContainer to ScoreRed
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(bgColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${mark.value}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = textColor
                                )
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
                        subjectSummary.marks.forEach { mark ->
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = mark.topic,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    val markTimeStr = if (!mark.createdAt.isNullOrBlank()) ru.mesh.expressive.util.DateUtils.formatRelativeDateTime(mark.createdAt) else ru.mesh.expressive.util.DateUtils.formatRelativeDate(mark.date)
                                    Text(
                                        text = "$markTimeStr${if (mark.weight > 1.0) " • Вес ${mark.weight}" else ""}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                val (bg, fg) = when (mark.value) {
                                    5 -> ScoreGreenContainer to ScoreGreen
                                    4 -> ScoreBlueContainer to ScoreBlue
                                    3 -> ScoreOrangeContainer to ScoreOrange
                                    else -> ScoreRedContainer to ScoreRed
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = bg,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${mark.value}",
                                            fontWeight = FontWeight.Bold,
                                            color = fg
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
