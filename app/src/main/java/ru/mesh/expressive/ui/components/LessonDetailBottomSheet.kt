package ru.mesh.expressive.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mesh.expressive.data.model.AcademicClassRankItem
import ru.mesh.expressive.data.model.LessonScheduleItem
import ru.mesh.expressive.data.model.MarkDateFormatter
import ru.mesh.expressive.ui.theme.ExpressiveCardShape
import ru.mesh.expressive.ui.theme.PillShape
import ru.mesh.expressive.ui.viewmodel.MeshMainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonDetailBottomSheet(viewModel: MeshMainViewModel) {
    val selectedLesson by viewModel.selectedLessonForDetails.collectAsState()
    val isLoading by viewModel.isLessonDetailsLoading.collectAsState()
    val context = LocalContext.current

    if (selectedLesson != null) {
        val lesson = selectedLesson!!
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeLessonDetails() },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            windowInsets = WindowInsets(0, 0, 0, 0),
            shape = ExpressiveCardShape,
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = lesson.subject,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val timeAndRoom = listOfNotNull(
                            if (lesson.startTime.isNotBlank() && lesson.endTime.isNotBlank()) "${lesson.startTime} – ${lesson.endTime}" else null,
                            lesson.room.takeIf { it.isNotBlank() }
                        ).joinToString(" • ")
                        if (timeAndRoom.isNotBlank()) {
                            Text(
                                text = timeAndRoom,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (lesson.teacherName.isNotBlank() && lesson.teacherName != "Учитель") {
                            Text(
                                text = lesson.teacherName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (lesson.mark != null) {
                        Surface(
                            shape = CircleShape,
                            color = if (lesson.mark >= 4) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier
                                .size(48.dp)
                                .clickable {
                                    viewModel.openMarkDetails(lesson)
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${lesson.mark}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (lesson.mark >= 4) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }

                if (isLoading) {
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Topic Card
                if (!lesson.topic.isNullOrBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = ExpressiveCardShape,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.MenuBook,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Тема урока",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = lesson.topic,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Homework Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (!lesson.homework.isNullOrBlank()) {
                                Modifier.clickable {
                                    viewModel.openHomeworkDetails(
                                        ru.mesh.expressive.data.model.HomeworkItem(
                                            id = lesson.id,
                                            subject = lesson.subject,
                                            subjectId = lesson.subjectId,
                                            description = lesson.homework,
                                            date = lesson.date,
                                            dueDate = lesson.date
                                        )
                                    )
                                }
                            } else Modifier
                        ),
                    shape = ExpressiveCardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.EditNote,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Заданное на этот урок задание",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        if (!lesson.homework.isNullOrBlank()) {
                            Text(
                                text = lesson.homework,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            Text(
                                text = "Домашнее задание на этот урок не задано",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Test Materials Card (Only Tests!)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ExpressiveCardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Quiz,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Прикрепленные тесты",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        if (lesson.testMaterials.isNotEmpty()) {
                            lesson.testMaterials.forEach { testMat ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = testMat.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = testMat.typeName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (!testMat.url.isNullOrBlank()) {
                                        Button(
                                            onClick = {
                                                viewModel.openTestExecution(testMat.url, "${lesson.subject}: ${testMat.title}")
                                            },
                                            shape = PillShape,
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                                        ) {
                                            Text("Выполнить", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "Тестовых заданий к уроку не прикреплено",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // If Mark exists, card to view Mark details and class rank
                if (lesson.mark != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.openMarkDetails(lesson)
                            },
                        shape = ExpressiveCardShape,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Оценка: ${lesson.mark}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                val form = lesson.markControlForm ?: "Ответ на уроке"
                                Text(
                                    text = "Форма: $form • Вес: ${lesson.markWeight}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            FilledTonalButton(
                                onClick = { viewModel.openMarkDetails(lesson) },
                                shape = PillShape
                            ) {
                                Text("Рейтинг и дата", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkDetailBottomSheet(viewModel: MeshMainViewModel) {
    val selectedMarkLesson by viewModel.selectedMarkLesson.collectAsState()
    val ranks by viewModel.markSubjectRanks.collectAsState()
    val isRanksLoading by viewModel.isMarkSubjectRanksLoading.collectAsState()
    val subjectSummaries by viewModel.subjectSummaries.collectAsState()
    val showWeightedGpa by viewModel.showWeightedGpa.collectAsState()

    if (selectedMarkLesson != null) {
        val lesson = selectedMarkLesson!!
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeMarkDetails() },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            windowInsets = WindowInsets(0, 0, 0, 0),
            shape = ExpressiveCardShape,
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp)
            ) {
                // Grade & Subject header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if ((lesson.mark ?: 0) >= 4) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${lesson.mark ?: "—"}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = if ((lesson.mark ?: 0) >= 4) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = lesson.subject,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val form = lesson.markControlForm ?: "Ответ на уроке"
                        Text(
                            text = "Форма контроля: $form",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Вес оценки: ${lesson.markWeight}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Teacher comment if present
                if (!lesson.markComment.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = ExpressiveCardShape,
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Comment,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = lesson.markComment,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Date and time with exact seconds precision and relative day
                Spacer(modifier = Modifier.height(12.dp))
                val formattedDate = ru.mesh.expressive.util.DateUtils.formatRelativeDateTime(lesson.markCreatedAt ?: lesson.date)
                if (formattedDate.isNotBlank()) {
                    Surface(
                        shape = ExpressiveCardShape,
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Дата выставления: $formattedDate",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Class Rank Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Рейтинг класса по предмету",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (ranks.isNotEmpty()) {
                        Surface(
                            shape = PillShape,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "${ranks.size} учеников",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (isRanksLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else if (ranks.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = ExpressiveCardShape,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Text(
                            text = "По этому предмету оценок в четверти ещё не выставлено ни одному ученику класса",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        itemsIndexed(ranks) { _, item ->
                            val isMe = item.isCurrentUser
                            val displayAvg = if (isMe) {
                                val liveSubj = subjectSummaries.find {
                                    (lesson.subjectId > 0 && it.subjectId == lesson.subjectId) ||
                                    (lesson.subject.isNotBlank() && it.subject.equals(lesson.subject, ignoreCase = true))
                                }
                                liveSubj?.getEffectiveAverage(showWeightedGpa)?.takeIf { it > 0.0 } ?: item.averageMark
                            } else {
                                item.averageMark
                            }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = ExpressiveCardShape,
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "${item.rankPlace}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = item.displayName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isMe) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isMe) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (item.gamificationId.isNotBlank()) {
                                                Text(
                                                    text = item.gamificationId,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontSize = 10.sp,
                                                    color = if (isMe) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                )
                                            }
                                        }
                                    }
                                    Surface(
                                        shape = PillShape,
                                        color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = String.format(java.util.Locale.US, "%.2f", displayAvg),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
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
