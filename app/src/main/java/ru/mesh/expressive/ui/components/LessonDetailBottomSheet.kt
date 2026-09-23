package ru.mesh.expressive.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mesh.expressive.data.model.AcademicClassRankItem
import ru.mesh.expressive.data.model.LessonScheduleItem
import ru.mesh.expressive.data.model.MarkDateFormatter
import ru.mesh.expressive.ui.theme.*
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

                    val hasMarkOrPoint = lesson.mark != null || lesson.isPoint || !lesson.rawMark.isNullOrBlank()
                    if (hasMarkOrPoint) {
                        val isPoint = lesson.isPoint || lesson.rawMark?.endsWith(".") == true || lesson.rawMark == "."
                        val markText = when {
                            !lesson.rawMark.isNullOrBlank() -> lesson.rawMark!!
                            lesson.isPoint -> if (lesson.mark != null) "${lesson.mark}." else "•"
                            lesson.mark != null -> "${lesson.mark}"
                            else -> "•"
                        }
                        val (markBg, markFg) = when {
                            isPoint -> ScoreOrangeContainer to ScoreOrange
                            (lesson.mark ?: 0) >= 4 -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
                        }

                        Surface(
                            shape = CircleShape,
                            color = markBg,
                            modifier = Modifier
                                .size(48.dp)
                                .clickable {
                                    viewModel.openMarkDetails(lesson)
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = markText,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = markFg
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

                // Attached Files & Materials Card (не ЦДЗ)
                if (lesson.fileMaterials.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = ExpressiveCardShape,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.AttachFile,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Прикрепленные файлы к уроку",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            lesson.fileMaterials.forEach { fileMat ->
                                val fileUrl = fileMat.url
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
                                        .then(
                                            if (!fileUrl.isNullOrBlank()) {
                                                Modifier.clickable {
                                                    viewModel.downloadAttachment(context, fileUrl, fileMat.title, openAfterDownload = true)
                                                }
                                            } else Modifier
                                        )
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Description,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = fileMat.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = fileMat.typeName,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    if (!fileUrl.isNullOrBlank()) {
                                        IconButton(
                                            onClick = {
                                                viewModel.downloadAttachment(context, fileUrl, fileMat.title, openAfterDownload = false)
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Download,
                                                contentDescription = "Скачать",
                                                modifier = Modifier.size(20.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Test Materials Card (Only Tests!)
                if (lesson.testMaterials.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
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
                                    text = "Прикрепленные тесты (ЦДЗ)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
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
                        }
                    }
                }

                // If Mark exists or is point, card to view Mark details and class statistics
                val hasMarkCard = lesson.mark != null || lesson.isPoint || !lesson.rawMark.isNullOrBlank()
                if (hasMarkCard) {
                    Spacer(modifier = Modifier.height(12.dp))
                    val isPt = lesson.isPoint || lesson.rawMark?.endsWith(".") == true || lesson.rawMark == "."
                    val displayMark = when {
                        !lesson.rawMark.isNullOrBlank() -> lesson.rawMark!!
                        isPt -> if (lesson.mark != null) "${lesson.mark}." else "•"
                        lesson.mark != null -> "${lesson.mark}"
                        else -> "•"
                    }
                    val formattedPtDate = if (isPt && !lesson.pointDate.isNullOrBlank()) {
                        ru.mesh.expressive.util.DateUtils.formatPointDate(lesson.pointDate)
                    } else null

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.openMarkDetails(lesson)
                            },
                        shape = ExpressiveCardShape,
                        colors = CardDefaults.cardColors(
                            containerColor = if (isPt) ScoreOrangeContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isPt) "Точка: $displayMark" else "Оценка: $displayMark",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPt) ScoreOrange else MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    if (isPt) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = PillShape,
                                            color = ScoreOrange
                                        ) {
                                            Text(
                                                text = "Временная",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                                val form = lesson.markControlForm ?: "Ответ на уроке"
                                Text(
                                    text = "Форма: $form • Вес: ${lesson.markWeight}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (!formattedPtDate.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Выставляется до $formattedPtDate",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ScoreOrange
                                    )
                                }
                            }
                            FilledTonalButton(
                                onClick = { viewModel.openMarkDetails(lesson) },
                                shape = PillShape
                            ) {
                                Text("Статистика", style = MaterialTheme.typography.labelSmall)
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
    val detailedMark by viewModel.selectedMarkDetailed.collectAsState()
    val isDetailedLoading by viewModel.isMarkDetailedLoading.collectAsState()
    val ranks by viewModel.markSubjectRanks.collectAsState()
    val isRanksLoading by viewModel.isMarkSubjectRanksLoading.collectAsState()

    if (selectedMarkLesson != null) {
        val lesson = selectedMarkLesson!!
        val isPoint = lesson.isPoint || (detailedMark?.isPoint == true) || lesson.rawMark?.endsWith(".") == true || (detailedMark?.value?.endsWith(".") == true) || lesson.rawMark == "."
        val pointDateRaw = detailedMark?.pointDate ?: lesson.pointDate
        val formattedPointDate = ru.mesh.expressive.util.DateUtils.formatPointDate(pointDateRaw)

        val rawVal = detailedMark?.value ?: lesson.rawMark
        val displayVal = when {
            !rawVal.isNullOrBlank() -> rawVal
            isPoint -> if (lesson.mark != null) "${lesson.mark}." else "•"
            lesson.mark != null -> "${lesson.mark}"
            else -> "—"
        }

        val myMarkNumeric = lesson.mark ?: displayVal.removeSuffix(".").toIntOrNull()

        val (markBadgeBg, markBadgeFg) = when {
            isPoint -> ScoreOrangeContainer to ScoreOrange
            (myMarkNumeric ?: 0) >= 4 -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
            else -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        }

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
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp)
            ) {
                // Grade & Subject header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = markBadgeBg,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = displayVal,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = markBadgeFg
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = lesson.subject,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (isPoint) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = PillShape,
                                    color = ScoreOrange
                                ) {
                                    Text(
                                        text = "Точка",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                        val form = detailedMark?.controlFormName ?: lesson.markControlForm ?: "Ответ на уроке"
                        Text(
                            text = "Форма контроля: $form",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val weightVal = detailedMark?.weight ?: lesson.markWeight
                        Text(
                            text = "Вес оценки: $weightVal",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // If it is a point, display info card with point expiration date
                if (isPoint) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = ExpressiveCardShape,
                        colors = CardDefaults.cardColors(
                            containerColor = ScoreOrangeContainer.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = ScoreOrange,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (formattedPointDate.isNotBlank()) "Будет выставлена: $formattedPointDate" else "Временная отметка (точка)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ScoreOrange
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Пока оценка не выставлена окончательно, есть возможность сдать работу или улучшить оценку.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Teacher comment if present
                val commentText = detailedMark?.comment ?: lesson.markComment
                if (!commentText.isNullOrBlank()) {
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
                                text = commentText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Date and time
                Spacer(modifier = Modifier.height(12.dp))
                val rawCreatedAt = detailedMark?.createdAt ?: lesson.markCreatedAt ?: lesson.date
                val formattedDate = ru.mesh.expressive.util.DateUtils.formatRelativeDateTime(rawCreatedAt)
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

                Spacer(modifier = Modifier.height(18.dp))

                // Class Results Section (Оценки класса за эту работу)
                val distributions = detailedMark?.classResults?.marksDistributions.orEmpty()
                val totalStudents = detailedMark?.classResults?.totalStudents ?: distributions.sumOf { it.numberOfStudents ?: 0 }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (distributions.isNotEmpty()) "Оценки класса за работу" else "Рейтинг класса за урок",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (distributions.isNotEmpty() && totalStudents > 0) {
                        Surface(
                            shape = PillShape,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "$totalStudents ${if (totalStudents % 10 == 1 && totalStudents % 100 != 11) "оценка" else "оценок"}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else if (ranks.isNotEmpty()) {
                        Surface(
                            shape = PillShape,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "${ranks.size} уч.",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (isDetailedLoading || isRanksLoading) {
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
                } else if (distributions.isNotEmpty()) {
                    // Official class mark distribution
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val sortedDist = distributions.sortedByDescending { it.markValue?.five ?: 0 }
                        sortedDist.forEach { dist ->
                            val markVal = dist.markValue?.five ?: 0
                            val count = dist.numberOfStudents ?: 0
                            val pct = (dist.percentageOfStudents ?: 0).toFloat()
                            val isMyGrade = myMarkNumeric != null && myMarkNumeric == markVal

                            val (gradeBg, gradeFg) = when (markVal) {
                                5 -> ScoreGreenContainer to ScoreGreen
                                4 -> ScoreBlueContainer to ScoreBlue
                                3 -> ScoreOrangeContainer to ScoreOrange
                                else -> ScoreRedContainer to ScoreRed
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = ExpressiveCardShape,
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isMyGrade) gradeBg.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = CircleShape,
                                                color = gradeBg,
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "$markVal",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = gradeFg
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "$count ${if (count % 10 == 1 && count % 100 != 11) "ученик" else if (count % 10 in 2..4 && (count % 100 !in 12..14)) "ученика" else "учеников"}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            if (isMyGrade) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Surface(
                                                    shape = PillShape,
                                                    color = gradeFg
                                                ) {
                                                    Text(
                                                        text = "Ваша оценка",
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }

                                        Text(
                                            text = "${pct.toInt()}%",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = gradeFg
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    LinearProgressIndicator(
                                        progress = (pct / 100f).coerceIn(0f, 1f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = gradeFg,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                            }
                        }
                    }
                } else if (ranks.isNotEmpty()) {
                    // Fallback to classmates academic ranking without fake lesson marks
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ranks.forEach { item ->
                            val isMe = item.isCurrentUser
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
                                    if (item.averageMark > 0.0) {
                                        Surface(
                                            shape = PillShape,
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = "ср. ${String.format(java.util.Locale.US, "%.2f", item.averageMark)}",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = ExpressiveCardShape,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Text(
                            text = "Оценки других учеников класса за эту работу ещё не выставлены",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
