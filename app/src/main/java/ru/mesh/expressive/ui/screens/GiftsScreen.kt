package ru.mesh.expressive.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.mesh.expressive.ui.components.ExpressiveEmptyState
import ru.mesh.expressive.ui.components.ExpressivePullToRefreshBox
import ru.mesh.expressive.ui.components.M3CircularWavyLoader
import ru.mesh.expressive.ui.components.M3WavyProgressIndicator
import ru.mesh.expressive.ui.theme.*
import ru.mesh.expressive.ui.viewmodel.MeshMainViewModel

enum class GiftsTabMode {
    SHOP_AND_QUESTS, RECENT_FEED, RATING_LEADERBOARD
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GiftsScreen(viewModel: MeshMainViewModel) {
    val gamification by viewModel.gamificationProfile.collectAsState()
    val rewards by viewModel.rewards.collectAsState()
    val profileRewards by viewModel.profileRewards.collectAsState()
    val works by viewModel.works.collectAsState()
    val starLeaders by viewModel.starLeaders.collectAsState()
    val isAutoCompleting by viewModel.isAutoCompleting.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    var activeTab by remember { mutableStateOf(GiftsTabMode.SHOP_AND_QUESTS) }
    var autoResultDialogText by remember { mutableStateOf<String?>(null) }
    var claimResultDialogText by remember { mutableStateOf<String?>(null) }
    var showSendGiftDialog by remember { mutableStateOf(false) }
    var targetGamificationId by remember { mutableStateOf("") }
    var giftComment by remember { mutableStateOf("") }
    var isAnonymousGift by remember { mutableStateOf(false) }
    var selectedGiftStars by remember { mutableIntStateOf(50) }
    var selectedRewardId by remember { mutableStateOf("281") }
    var giftSendingInProgress by remember { mutableStateOf(false) }
    var giftResultDialogText by remember { mutableStateOf<String?>(null) }

    val uncompletedWorksCount = works.count { !it.isCompleted }

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
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
        ) {
            // 1. Clean Stars & Level Hero Card
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
                                        text = if (gamification.infiniteStarsOverride) "999 999 999" else "${gamification.coinsCount}",
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = StarGold,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            // Daily Gift Button
                            Button(
                                onClick = {
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

                        Spacer(modifier = Modifier.height(14.dp))

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

            // Tab Selector (3 Tabs: Магазин, Лента подарков, Рейтинг щедрости)
            item {
                PrimaryTabRow(
                    selectedTabIndex = when (activeTab) {
                        GiftsTabMode.SHOP_AND_QUESTS -> 0
                        GiftsTabMode.RECENT_FEED -> 1
                        GiftsTabMode.RATING_LEADERBOARD -> 2
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Tab(
                        selected = activeTab == GiftsTabMode.SHOP_AND_QUESTS,
                        onClick = { activeTab = GiftsTabMode.SHOP_AND_QUESTS },
                        text = { Text("Магазин", fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Default.CardGiftcard, contentDescription = null) }
                    )
                    Tab(
                        selected = activeTab == GiftsTabMode.RECENT_FEED,
                        onClick = { activeTab = GiftsTabMode.RECENT_FEED },
                        text = { Text("Лента", fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Default.VolunteerActivism, contentDescription = null) }
                    )
                    Tab(
                        selected = activeTab == GiftsTabMode.RATING_LEADERBOARD,
                        onClick = { activeTab = GiftsTabMode.RATING_LEADERBOARD },
                        text = { Text("Рейтинг", fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Default.EmojiEvents, contentDescription = null) }
                    )
                }
            }

            if (activeTab == GiftsTabMode.SHOP_AND_QUESTS) {
                // 2. Auto-Submit Action Card
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
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (uncompletedWorksCount > 0)
                                        "Доступно к сдаче: $uncompletedWorksCount заданий"
                                    else
                                        "Заданий к сдаче нет",
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

                // 3. Quests & Tasks List
                item {
                    Text(
                        text = "Текущие задания за звезды",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (works.isEmpty()) {
                    item {
                        ExpressiveEmptyState(
                            title = "Здесь ничего нет",
                            subtitle = "Активных заданий за звезды пока нет",
                            icon = Icons.Default.Star
                        )
                    }
                } else {
                    items(works) { work ->
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
                                    Text(
                                        text = work.description.orEmpty(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
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

                // 4. Rewards Shop
                item {
                    Text(
                        text = "Магазин наград",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (rewards.isEmpty()) {
                    item {
                        ExpressiveEmptyState(
                            title = "Здесь ничего нет",
                            subtitle = "Наград в магазине пока нет",
                            icon = Icons.Default.CardGiftcard
                        )
                    }
                } else {
                    items(rewards) { reward ->
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
                                        text = reward.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = reward.description.orEmpty(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                if (reward.isUnlocked) {
                                    Surface(
                                        shape = PillShape,
                                        color = ScoreGreenContainer
                                    ) {
                                        Text(
                                            text = "Открыто",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = ScoreGreen
                                        )
                                    }
                                } else {
                                    Button(
                                        onClick = { viewModel.unlockReward(reward.id) },
                                        enabled = gamification.coinsCount >= reward.costStars || gamification.infiniteStarsOverride,
                                        shape = PillShape,
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("${reward.costStars} звезд")
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. Send Gift to Friend Action
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = ExpressiveCardShape,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(M3Cookie7Shape(8))
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Подарить другу по ID",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Отправить подарок или звезды однокласснику",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                            Button(
                                onClick = { showSendGiftDialog = true },
                                shape = PillShape,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Подарить")
                            }
                        }
                    }
                }

                // 6. Developer Modifier Switch
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
                                    text = "Разблокирует все награды и визуальные стили профиля",
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
            } else if (activeTab == GiftsTabMode.RECENT_FEED) {
                // RECENT LIVE GIFTS FEED TAB
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
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolunteerActivism,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Живая лента подарков",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "Подарки и поздравления между учениками в реальном времени",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                if (profileRewards.isEmpty()) {
                    item {
                        ExpressiveEmptyState(
                            title = "Здесь ничего нет",
                            subtitle = "История полученных и отправленных подарков пока пуста",
                            icon = Icons.Default.CardGiftcard
                        )
                    }
                } else {
                    items(profileRewards) { gift ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = ExpressiveCardShape,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val senderName = if (gift.from != null) "${gift.from.firstName} ${gift.from.lastName}." else "Анонимный отправитель"
                                    val recipientName = if (gift.to != null) "${gift.to.firstName} ${gift.to.lastName}." else "Получатель"

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "$senderName → $recipientName",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Подарок: ${gift.name}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    if (gift.sendingMode == "PRIVATE") {
                                        Surface(
                                            shape = PillShape,
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = "Анонимно",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                if (!gift.comment.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
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
            } else {
                // RATING LEADERBOARD TAB (Рейтинг щедрости по потраченным звездам)
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
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = StarGold,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Рейтинг щедрости",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "Места в классе формируются по количеству потраченных звезд на подарки",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                if (starLeaders.isEmpty()) {
                    item {
                        ExpressiveEmptyState(
                            title = "Здесь ничего нет",
                            subtitle = "В этом месяце подарки за звезды еще не отправлялись",
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

    if (showSendGiftDialog) {
        AlertDialog(
            onDismissRequest = { if (!giftSendingInProgress) showSendGiftDialog = false },
            icon = { Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = StarGold, modifier = Modifier.size(36.dp)) },
            title = { Text("Подарить подарок", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Выберите подарок из магазина:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    val availableGifts = if (rewards.isNotEmpty()) rewards else listOf(
                        ru.mesh.expressive.data.model.RewardItem(id = "281", title = "Баскетбол", costStars = 20),
                        ru.mesh.expressive.data.model.RewardItem(id = "277", title = "Оливье", costStars = 75),
                        ru.mesh.expressive.data.model.RewardItem(id = "275", title = "Сёмга", costStars = 125),
                        ru.mesh.expressive.data.model.RewardItem(id = "274", title = "67", costStars = 150),
                        ru.mesh.expressive.data.model.RewardItem(id = "266", title = "Капибара", costStars = 200)
                    )
                    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(availableGifts) { giftItem ->
                            val isSelected = selectedRewardId == giftItem.id
                            Surface(
                                shape = PillShape,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable {
                                    selectedRewardId = giftItem.id
                                    selectedGiftStars = giftItem.costStars
                                }
                            ) {
                                Text(
                                    text = "${giftItem.title} (${giftItem.costStars} ★)",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (starLeaders.isNotEmpty()) {
                        Text("Одноклассники:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(starLeaders.filter { !it.isCurrentUser && it.gamificationId.isNotBlank() }) { mate ->
                                val isSelected = targetGamificationId == mate.gamificationId
                                Surface(
                                    shape = PillShape,
                                    color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceContainerHigh,
                                    modifier = Modifier.clickable { targetGamificationId = mate.gamificationId }
                                ) {
                                    Text(
                                        text = mate.name,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Text("ID получателя в МЭШ (Gamification ID):", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = targetGamificationId,
                        onValueChange = { targetGamificationId = it.uppercase() },
                        placeholder = { Text("например: AAC10369") },
                        singleLine = true,
                        shape = PillShape,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = giftComment,
                        onValueChange = { giftComment = it },
                        placeholder = { Text("Пожелание к подарку...") },
                        singleLine = true,
                        shape = PillShape,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Отправить анонимно", style = MaterialTheme.typography.bodySmall)
                        Switch(
                            checked = isAnonymousGift,
                            onCheckedChange = { isAnonymousGift = it }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (targetGamificationId.isNotBlank()) {
                            giftSendingInProgress = true
                            viewModel.sendGift(
                                rewardId = selectedRewardId,
                                costStars = selectedGiftStars,
                                gamificationId = targetGamificationId,
                                comment = giftComment,
                                isAnonymous = isAnonymousGift
                            ) { _, msg ->
                                giftSendingInProgress = false
                                showSendGiftDialog = false
                                giftResultDialogText = msg
                            }
                        }
                    },
                    enabled = !giftSendingInProgress && targetGamificationId.isNotBlank(),
                    shape = PillShape
                ) {
                    Text(if (giftSendingInProgress) "Отправка..." else "Подарить ($selectedGiftStars звезд)")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSendGiftDialog = false },
                    enabled = !giftSendingInProgress
                ) {
                    Text("Отмена")
                }
            }
        )
    }

    if (giftResultDialogText != null) {
        AlertDialog(
            onDismissRequest = { giftResultDialogText = null },
            icon = { Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp)) },
            title = { Text("Результат отправки", fontWeight = FontWeight.Bold) },
            text = { Text(giftResultDialogText!!) },
            confirmButton = {
                Button(onClick = { giftResultDialogText = null }, shape = PillShape) {
                    Text("ОК")
                }
            }
        )
    }
}
