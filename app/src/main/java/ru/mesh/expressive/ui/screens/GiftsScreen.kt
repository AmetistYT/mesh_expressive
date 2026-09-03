package ru.mesh.expressive.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import ru.mesh.expressive.data.model.RewardItem
import ru.mesh.expressive.ui.components.ExpressiveEmptyState
import ru.mesh.expressive.ui.components.ExpressivePullToRefreshBox
import ru.mesh.expressive.ui.components.M3CircularWavyLoader
import ru.mesh.expressive.ui.theme.M3Cookie7Shape
import ru.mesh.expressive.ui.components.M3WavyProgressIndicator
import ru.mesh.expressive.ui.theme.*
import ru.mesh.expressive.ui.viewmodel.MeshMainViewModel

enum class GiftsTabMode(val label: String) {
    SHOWCASE("Витрина"),
    FEED("Лента"),
    LEADERBOARD("Рейтинг")
}

enum class FeedFilterMode(val label: String) {
    ALL_SCHOOL("Вся школа"),
    MINE("Связанное со мной"),
    BY_PERSON("По человеку (PID)")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GiftsScreen(viewModel: MeshMainViewModel) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    val gamification by viewModel.gamificationProfile.collectAsState()
    val rewards by viewModel.rewards.collectAsState()
    val profileRewards by viewModel.profileRewards.collectAsState()
    val works by viewModel.works.collectAsState()
    val hideCompletedQuests by viewModel.hideCompletedQuests.collectAsState()
    val displayedWorks = remember(works, hideCompletedQuests) {
        if (hideCompletedQuests) works.filter { !it.isCompleted } else works
    }
    val starLeaders by viewModel.starLeaders.collectAsState()
    val classmates by viewModel.classmates.collectAsState()
    val studentProfile by viewModel.studentProfile.collectAsState()
    val isAutoCompleting by viewModel.isAutoCompleting.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val customFeed by viewModel.customFeedItems.collectAsState()
    val isFeedLoading by viewModel.isFeedLoading.collectAsState()

    var activeTab by remember { mutableStateOf(GiftsTabMode.SHOWCASE) }
    var autoResultDialogText by remember { mutableStateOf<String?>(null) }
    var claimResultDialogText by remember { mutableStateOf<String?>(null) }

    var feedFilter by remember { mutableStateOf(FeedFilterMode.ALL_SCHOOL) }
    var selectedPersonPid by remember { mutableStateOf<Long?>(null) }
    var customPidText by remember { mutableStateOf("") }

    LaunchedEffect(feedFilter, selectedPersonPid, customPidText) {
        when (feedFilter) {
            FeedFilterMode.ALL_SCHOOL -> {
                viewModel.clearCustomFeed()
            }
            FeedFilterMode.MINE -> {
                viewModel.loadPersonalFeed()
            }
            FeedFilterMode.BY_PERSON -> {
                val manualPid = customPidText.trim().toLongOrNull()
                val targetPid = selectedPersonPid ?: manualPid
                if (targetPid != null && targetPid > 0) {
                    viewModel.loadPersonFeedByPid(targetPid)
                } else {
                    viewModel.clearCustomFeed()
                }
            }
        }
    }

    val uncompletedWorksCount = works.count { !it.isCompleted }

    val myPid = gamification.id ?: studentProfile.personId
    val myFirstName = studentProfile.firstName
    val myLastName = studentProfile.lastName
    val myGamifId = gamification.gamificationId

