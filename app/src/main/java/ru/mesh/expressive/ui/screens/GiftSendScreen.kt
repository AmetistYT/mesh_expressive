package ru.mesh.expressive.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import coil.request.CachePolicy
import coil.request.ImageRequest
import ru.mesh.expressive.data.model.RewardItem
import ru.mesh.expressive.ui.components.M3CircularWavyLoader
import ru.mesh.expressive.ui.theme.*
import ru.mesh.expressive.ui.viewmodel.MeshMainViewModel

private enum class GiftFilterCategory(val label: String) {
    ALL("Все"),
    BUDGET("До 50"),
    MEDIUM("100-200"),
    PREMIUM("250+")
}

data class RecipientOption(
    val id: String,
    val name: String,
    val subtitle: String,
    val isBirthday: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GiftSendScreen(
    viewModel: MeshMainViewModel
) {
    BackHandler { viewModel.closeGiftSend() }

    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    val rewards by viewModel.rewards.collectAsState()
    val gamification by viewModel.gamificationProfile.collectAsState()
    val starLeaders by viewModel.starLeaders.collectAsState()
    val classmates by viewModel.classmates.collectAsState()
    val studentProfile by viewModel.studentProfile.collectAsState()

    val myGamifId = gamification.gamificationId ?: studentProfile.contingentGuid.ifEmpty { "SELF" }

    val initialGift by viewModel.selectedGiftForSend.collectAsState()
    val initialRecipientId by viewModel.targetRecipientGamifId.collectAsState()

    var selectedGiftId by remember(initialGift, rewards) {
        mutableStateOf(initialGift?.id ?: rewards.firstOrNull()?.id ?: "306")
    }
    var isChangingGift by remember(initialGift) {
        mutableStateOf(initialGift == null)
    }

    var recipientSearchQuery by remember { mutableStateOf("") }
    var selectedRecipientId by remember(initialRecipientId) {
        mutableStateOf(initialRecipientId ?: "")
    }
    var selectedRecipientName by remember { mutableStateOf("") }
    var manualRecipientId by remember { mutableStateOf("") }
    var isManualInputMode by remember { mutableStateOf(false) }

    var customWish by remember { mutableStateOf("") }
    var isAnonymous by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(GiftFilterCategory.ALL) }

    var isSending by remember { mutableStateOf(false) }
    var resultDialogText by remember { mutableStateOf<String?>(null) }
    var isSuccessResult by remember { mutableStateOf(false) }

    val selectedGift = rewards.find { it.id == selectedGiftId } ?: rewards.firstOrNull()
    val giftCost = selectedGift?.costStars ?: 50

    val filteredGifts = remember(rewards, selectedCategory) {
        when (selectedCategory) {
            GiftFilterCategory.ALL -> rewards
            GiftFilterCategory.BUDGET -> rewards.filter { it.costStars <= 50 }
            GiftFilterCategory.MEDIUM -> rewards.filter { it.costStars in 51..200 }
            GiftFilterCategory.PREMIUM -> rewards.filter { it.costStars >= 250 }
        }
    }

    // Comprehensive list of all available classmates
    val allRecipients = remember(classmates, starLeaders) {
        val fromClass = classmates.filter { !it.isCurrentUser }.map {
            RecipientOption(
                id = it.gamificationId,
                name = "${it.firstName} ${it.lastName}".trim(),
                subtitle = if (it.gamificationId.isNotEmpty()) "ID: ${it.gamificationId}" else "",
                isBirthday = it.isBirthdayToday
            )
        }
        val fromLeaders = starLeaders.filter { !it.isCurrentUser && it.gamificationId.isNotBlank() }.map {
            RecipientOption(
                id = it.gamificationId,
                name = it.name,
                subtitle = if (it.className.isNotEmpty()) "${it.className} • ID: ${it.gamificationId}" else "ID: ${it.gamificationId}",
                isBirthday = false
            )
        }
        val combined = (fromClass + fromLeaders).distinctBy { it.id.ifEmpty { it.name } }
        if (combined.isEmpty()) {
            listOf(
                RecipientOption("DEMO1001", "Алексей Смирнов", "ID: DEMO1001"),
                RecipientOption("DEMO1002", "Дарья Васильева", "ID: DEMO1002", isBirthday = true),
                RecipientOption("DEMO1003", "Иван Кузнецов", "ID: DEMO1003"),
                RecipientOption("DEMO1004", "Мария Попова", "ID: DEMO1004"),
                RecipientOption("DEMO1005", "Никита Соколов", "ID: DEMO1005")
            )
        } else combined
    }

    val filteredRecipients = remember(allRecipients, recipientSearchQuery) {
        if (recipientSearchQuery.isBlank()) {
            allRecipients
        } else {
            allRecipients.filter {
                it.name.contains(recipientSearchQuery, ignoreCase = true) ||
                it.id.contains(recipientSearchQuery, ignoreCase = true)
            }
        }
    }

    val activeRecipientId = if (isManualInputMode) manualRecipientId.trim().uppercase() else selectedRecipientId
    val userCoins = gamification.coinsCount
    val isInfinite = gamification.infiniteStarsOverride
    val canAfford = isInfinite || userCoins >= giftCost
    val canSend = canAfford && activeRecipientId.isNotBlank() && !isSending

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    Text(
                        text = "Отправить подарок",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.closeGiftSend() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = PillShape,
                        color = StarGold.copy(alpha = 0.15f),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = StarGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isInfinite) "∞" else "$userCoins",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 12.dp,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = selectedGift?.title ?: "Подарок",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            val statusText = when {
                                !canAfford -> "Не хватает звезд на балансе"
                                activeRecipientId.isBlank() -> "Выберите получателя ниже"
                                else -> "Получатель: ${selectedRecipientName.ifBlank { activeRecipientId }}"
                            }
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (!canAfford) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$giftCost",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = StarGold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = StarGold,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (canSend) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                isSending = true
                                val finalComment = customWish.trim()
                                viewModel.sendGift(
                                    rewardId = selectedGiftId,
                                    costStars = giftCost,
                                    gamificationId = activeRecipientId,
                                    comment = finalComment,
                                    isAnonymous = isAnonymous
                                ) { success, msg ->
                                    isSending = false
                                    isSuccessResult = success
                                    resultDialogText = msg
                                }
                            }
                        },
                        enabled = canSend,
                        shape = PillShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        if (isSending) {
                            M3CircularWavyLoader(size = 24.dp, color = MaterialTheme.colorScheme.onPrimary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Отправка...")
                        } else {
                            Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Подарить ($giftCost",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = StarGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = ")",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
        ) {
            // Section 1: Selected Gift (Prominent Card) or Catalog
            if (selectedGift != null && !isChangingGift) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = ExpressiveCardShape,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
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
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.surface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val animUrl = selectedGift.animationUrl ?: selectedGift.iconName
                                    if (!animUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(animUrl)
                                                .memoryCachePolicy(CachePolicy.ENABLED)
                                                .diskCachePolicy(CachePolicy.ENABLED)
                                                .crossfade(false)
                                                .build(),
                                            contentDescription = selectedGift.title,
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier.size(72.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Surface(
                                        shape = PillShape,
                                        color = MaterialTheme.colorScheme.primary
                                    ) {
                                        Text(
                                            text = "Выбранный подарок",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = selectedGift.title,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${selectedGift.costStars}",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = StarGold
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = StarGold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        if (selectedGift.remainingStock != null) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "• Осталось: ${selectedGift.remainingStock} шт.",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            TextButton(
                                onClick = { isChangingGift = true },
                                shape = PillShape
                            ) {
                                Text("Изменить")
                            }
                        }
                    }
                }
            } else {
                // Changing or choosing gift
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "1. Выберите подарок",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (selectedGift != null) {
                                TextButton(onClick = { isChangingGift = false }) {
                                    Text("Скрыть")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(GiftFilterCategory.entries) { category ->
                                val isCatSelected = selectedCategory == category
                                FilterChip(
                                    selected = isCatSelected,
                                    onClick = { selectedCategory = category },
                                    label = { Text(category.label) },
                                    shape = PillShape,
                                    leadingIcon = if (isCatSelected) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                    } else null
                                )
                            }
                        }
                    }
                }

                val giftPairs = filteredGifts.chunked(2)
                items(giftPairs) { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        pair.forEach { giftItem ->
                            val isSelected = giftItem.id == selectedGiftId
                            Box(modifier = Modifier.weight(1f)) {
                                GiftItemCard(
                                    gift = giftItem,
                                    isSelected = isSelected,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        selectedGiftId = giftItem.id
                                        isChangingGift = false
                                    }
                                )
                            }
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Section 2: Recipient Selection
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ExpressiveCardShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "2. Кому отправить?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Выберите ученика из списка одноклассников или введите ID вручную",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Mode Selector: Classmates vs Manual ID
                        Row(modifier = Modifier.fillMaxWidth()) {
                            FilterChip(
                                selected = !isManualInputMode,
                                onClick = { isManualInputMode = false },
                                label = { Text("Одноклассники (${allRecipients.size})") },
                                shape = PillShape,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            FilterChip(
                                selected = isManualInputMode,
                                onClick = { isManualInputMode = true },
                                label = { Text("Ввести ID") },
                                shape = PillShape,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (!isManualInputMode) {
                            OutlinedTextField(
                                value = recipientSearchQuery,
                                onValueChange = { recipientSearchQuery = it },
                                placeholder = { Text("Поиск по имени или ID...") },
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = null)
                                },
                                trailingIcon = {
                                    if (recipientSearchQuery.isNotEmpty()) {
                                        IconButton(onClick = { recipientSearchQuery = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Очистить")
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = PillShape,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Option: Gifting to oneself
                            val isSelfSelected = selectedRecipientId in listOf("SELF", "MY_SELF", myGamifId)
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelfSelected)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = if (isSelfSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        selectedRecipientId = if (myGamifId.isNotBlank()) myGamifId else "SELF"
                                        selectedRecipientName = "Себе (${studentProfile.firstName.ifBlank { "Мой профиль" }})"
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelfSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = if (isSelfSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onTertiaryContainer,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Text(
                                                text = "Подарить себе",
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${studentProfile.firstName} ${studentProfile.lastName} (Мой профиль)".trim(),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    RadioButton(
                                        selected = isSelfSelected,
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            selectedRecipientId = if (myGamifId.isNotBlank()) myGamifId else "SELF"
                                            selectedRecipientName = "Себе (${studentProfile.firstName.ifBlank { "Мой профиль" }})"
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Scrollable list of ALL classmates
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                filteredRecipients.forEach { mate ->
                                    val isMateSelected = selectedRecipientId.equals(mate.id, ignoreCase = true)
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (isMateSelected)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else
                                            MaterialTheme.colorScheme.surfaceContainerHigh,
                                        border = if (isMateSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                selectedRecipientId = mate.id
                                                selectedRecipientName = mate.name
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(38.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isMateSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = mate.name.take(1),
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isMateSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(12.dp))

                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = mate.name,
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        if (mate.isBirthday) {
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Surface(
                                                                shape = PillShape,
                                                                color = StarGold.copy(alpha = 0.2f)
                                                            ) {
                                                                Text(
                                                                    text = "День рождения",
                                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                                    style = MaterialTheme.typography.labelSmall,
                                                                    color = MaterialTheme.colorScheme.onSurface,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                            }
                                                        }
                                                    }
                                                    if (mate.subtitle.isNotEmpty()) {
                                                        Text(
                                                            text = mate.subtitle,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }

                                            RadioButton(
                                                selected = isMateSelected,
                                                onClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    selectedRecipientId = mate.id
                                                    selectedRecipientName = mate.name
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            OutlinedTextField(
                                value = manualRecipientId,
                                onValueChange = { manualRecipientId = it.uppercase() },
                                label = { Text("Gamification ID получателя") },
                                placeholder = { Text("Например: AAC10369") },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null)
                                },
                                trailingIcon = {
                                    if (manualRecipientId.isNotEmpty()) {
                                        IconButton(onClick = { manualRecipientId = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Очистить")
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = PillShape,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Section 3: Wish Input
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ExpressiveCardShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "3. Пожелание к подарку",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = customWish,
                            onValueChange = { if (it.length <= 150) customWish = it },
                            placeholder = { Text("Напишите пожелание (необязательно)...") },
                            supportingText = {
                                Text(
                                    text = "${customWish.length} / 150",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.End,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            singleLine = false,
                            maxLines = 3,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Section 4: Privacy Settings
            item {
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
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isAnonymous) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = if (isAnonymous) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Отправить анонимно",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Получатель увидит подарок, но не узнает ваше имя",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = isAnonymous,
                            onCheckedChange = { isAnonymous = it }
                        )
                    }
                }
            }
        }
    }

    if (resultDialogText != null) {
        AlertDialog(
            onDismissRequest = {
                val wasSuccess = isSuccessResult
                resultDialogText = null
                if (wasSuccess) {
                    viewModel.closeGiftSend()
                }
            },
            icon = {
                Icon(
                    imageVector = if (isSuccessResult) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (isSuccessResult) ScoreGreen else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(40.dp)
                )
            },
            title = {
                Text(
                    text = if (isSuccessResult) "Подарок отправлен!" else "Не удалось отправить",
                    fontWeight = FontWeight.Bold
                )
            },
            text = { Text(resultDialogText.orEmpty()) },
            confirmButton = {
                Button(
                    onClick = {
                        val wasSuccess = isSuccessResult
                        resultDialogText = null
                        if (wasSuccess) {
                            viewModel.closeGiftSend()
                        }
                    },
                    shape = PillShape
                ) {
                    Text("Отлично")
                }
            }
        )
    }
}

@Composable
fun GiftItemCard(
    gift: RewardItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val animUrl = gift.animationUrl ?: gift.iconName

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .animateContentSize(),
        shape = ExpressiveCardShape,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                if (!animUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(animUrl)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .diskCachePolicy(CachePolicy.ENABLED)
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
                        modifier = Modifier.size(44.dp)
                    )
                }

                if (isSelected) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

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
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}
