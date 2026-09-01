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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Subject
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
import ru.mesh.expressive.ui.components.ExpressivePullToRefreshBox
import ru.mesh.expressive.ui.components.M3WavyProgressIndicator
import ru.mesh.expressive.ui.components.expressiveBounceClick
import ru.mesh.expressive.ui.theme.*
import ru.mesh.expressive.ui.viewmodel.MeshMainViewModel
import kotlin.math.ceil

enum class MarksViewMode {
    BY_SUBJECT, BY_DATE
}

@Composable
fun MarksScreen(viewModel: MeshMainViewModel) {
    val subjectSummaries by viewModel.subjectSummaries.collectAsState()
    val profile by viewModel.studentProfile.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    var viewMode by remember { mutableStateOf(MarksViewMode.BY_SUBJECT) }
    var showCalculatorDialog by remember { mutableStateOf(false) }
    var selectedSubjectForCalc by remember { mutableStateOf<SubjectSummary?>(null) }

    // Flatten all marks grouped by date
    val allMarksGroupedByDate = remember(subjectSummaries) {
        subjectSummaries.flatMap { it.marks }
            .groupBy { it.date }
            .toList()
            .sortedByDescending { it.first }
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
                                        text = "Успеваемость",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                    Text(
                                        text = if (profile.gpa > 0.0) "Средний балл: ${String.format("%.2f", profile.gpa)}" else "Средний балл: —",
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
                                text = "1-й триместр 2026/2027 учебного года",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // 2. Mode Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (viewMode == MarksViewMode.BY_SUBJECT) "Оценки по предметам" else "Оценки по датам",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = if (viewMode == MarksViewMode.BY_SUBJECT) "${subjectSummaries.size} предметов" else "${allMarksGroupedByDate.sumOf { it.second.size }} оценок",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            onOpenCalculator = {
                                selectedSubjectForCalc = subject
                                showCalculatorDialog = true
                            }
                        )
                    }
                } else {
                    // Grouped by Date (по числу)
                    items(allMarksGroupedByDate) { (date, marks) ->
                        DateMarksGroupCard(date = date, marks = marks)
                    }
                }
            }

            // Floating Bottom Toggle: По предметам / По числу
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp),
                shape = PillShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    MarksModeSegmentButton(
                        title = "По предметам",
                        icon = Icons.Default.Subject,
                        isSelected = viewMode == MarksViewMode.BY_SUBJECT,
                        onClick = { viewMode = MarksViewMode.BY_SUBJECT }
                    )
                    MarksModeSegmentButton(
                        title = "По числу",
                        icon = Icons.Default.CalendarMonth,
                        isSelected = viewMode == MarksViewMode.BY_DATE,
                        onClick = { viewMode = MarksViewMode.BY_DATE }
                    )
                }
            }
        }
    }

    if (showCalculatorDialog && selectedSubjectForCalc != null) {
        val subject = selectedSubjectForCalc!!
        var targetGpa by remember { mutableFloatStateOf(4.5f) }
        val neededFives = calculateNeededFives(subject.averageMark, targetGpa, subject.marks.size)

        AlertDialog(
            onDismissRequest = { showCalculatorDialog = false },
            icon = { Icon(Icons.Default.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp)) },
            title = { Text("Калькулятор баллов", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Предмет: ${subject.subject}", fontWeight = FontWeight.SemiBold)
                    Text(text = "Текущий средний балл: ${String.format("%.2f", subject.averageMark)}")

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Целевой балл: ${String.format("%.1f", targetGpa)}", style = MaterialTheme.typography.bodyMedium)
                    Slider(
                        value = targetGpa,
                        onValueChange = { targetGpa = it },
                        valueRange = 3.5f..5.0f,
                        steps = 2
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = if (neededFives == 0) "Цель уже достигнута!" else "Нужно получить пятерок (с весом 1.0): $neededFives шт.",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
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
private fun MarksModeSegmentButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "modeBtnBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "modeBtnContent"
    )

    Surface(
        shape = PillShape,
        color = bgColor,
        modifier = Modifier
            .clip(PillShape)
            .expressiveBounceClick(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

@Composable
fun DateMarksGroupCard(
    date: String,
    marks: List<MarkItem>
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
                        text = date,
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
    onOpenCalculator: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val markColor = when {
        subjectSummary.averageMark <= 0.0 -> MaterialTheme.colorScheme.onSurfaceVariant
        subjectSummary.averageMark >= 4.5 -> ScoreGreen
        subjectSummary.averageMark >= 3.6 -> ScoreBlue
        subjectSummary.averageMark >= 2.7 -> ScoreOrange
        else -> ScoreRed
    }

    val markBgColor = when {
        subjectSummary.averageMark <= 0.0 -> MaterialTheme.colorScheme.surfaceVariant
        subjectSummary.averageMark >= 4.5 -> ScoreGreenContainer
        subjectSummary.averageMark >= 3.6 -> ScoreBlueContainer
        subjectSummary.averageMark >= 2.7 -> ScoreOrangeContainer
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
                        text = "${subjectSummary.marks.size} оценок в триместре",
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
                            text = if (subjectSummary.averageMark > 0.0) String.format("%.2f", subjectSummary.averageMark) else "—",
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
                                    Text(
                                        text = "Дата: ${mark.date}${if (mark.weight > 1.0) " • Вес ${mark.weight}" else ""}",
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

private fun calculateNeededFives(currentGpa: Double, targetGpa: Float, marksCount: Int): Int {
    if (currentGpa >= targetGpa) return 0
    val count = if (marksCount > 0) marksCount else 1
    val currentSum = currentGpa * count
    val needed = ceil((targetGpa * count - currentSum) / (5.0 - targetGpa)).toInt()
    return if (needed > 0) needed else 0
}