    val filteredFeed = remember(profileRewards, customFeed, feedFilter, selectedPersonPid, customPidText, myPid, myFirstName, myLastName, myGamifId) {
        when (feedFilter) {
            FeedFilterMode.ALL_SCHOOL -> profileRewards
            FeedFilterMode.MINE -> {
                customFeed ?: profileRewards.filter { item ->
                    val fromMe = (myPid > 0 && item.from?.id == myPid) ||
                            (myFirstName.isNotBlank() && item.from?.firstName.equals(myFirstName, ignoreCase = true)) ||
                            (!myGamifId.isNullOrBlank() && item.from?.gamificationId.equals(myGamifId, ignoreCase = true))
                    val toMe = (myPid > 0 && item.to?.id == myPid) ||
                            (myFirstName.isNotBlank() && item.to?.firstName.equals(myFirstName, ignoreCase = true)) ||
                            (!myGamifId.isNullOrBlank() && item.to?.gamificationId.equals(myGamifId, ignoreCase = true))
                    fromMe || toMe
                }
            }
            FeedFilterMode.BY_PERSON -> {
                if (customFeed != null) {
                    customFeed!!
                } else {
                    val manualPid = customPidText.trim().toLongOrNull()
                    val targetPid = selectedPersonPid ?: manualPid
                    val query = customPidText.trim()

                    if (targetPid != null && targetPid > 0) {
                        profileRewards.filter { item ->
                            item.from?.id == targetPid || item.to?.id == targetPid
                        }
                    } else if (query.isNotBlank()) {
                        profileRewards.filter { item ->
                            item.from?.firstName?.contains(query, ignoreCase = true) == true ||
                            item.from?.lastName?.contains(query, ignoreCase = true) == true ||
                            item.to?.firstName?.contains(query, ignoreCase = true) == true ||
                            item.to?.lastName?.contains(query, ignoreCase = true) == true ||
                            item.from?.gamificationId?.contains(query, ignoreCase = true) == true ||
                            item.to?.gamificationId?.contains(query, ignoreCase = true) == true
                        }
                    } else {
                        emptyList()
                    }
                }
            }
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
        ) {
            // 1. Stars Balance & Level Hero Card
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
                            Column {
                                Text(
                                    text = "Баланс звезд",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (gamification.infiniteStarsOverride) "∞" else "${gamification.coinsCount}",
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = StarGold,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            // Daily Gift Button
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.claimDailyGift { message ->
                                        claimResultDialogText = message
                                    }
                                },
                                enabled = gamification.dailyGiftAvailable,
                                shape = PillShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (gamification.dailyGiftAvailable) StarGold else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (gamification.dailyGiftAvailable) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Icon(
                                    imageVector = if (gamification.dailyGiftAvailable) Icons.Default.CardGiftcard else Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (gamification.dailyGiftAvailable) "+150 звезд" else "Получено")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Уровень ${gamification.level}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${gamification.currentXp} / ${gamification.nextLevelXp} XP",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        M3WavyProgressIndicator(
                            progress = (gamification.currentXp.toFloat() / gamification.nextLevelXp.toFloat()).coerceIn(0f, 1f),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }

            // 2. Tab Selector
            item {
                PrimaryTabRow(
                    selectedTabIndex = activeTab.ordinal,
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    GiftsTabMode.entries.forEach { tab ->
                        val isSelected = activeTab == tab
                        Tab(
                            selected = isSelected,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                activeTab = tab
                            },
                            text = { Text(tab.label, fontWeight = FontWeight.SemiBold) },
                            icon = {
                                Icon(
                                    imageVector = when (tab) {
                                        GiftsTabMode.SHOWCASE -> Icons.Default.CardGiftcard
                                        GiftsTabMode.FEED -> Icons.Default.CardGiftcard
                                        GiftsTabMode.LEADERBOARD -> Icons.Default.EmojiEvents
                                    },
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }
            }

            when (activeTab) {
                GiftsTabMode.SHOWCASE -> {
                    // Send Gift Hero Banner
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = ExpressiveCardShape,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(M3Cookie7Shape(8))
                                            .background(MaterialTheme.colorScheme.secondary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CardGiftcard,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSecondary,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = "Подарить подарок",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Text(
                                            text = "Порадуйте друзей и одноклассников",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.openGiftSend()
                                    },
                                    shape = PillShape,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary,
                                        contentColor = MaterialTheme.colorScheme.onSecondary
                                    )
                                ) {
                                    Text("Подарить")
                                }
                            }
                        }
                    }

                    // Showcase Section Title
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Витрина подарков",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${rewards.size} подарков",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 2-Column Grid of Real Animated Gifts
                    val giftPairs = rewards.chunked(2)
                    items(giftPairs) { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            pair.forEach { gift ->
                                Box(modifier = Modifier.weight(1f)) {
                                    ShowcaseGiftCard(
                                        gift = gift,
                                        onSendClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            viewModel.openGiftSend(gift = gift)
                                        }
                                    )
                                }
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    // Quests & Tasks Section
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Задания за звезды",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Auto-Submit Quests Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Авто-сдача заданий",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Text(
                                        text = if (uncompletedWorksCount > 0)
                                            "Доступно к сдаче: $uncompletedWorksCount заданий"
                                        else
                                            "Все текущие задания сданы",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                                    )
                                }

                                if (isAutoCompleting) {
                                    M3CircularWavyLoader(size = 32.dp, color = MaterialTheme.colorScheme.tertiary)
                                } else {
                                    Button(
                                        onClick = {
                                            viewModel.autoCompleteAllQuests { result ->
                                                autoResultDialogText = result.message
                                            }
                                        },
                                        enabled = uncompletedWorksCount > 0,
                                        shape = PillShape,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.tertiary,
                                            contentColor = MaterialTheme.colorScheme.onTertiary
                                        )
                                    ) {
                                        Text("Сдать все")
                                    }
                                }
                            }
                        }
                    }

                    // List of Quests
                    if (displayedWorks.isEmpty()) {
                        item {
                            ExpressiveEmptyState(
                                title = "Заданий пока нет",
                                subtitle = if (hideCompletedQuests && works.isNotEmpty()) "Все выполненные задания скрыты настройкой" else "Новые задания за звезды появятся при появлении ЦДЗ",
                                icon = Icons.Default.Star
                            )
                        }
                    } else {
                        items(displayedWorks) { work ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = ExpressiveCardShape,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = work.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        if (!work.description.isNullOrBlank()) {
                                            Text(
                                                text = work.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    if (work.isCompleted) {
                                        Surface(
                                            shape = PillShape,
                                            color = ScoreGreenContainer
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = ScoreGreen,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Сдано",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ScoreGreen
                                                )
                                            }
                                        }
                                    } else {
                                        Surface(
                                            shape = PillShape,
                                            color = StarGold.copy(alpha = 0.2f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = StarGold,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "+${work.rewardStars}",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Developer Infinite Stars Toggle
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = ExpressiveCardShape,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Режим бесконечных звезд",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Разблокирует неограниченную отправку подарков",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = gamification.infiniteStarsOverride,
                                    onCheckedChange = { viewModel.toggleInfiniteStars(it) }
                                )
                            }
                        }
                    }
                }

                GiftsTabMode.FEED -> {
                    // 1. Фильтры ленты
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FeedFilterMode.values().forEach { mode ->
                                val isSelected = feedFilter == mode
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        feedFilter = mode
                                    },
                                    label = {
                                        Text(
                                            text = mode.label,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    leadingIcon = {
                                        when (mode) {
                                            FeedFilterMode.ALL_SCHOOL -> Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(18.dp))
                                            FeedFilterMode.MINE -> Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                                            FeedFilterMode.BY_PERSON -> Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(18.dp))
                                        }
                                    },
                                    shape = PillShape,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }
                    }

                    // 2. Выбор человека / одноклассника / ввод PID
                    if (feedFilter == FeedFilterMode.BY_PERSON) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = ExpressiveCardShape,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "Выбор человека (свой класс или PID)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    if (classmates.isNotEmpty()) {
                                        Text(
                                            text = "Одноклассники:",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            FilterChip(
                                                selected = selectedPersonPid == null && customPidText.isBlank(),
                                                onClick = {
                                                    selectedPersonPid = null
                                                    customPidText = ""
                                                },
                                                label = { Text("Все") },
                                                shape = PillShape
                                            )

                                            classmates.forEach { mate ->
                                                val isPicked = (selectedPersonPid != null && selectedPersonPid == mate.profileId) ||
                                                               (customPidText.isNotBlank() && customPidText == mate.profileId.toString())
                                                FilterChip(
                                                    selected = isPicked,
                                                    onClick = {
                                                        if (isPicked) {
                                                            selectedPersonPid = null
                                                            customPidText = ""
                                                        } else {
                                                            selectedPersonPid = mate.profileId
                                                            customPidText = if (mate.profileId > 0) mate.profileId.toString() else mate.firstName
                                                        }
                                                    },
                                                    label = {
                                                        Text("${mate.firstName} ${mate.lastName.take(1)}.")
                                                    },
                                                    shape = PillShape,
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    OutlinedTextField(
                                        value = customPidText,
                                        onValueChange = { newText ->
                                            customPidText = newText
                                            selectedPersonPid = newText.trim().toLongOrNull()
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text("Или введите PID / имя вручную...") },
                                        leadingIcon = {
                                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp))
                                        },
                                        trailingIcon = {
                                            if (customPidText.isNotBlank()) {
                                                IconButton(onClick = {
                                                    customPidText = ""
                                                    selectedPersonPid = null
                                                }) {
                                                    Icon(Icons.Default.Close, contentDescription = "Очистить", modifier = Modifier.size(18.dp))
                                                }
                                            }
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (isFeedLoading) {
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
                    } else if (filteredFeed.isEmpty()) {
                        item {
                            val emptySub = when (feedFilter) {
                                FeedFilterMode.ALL_SCHOOL -> "В школе пока нет активных подарков в ленте"
                                FeedFilterMode.MINE -> "Здесь появятся подарки, отправленные вами или подаренные вам"
                                FeedFilterMode.BY_PERSON -> if (selectedPersonPid == null && customPidText.isBlank()) "Выберите одноклассника выше или введите PID для просмотра истории подарков" else "Для выбранного человека (PID: ${customPidText.ifBlank { "—" }}) подарков в ленте не найдено"
                            }
                            ExpressiveEmptyState(
                                title = "Подарков не найдено",
                                subtitle = emptySub,
                                icon = Icons.Default.CardGiftcard
                            )
                        }
                    } else {
                        items(filteredFeed) { gift ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = ExpressiveCardShape,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val animUrl = gift.animationUrl ?: gift.imageUrl
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (!animUrl.isNullOrBlank()) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(context)
                                                    .data(animUrl)
                                                    .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                                                    .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                                                    .crossfade(false)
                                                    .build(),
                                                contentDescription = gift.name,
                                                contentScale = ContentScale.Fit,
                                                modifier = Modifier.size(46.dp)
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.CardGiftcard,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        val senderName = if (gift.from != null) "${gift.from.firstName} ${gift.from.lastName}." else "Анонимный отправитель"
                                        val recipientName = if (gift.to != null) "${gift.to.firstName} ${gift.to.lastName}." else "Получатель"

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "$senderName → $recipientName",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.weight(1f),
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            if (gift.sendingMode == "PRIVATE") {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Surface(
                                                    shape = PillShape,
                                                    color = MaterialTheme.colorScheme.surfaceVariant
                                                ) {
                                                    Text(
                                                        text = "Анонимно",
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        maxLines = 1,
                                                        softWrap = false
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Подарок: ${gift.name}",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.primary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )

                                            val fromPid = gift.from?.id
                                            val toPid = gift.to?.id
                                            val pidLabel = if (fromPid != null && toPid != null && fromPid > 0 && toPid > 0) {
                                                "PID: $fromPid → $toPid"
                                            } else if (fromPid != null && fromPid > 0) {
                                                "PID: $fromPid"
                                            } else if (toPid != null && toPid > 0) {
                                                "PID: $toPid"
                                            } else null

                                            if (pidLabel != null) {
                                                Text(
                                                    text = pidLabel,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.outline,
                                                    fontSize = 10.sp,
                                                    maxLines = 1
                                                )
                                            }
                                        }

                                        if (!gift.comment.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "«${gift.comment}»",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        if (!gift.purchasedAt.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            val formattedDate = if (gift.purchasedAt.length >= 16) {
                                                gift.purchasedAt.substring(0, 10) + " " + gift.purchasedAt.substring(11, 16)
                                            } else gift.purchasedAt
                                            Text(
                                                text = formattedDate,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                GiftsTabMode.LEADERBOARD -> {
                    // Generosity Leaderboard
                    if (starLeaders.isEmpty()) {
                        item {
                            ExpressiveEmptyState(
                                title = "Рейтинг пуст",
                                subtitle = "В этом месяце подарки еще не отправлялись",
                                icon = Icons.Default.EmojiEvents
                            )
                        }
                    } else {
                        items(starLeaders) { leader ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = ExpressiveCardShape,
                                colors = CardDefaults.cardColors(
                                    containerColor = if (leader.isCurrentUser)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surfaceContainerLow
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
                                            color = when (leader.rank) {
                                                1 -> StarGold
                                                2 -> Color(0xFFC0C0C0)
                                                3 -> Color(0xFFCD7F32)
                                                else -> MaterialTheme.colorScheme.surfaceVariant
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "${leader.rank}",
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (leader.rank <= 3) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Text(
                                                text = leader.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = if (leader.isCurrentUser) FontWeight.Bold else FontWeight.Medium,
                                                color = if (leader.isCurrentUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = if (leader.gamificationId.isNotEmpty()) "ID: ${leader.gamificationId} • ${leader.className}" else leader.className,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "${leader.spentStars}",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = StarGold
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = StarGold,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Text(
                                                text = "потрачено",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        if (!leader.isCurrentUser && leader.gamificationId.isNotBlank()) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            IconButton(
                                                onClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    viewModel.openGiftSend(recipientGamifId = leader.gamificationId)
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CardGiftcard,
                                                    contentDescription = "Подарить",
                                                    tint = MaterialTheme.colorScheme.primary
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

    if (autoResultDialogText != null) {
        AlertDialog(
            onDismissRequest = { autoResultDialogText = null },
            icon = { Icon(Icons.Default.DoneAll, contentDescription = null, tint = ScoreGreen, modifier = Modifier.size(36.dp)) },
            title = { Text("Авто-сдача заданий", fontWeight = FontWeight.Bold) },
            text = { Text(autoResultDialogText!!) },
            confirmButton = {
                Button(onClick = { autoResultDialogText = null }, shape = PillShape) {
                    Text("Готово")
                }
            }
        )
    }

    if (claimResultDialogText != null) {
        AlertDialog(
            onDismissRequest = { claimResultDialogText = null },
            icon = { Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = StarGold, modifier = Modifier.size(36.dp)) },
            title = { Text("Ежедневный подарок", fontWeight = FontWeight.Bold) },
            text = { Text(claimResultDialogText!!) },
            confirmButton = {
                Button(onClick = { claimResultDialogText = null }, shape = PillShape) {
                    Text("Понятно")
                }
            }
        )
    }
}

@Composable
fun ShowcaseGiftCard(
    gift: RewardItem,
    onSendClick: () -> Unit
) {
    val context = LocalContext.current
    val animUrl = gift.animationUrl ?: gift.iconName

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSendClick() },
        shape = ExpressiveCardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                if (!animUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(animUrl)
                            .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                            .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                            .crossfade(false)
                            .build(),
                        contentDescription = gift.title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(80.dp)
                            .padding(4.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = gift.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${gift.costStars}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = StarGold
                )
                Spacer(modifier = Modifier.width(3.dp))
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = StarGold,
                    modifier = Modifier.size(15.dp)
                )
            }

            if (gift.remainingStock != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${gift.remainingStock} шт.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onSendClick,
                shape = PillShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text("Подарить", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}
