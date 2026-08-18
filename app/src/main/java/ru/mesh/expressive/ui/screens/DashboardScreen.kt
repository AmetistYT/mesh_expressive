package ru.mesh.expressive.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import ru.mesh.expressive.data.model.*
import ru.mesh.expressive.ui.components.ExpressivePullToRefreshBox
import ru.mesh.expressive.ui.components.M3WavyProgressIndicator
import ru.mesh.expressive.ui.components.expressiveBounceClick
import ru.mesh.expressive.ui.theme.*
import ru.mesh.expressive.ui.viewmodel.DashboardDay
import ru.mesh.expressive.ui.viewmodel.MainTab
import ru.mesh.expressive.ui.viewmodel.MeshMainViewModel

@Composable
fun DashboardScreen(
    viewModel: MeshMainViewModel,
    onNavigate: (MainTab) -> Unit
) {
    val profile by viewModel.studentProfile.collectAsState()
    val scheduleToday by viewModel.scheduleToday.collectAsState()
    val scheduleTomorrow by viewModel.scheduleTomorrow.collectAsState()
    val homeworkList by viewModel.homeworkList.collectAsState()
    val gamification by viewModel.gamificationProfile.collectAsState()
    val meals by viewModel.mealsBalance.collectAsState()
    val dashboardDay by viewModel.dashboardDay.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    var showGiftClaimDialog by remember { mutableStateOf(false) }

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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
        ) {
            // Demo Mode Banner
            if (!viewModel.isLoggedIn) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigate(MainTab.AUTH) },
                        shape = ExpressiveCardShape,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
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
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Демонстрационный режим",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Text(
                                        text = "Данные вымышлены. Нажмите для входа через mos.ru",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }

            // 1. Expressive Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ExpressiveHeroShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val initials = "${profile.firstName.firstOrNull() ?: 'М'}${profile.lastName.firstOrNull() ?: 'Э'}"
                    // 7-Sided Cookie Avatar
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(M3Cookie7Shape(7))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (profile.firstName.isNotBlank()) "Привет, ${profile.firstName}!" else "Дневник МЭШ",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (profile.className.isNotBlank() || profile.schoolName.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (profile.className.isNotBlank()) {
                                    Surface(
                                        shape = PillShape,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = profile.className,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                if (profile.schoolName.isNotBlank()) {
                                    Text(
                                        text = profile.schoolName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "Московская электронная школа",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // 2. Overview Row: GPA & Moskvionok Balance
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // GPA Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .expressiveBounceClick { onNavigate(MainTab.MARKS) },
                    shape = ExpressiveCardShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ср. балл",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = ScoreGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (profile.gpa > 0.0) String.format(java.util.Locale.getDefault(), "%.2f", profile.gpa) else "—",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (profile.gpa > 0.0) ScoreGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (profile.gpa > 0.0) {
                            M3WavyProgressIndicator(
                                progress = (profile.gpa / 5.0).toFloat().coerceIn(0f, 1f),
                                color = ScoreGreen,
                                amplitude = 3f,
                                wavelength = 28f
                            )
                        } else {
                            Text(
                                text = "Оценок пока нет",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Москвёнок (Питание) Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .expressiveBounceClick { onNavigate(MainTab.MEALS) },
                    shape = ExpressiveCardShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Москвёнок",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = MoskvionokBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = String.format(java.util.Locale.getDefault(), "%.2f ₽", meals.clientBalanceRub),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (meals.hotMealSubscribed) "Горячее питание" else "Буфет",
                            style = MaterialTheme.typography.labelSmall,
                            color = MoskvionokBlue
                        )
                    }
                }
            }
        }

        // 3. Daily Gift & Stars Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .expressiveBounceClick {
                        if (gamification.dailyGiftAvailable) {
                            viewModel.claimDailyGift()
                            showGiftClaimDialog = true
                        } else {
                            onNavigate(MainTab.GIFTS)
                        }
                    },
                shape = ExpressiveCardShape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(M3Cookie7Shape(7))
                                .background(StarGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (gamification.dailyGiftAvailable) "Ежедневный подарок готов!" else "Баланс звезд",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                text = if (gamification.dailyGiftAvailable) "Нажмите, чтобы забрать +150 звезд" else "${gamification.coinsCount} звезд на счете",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (gamification.dailyGiftAvailable) {
                                viewModel.claimDailyGift()
                                showGiftClaimDialog = true
                            } else {
                                onNavigate(MainTab.GIFTS)
                            }
                        },
                        shape = PillShape,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Text(
                            text = if (gamification.dailyGiftAvailable) "Забрать" else "В магазин",
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // 4. Schedule Section with Segmented Toggle (Сегодня / Завтра)
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Расписание",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    // Expressive Pill Segmented Selector
                    Surface(
                        shape = PillShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest
                    ) {
                        Row(modifier = Modifier.padding(4.dp)) {
                            SegmentedPill(
                                title = "Сегодня",
                                isSelected = dashboardDay == DashboardDay.TODAY,
                                onClick = { viewModel.selectDashboardDay(DashboardDay.TODAY) }
                            )
                            SegmentedPill(
                                title = "Завтра",
                                isSelected = dashboardDay == DashboardDay.TOMORROW,
                                onClick = { viewModel.selectDashboardDay(DashboardDay.TOMORROW) }
                            )
                        }
                    }
                }
            }
        }

        val currentSchedule = if (dashboardDay == DashboardDay.TODAY) scheduleToday else scheduleTomorrow
        if (currentSchedule.isEmpty()) {
            item {
                ru.mesh.expressive.ui.components.ExpressiveEmptyState(
                    title = "Здесь ничего нет",
                    subtitle = "На выбранный день уроков в расписании нет",
                    icon = Icons.Default.EventBusy
                )
            }
        } else {
            items(currentSchedule) { lesson ->
                LessonCard(lesson = lesson)
            }
        }

        // 5. Homework for Tomorrow Section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Домашка на завтра",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (homeworkList.isNotEmpty()) {
                    TextButton(onClick = { onNavigate(MainTab.HOMEWORK) }) {
                        Text("Все (${homeworkList.size})")
                    }
                }
            }
        }

        val tomorrowHw = homeworkList.take(3)
        if (tomorrowHw.isEmpty()) {
            item {
                ru.mesh.expressive.ui.components.ExpressiveEmptyState(
                    title = "Здесь ничего нет",
                    subtitle = "Домашних заданий не найдено",
                    icon = Icons.Default.CheckCircleOutline
                )
            }
        } else {
            items(tomorrowHw) { hw ->
                HomeworkCard(
                    homework = hw,
                    onToggle = { viewModel.toggleHomework(hw.id) }
                )
            }
        }
    }

    if (showGiftClaimDialog) {
        AlertDialog(
            onDismissRequest = { showGiftClaimDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = StarGold,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text("Подарок получен!", fontWeight = FontWeight.Bold) },
            text = { Text("+150 звезд начислено на ваш баланс. Используйте их для открытия эксклюзивных наград и стилей в разделе «Подарки».") },
            confirmButton = {
                Button(
                    onClick = { showGiftClaimDialog = false },
                    shape = PillShape
                ) {
                    Text("Отлично")
                }
            }
        )
    }
}
}

@Composable
fun SegmentedPill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "pillBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "pillText"
    )

    Box(
        modifier = Modifier
            .clip(PillShape)
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
fun LessonCard(lesson: LessonScheduleItem) {
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lesson Number & Time
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(52.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (lesson.isOngoing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${lesson.lessonNumber}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (lesson.isOngoing) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = lesson.startTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Subject, Room, Teacher
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = lesson.subject,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
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
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${lesson.room} • ${lesson.teacherName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Mark if graded
            if (lesson.mark != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ScoreGreenContainer
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${lesson.mark}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = ScoreGreen
                        )
                        if (lesson.markWeight > 1.0) {
                            Text(
                                text = "вес ${lesson.markWeight.toInt()}",
                                fontSize = 8.sp,
                                color = ScoreGreen
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeworkCard(
    homework: HomeworkItem,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .expressiveBounceClick(onClick = onToggle),
        shape = ExpressiveCardShape,
        colors = CardDefaults.cardColors(
            containerColor = if (homework.isDone)
                MaterialTheme.colorScheme.surfaceContainerLowest
            else
                MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = homework.isDone,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = homework.subject,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = homework.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (homework.isDone) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (homework.hasDigitalTest) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = PillShape,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "Цифровой тест ЦДЗ",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}
