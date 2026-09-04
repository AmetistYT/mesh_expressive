package ru.mesh.expressive.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.mesh.expressive.ui.components.ExpressiveEmptyState
import ru.mesh.expressive.ui.components.ExpressivePullToRefreshBox
import ru.mesh.expressive.ui.theme.*
import ru.mesh.expressive.ui.viewmodel.MeshMainViewModel
import java.util.Locale

@Composable
fun RatingScreen(viewModel: MeshMainViewModel) {
    val rating by viewModel.ratingInfo.collectAsState()
    val academicRanks by viewModel.academicClassRanks.collectAsState()
    val classmates by viewModel.classmates.collectAsState()
    val profile by viewModel.studentProfile.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val subjectSummaries by viewModel.subjectSummaries.collectAsState()
    val showWeightedGpa by viewModel.showWeightedGpa.collectAsState()
    val selectedRatingSubjectId by viewModel.selectedRatingSubjectId.collectAsState()
    val subjectAcademicRanks by viewModel.subjectAcademicRanks.collectAsState()
    val isSubjectRankLoading by viewModel.isSubjectRankLoading.collectAsState()

    val liveGpa = remember(subjectSummaries, showWeightedGpa) {
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
        } else {
            val avgs = subjectSummaries.map { it.getEffectiveAverage(showWeightedGpa) }.filter { it > 0.0 }
            if (avgs.isNotEmpty()) avgs.average() else 0.0
        }
    }

    val currentRanks = if (selectedRatingSubjectId != null) subjectAcademicRanks else academicRanks

    val unratedStudents = androidx.compose.runtime.remember(classmates, currentRanks) {
        val ratedPids = currentRanks.map { it.profileId }.filter { it > 0 }.toSet()
        val ratedGamifs = currentRanks.map { it.gamificationId }.filter { it.isNotBlank() }.toSet()
        classmates.filter { it.profileId !in ratedPids && it.gamificationId !in ratedGamifs && !it.isCurrentUser }
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (academicRanks.isEmpty()) {
            viewModel.refreshData()
        }
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
        ) {
            // 1. Hero Rating Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ExpressiveHeroShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    val myRankItem = currentRanks.find { it.isCurrentUser }
                    val effectiveClassRank = myRankItem?.rankPlace?.takeIf { it > 0 } ?: rating.classRank
                    val effectiveAvg = myRankItem?.averageMark?.takeIf { it > 0.0 } ?: (if (liveGpa > 0.0) liveGpa else (rating.score / 20.0).takeIf { it > 0.0 } ?: profile.gpa)
                    val effectiveRankChange = if (myRankItem != null) {
                        if (myRankItem.rankStatus.equals("up", ignoreCase = true)) 1
                        else if (myRankItem.rankStatus.equals("down", ignoreCase = true)) -1
                        else 0
                    } else {
                        rating.rankChange
                    }
                    val totalRanked = currentRanks.size.takeIf { it > 0 } ?: rating.totalInClass

                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (selectedRatingSubjectId != null) "Рейтинг по предмету" else "Рейтинг учащегося",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = if (effectiveClassRank > 0) "$effectiveClassRank место в классе" else "Рейтинг не рассчитан",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Всего в рейтинге класса: $totalRanked учеников",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(M3Cookie7Shape(7))
                                    .background(StarGold),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }

                        if (effectiveClassRank > 0) {
                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val avgMarkFormatted = if (effectiveAvg > 0.0) String.format(Locale.US, "%.2f", effectiveAvg) else "—"
                                Surface(
                                    shape = PillShape,
                                    color = ScoreGreenContainer
                                ) {
                                    Text(
                                        text = "Средний балл: $avgMarkFormatted",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = ScoreGreen
                                    )
                                }

                                Surface(
                                    shape = PillShape,
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    val statusText = if (effectiveRankChange > 0) "Динамика: ▲" else if (effectiveRankChange < 0) "Динамика: ▼" else "Динамика: —"
                                    val statusColor = if (effectiveRankChange > 0) ScoreGreen else if (effectiveRankChange < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                    Text(
                                        text = statusText,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = statusColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Subject Filter Row
            if (subjectSummaries.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                        Text(
                            text = "Фильтр по предмету",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedRatingSubjectId == null,
                                    onClick = { viewModel.selectRatingSubject(null) },
                                    label = { Text("Все предметы") },
                                    shape = PillShape
                                )
                            }
                            items(subjectSummaries, key = { it.subjectId }) { subj ->
                                FilterChip(
                                    selected = selectedRatingSubjectId == subj.subjectId,
                                    onClick = { viewModel.selectRatingSubject(subj.subjectId) },
                                    label = { Text(subj.subject) },
                                    shape = PillShape
                                )
                            }
                        }
                    }
                }
            }

            // 3. Leaderboard List
            if (isSubjectRankLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(36.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                    }
                }
            } else if (currentRanks.isEmpty()) {
                item {
                    if (selectedRatingSubjectId != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = ExpressiveCardShape,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Пока нет оценок",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "По выбранному предмету в 1 четверти ещё не выставлено оценок ни одному ученику класса.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else if (isRefreshing) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 3.dp
                            )
                        }
                    } else {
                        ExpressiveEmptyState(
                            title = "Рейтинг не сформирован",
                            subtitle = "Сервер МЭШ ещё не рассчитал рейтинг успеваемости для вашего класса",
                            icon = Icons.Default.EmojiEvents
                        )
                    }
                }
            } else {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val activeSubjName = subjectSummaries.find { it.subjectId == selectedRatingSubjectId }?.subject
                        Text(
                            text = if (activeSubjName != null) "Рейтинг: $activeSubjName" else "Места в классе (Общий)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = PillShape,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "${currentRanks.size} учеников",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                itemsIndexed(currentRanks) { index, rankItem ->
                    val isMe = rankItem.isCurrentUser
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
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = when (rankItem.rankPlace) {
                                        1 -> StarGold
                                        2 -> Color(0xFFC0C0C0)
                                        3 -> Color(0xFFCD7F32)
                                        else -> if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${rankItem.rankPlace}",
                                            fontWeight = FontWeight.Bold,
                                            color = if (rankItem.rankPlace <= 3 || isMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = if (isMe) "${profile.lastName} ${profile.firstName} (Вы)" else rankItem.displayName.ifBlank { "Ученик ${index + 1}" },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = if (isMe) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (isMe) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (rankItem.gamificationId.isNotBlank()) {
                                        Text(
                                            text = "ID: ${rankItem.gamificationId}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isMe) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        val statusIcon = when (rankItem.rankStatus.lowercase()) {
                                            "up" -> Icons.Default.ArrowUpward
                                            "down" -> Icons.Default.ArrowDownward
                                            else -> Icons.Default.Remove
                                        }
                                        val statusColor = when (rankItem.rankStatus.lowercase()) {
                                            "up" -> ScoreGreen
                                            "down" -> MaterialTheme.colorScheme.error
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        }
                                        Icon(
                                            imageVector = statusIcon,
                                            contentDescription = null,
                                            tint = statusColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = when (rankItem.rankStatus.lowercase()) {
                                                "up" -> "Поднялся"
                                                "down" -> "Опустился"
                                                else -> "Без изменений"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = statusColor
                                        )
                                    }
                                }
                            }

                            Surface(
                                shape = PillShape,
                                color = if (rankItem.averageMark >= 4.5) ScoreGreenContainer else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                val markFormatted = String.format(Locale.US, "%.2f", rankItem.averageMark)
                                Text(
                                    text = markFormatted,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (rankItem.averageMark >= 4.5) ScoreGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (unratedStudents.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Пока без оценок в четверти",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Surface(
                                shape = PillShape,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "${unratedStudents.size} уч.",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    items(unratedStudents, key = { it.profileId }) { cm ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = ExpressiveCardShape,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.6f))
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
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "—",
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = "${cm.firstName} ${cm.lastName}".trim(),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "ID: ${cm.gamificationId} • Не аттестован(а)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                }

                                Surface(
                                    shape = PillShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ) {
                                    Text(
                                        text = "—",
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
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
