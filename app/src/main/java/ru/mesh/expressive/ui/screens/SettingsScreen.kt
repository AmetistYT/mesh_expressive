package ru.mesh.expressive.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mesh.expressive.ui.theme.*
import ru.mesh.expressive.ui.viewmodel.MeshMainViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MeshMainViewModel,
    isMonetEnabled: Boolean,
    onToggleMonet: (Boolean) -> Unit,
    onNavigateToAuth: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val profile by viewModel.studentProfile.collectAsState()
    val isCompactSchedule by viewModel.isCompactSchedule.collectAsState()
    val showWeightedGpa by viewModel.showWeightedGpa.collectAsState()
    val hideCompletedQuests by viewModel.hideCompletedQuests.collectAsState()
    val enableSpringPhysics by viewModel.enableSpringPhysics.collectAsState()
    val hideEmptyScheduleDays by viewModel.hideEmptyScheduleDays.collectAsState()
    val gpaTargetScore by viewModel.gpaTargetScore.collectAsState()
    val autoRefreshMinutes by viewModel.autoRefreshMinutes.collectAsState()
    val gamification by viewModel.gamificationProfile.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    var showTokenDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showClassInfoDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
    ) {
        // 1. Account & Profile Card
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(M3Cookie7Shape(7))
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = if (viewModel.isLoggedIn) "${profile.firstName} ${profile.lastName}" else "Демо-режим",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = if (viewModel.isLoggedIn) "${profile.className} • ${profile.schoolName}" else "Локальные демо-данные",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (viewModel.isLoggedIn) {
                                    viewModel.logout()
                                } else {
                                    onNavigateToAuth()
                                }
                            },
                            shape = PillShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(
                                imageVector = if (viewModel.isLoggedIn) Icons.AutoMirrored.Filled.Logout else Icons.AutoMirrored.Filled.Login,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (viewModel.isLoggedIn) "Выйти" else "Войти")
                        }
                    }

                    if (viewModel.isLoggedIn) {
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { showClassInfoDialog = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Данные класса", style = MaterialTheme.typography.labelSmall)
                            }

                            TextButton(
                                onClick = { showTokenDialog = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Токен (JWT)", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }

        // 2. Privacy & Security Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = ExpressiveCardShape,
                colors = CardDefaults.cardColors(containerColor = ScoreGreenContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(M3Cookie7Shape(7))
                            .background(ScoreGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Приватность 100% — Чистый стек",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ScoreGreen
                        )
                        Text(
                            text = "0 трекеров, 0 фонового GPS, прямой TLS к school.mos.ru",
                            style = MaterialTheme.typography.bodySmall,
                            color = ScoreGreen.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }

        // 3. Section: Внешний вид и интерфейс
        item {
            Text(
                text = "Внешний вид и интерфейс",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = ExpressiveCardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Monet Dynamic Theme
                    SettingsToggleRow(
                        icon = Icons.Default.Palette,
                        title = "Monet Dynamic Color",
                        subtitle = "Адаптивные цвета под обои устройства (Material You)",
                        isChecked = isMonetEnabled,
                        onCheckedChange = onToggleMonet
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Spring Physics
                    SettingsToggleRow(
                        icon = Icons.Default.TouchApp,
                        title = "Пружинная физика анимаций",
                        subtitle = "Эффект упругости Material Expressive при нажатиях",
                        isChecked = enableSpringPhysics,
                        onCheckedChange = { viewModel.toggleEnableSpringPhysics(it) }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Compact Schedule
                    SettingsToggleRow(
                        icon = Icons.Default.ViewAgenda,
                        title = "Компактный вид расписания",
                        subtitle = "Уменьшенные карточки уроков для плотного отображения",
                        isChecked = isCompactSchedule,
                        onCheckedChange = { viewModel.toggleCompactSchedule(it) }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Hide Empty Schedule Days
                    SettingsToggleRow(
                        icon = Icons.Default.EventBusy,
                        title = "Скрывать пустые дни",
                        subtitle = "Не показывать карточки дней без запланированных уроков",
                        isChecked = hideEmptyScheduleDays,
                        onCheckedChange = { viewModel.toggleHideEmptyScheduleDays(it) }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Replay Onboarding Tour
                    SettingsActionRow(
                        icon = Icons.Default.School,
                        title = "Обучение и подсказки",
                        subtitle = "Пройти интерактивный гид по функциям заново",
                        onClick = {
                            viewModel.restartOnboardingGuide()
                            viewModel.selectTab(ru.mesh.expressive.ui.viewmodel.MainTab.DASHBOARD)
                            Toast.makeText(context, "Интерактивный гид запущен", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        // 4. Section: Оценки и Успеваемость
        item {
            Text(
                text = "Оценки и Цели успеваемости",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = ExpressiveCardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Weighted GPA
                    SettingsToggleRow(
                        icon = Icons.Default.Calculate,
                        title = "Средневзвешенный балл",
                        subtitle = "Учитывать коэффициенты веса контрольных и экзаменов",
                        isChecked = showWeightedGpa,
                        onCheckedChange = { viewModel.toggleShowWeightedGpa(it) }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Target GPA Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Grade, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(14.dp))
                                Text("Целевой средний балл", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            }
                            Text(
                                text = String.format(java.util.Locale.US, "%.2f", gpaTargetScore),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Slider(
                            value = gpaTargetScore,
                            onValueChange = { viewModel.setGpaTargetScore((it * 100).roundToInt() / 100f) },
                            valueRange = 4.00f..5.00f,
                            steps = 9
                        )
                    }
                }
            }
        }

        // 5. Section: Звезды и Автоматизация
        item {
            Text(
                text = "Звезды и Автоматизация",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = ExpressiveCardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Infinite Stars Override
                    SettingsToggleRow(
                        icon = Icons.Default.Star,
                        title = "Режим бесконечных звезд",
                        subtitle = "Разблокирует все визуальные награды профиля (Dev)",
                        isChecked = gamification.infiniteStarsOverride,
                        onCheckedChange = { viewModel.toggleInfiniteStars(it) }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Hide Completed Quests
                    SettingsToggleRow(
                        icon = Icons.Default.CheckCircleOutline,
                        title = "Скрывать сданные задания",
                        subtitle = "Оставлять в списке только активные задания за звезды",
                        isChecked = hideCompletedQuests,
                        onCheckedChange = { viewModel.toggleHideCompletedQuests(it) }
                    )
                }
            }
        }

        // 6. Section: Синхронизация и Сеть
        item {
            Text(
                text = "Синхронизация данных",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = ExpressiveCardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Sync, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text("Период авто-обновления", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text("Интервал актуализации данных", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Surface(
                            shape = PillShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Text(
                                text = "$autoRefreshMinutes мин",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.refreshData() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = PillShape,
                        enabled = !isRefreshing
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isRefreshing) "Обновление..." else "Принудительно обновить данные сейчас")
                    }
                }
            }
        }

        // 7. Section: Аудит безопасности
        item {
            Text(
                text = "Аудит трекеров и слежки",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = ExpressiveCardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PrivacyAuditRow(
                        title = "AppMetrica / Yandex SDK",
                        subtitle = "Фоновая отправка событий и кликов",
                        status = "Вырезано"
                    )
                    PrivacyAuditRow(
                        title = "Спутник МЭШ (Геотрекинг)",
                        subtitle = "GPS отслеживание местоположения",
                        status = "Вырезано"
                    )
                    PrivacyAuditRow(
                        title = "VK MyTracker & OK Tracer",
                        subtitle = "Рекламные профили и идентификация",
                        status = "Вырезано"
                    )
                    PrivacyAuditRow(
                        title = "Sentry APM & Varioqub",
                        subtitle = "Удаленный сбор дампов и конфигураций",
                        status = "Вырезано"
                    )
                    PrivacyAuditRow(
                        title = "Прямое шифрование TLS",
                        subtitle = "Защищенный трафик только к school.mos.ru",
                        status = "Активно"
                    )
                }
            }
        }

        // 8. Section: Сброс и Очистка
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = ExpressiveCardShape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
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
                            text = "Сброс данных и сессии",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Очищает сохраненный токен, профиль и локальный кэш",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedButton(
                        onClick = { showResetConfirmDialog = true },
                        shape = PillShape,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Очистить")
                    }
                }
            }
        }

        // 9. About App
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = ExpressiveCardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Дневник МЭШ Expressive",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Версия 1.4.0 • Material Design 3 Expressive • Open Source",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Чистый клиент Московской электронной школы. Без трекеров, без рекламы, с прямой работой через боевые API.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = PillShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text = "вайбкод",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }

    if (showClassInfoDialog) {
        AlertDialog(
            onDismissRequest = { showClassInfoDialog = false },
            icon = { Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp)) },
            title = { Text("Параметры класса", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Класс: ${profile.className}", fontWeight = FontWeight.SemiBold)
                    Text("Class Unit ID: ${profile.classUnitId}", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    Text("Class UID: ${profile.classUid}", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    Text("Contingent GUID: ${profile.contingentGuid}", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(profile.classUid))
                        Toast.makeText(context, "Class UID скопирован", Toast.LENGTH_SHORT).show()
                        showClassInfoDialog = false
                    },
                    shape = PillShape
                ) {
                    Text("Скопировать Class UID")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClassInfoDialog = false }) {
                    Text("Закрыть")
                }
            }
        )
    }

    if (showTokenDialog) {
        val token = viewModel.currentAuthToken.orEmpty()
        AlertDialog(
            onDismissRequest = { showTokenDialog = false },
            icon = { Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp)) },
            title = { Text("Активный токен МЭШ", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Используется для прямого доступа к API:", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (token.length > 80) "${token.take(40)}...${token.takeLast(30)}" else token,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(token))
                        Toast.makeText(context, "Токен скопирован в буфер обмена", Toast.LENGTH_SHORT).show()
                        showTokenDialog = false
                    },
                    shape = PillShape
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Скопировать")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTokenDialog = false }) {
                    Text("Закрыть")
                }
            }
        )
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp)) },
            title = { Text("Сбросить данные?", fontWeight = FontWeight.Bold) },
            text = { Text("Вы выйдете из аккаунта, а локальные настройки и кэш будут полностью удалены.") },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirmDialog = false
                        viewModel.clearCache()
                    },
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Да, сбросить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun PrivacyAuditRow(
    title: String,
    subtitle: String,
    status: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }

        Surface(
            shape = PillShape,
            color = if (status == "Вырезано" || status == "Активно") ScoreGreenContainer else MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = status,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (status == "Вырезано" || status == "Активно") ScoreGreen else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
