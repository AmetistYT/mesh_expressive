package ru.mesh.expressive.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import ru.mesh.expressive.ui.components.expressiveBounceClick
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.mesh.expressive.data.model.ClassmateItem
import ru.mesh.expressive.ui.components.ExpressiveEmptyState
import ru.mesh.expressive.ui.components.ExpressivePullToRefreshBox
import ru.mesh.expressive.ui.theme.*
import ru.mesh.expressive.ui.viewmodel.MeshMainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassmatesScreen(viewModel: MeshMainViewModel) {
    val classmates by viewModel.classmates.collectAsState()
    val profile by viewModel.studentProfile.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedClassmateForModal by remember { mutableStateOf<ClassmateItem?>(null) }

    val filteredList = remember(classmates, searchQuery) {
        if (searchQuery.isBlank()) {
            classmates
        } else {
            classmates.filter {
                "${it.firstName} ${it.lastName}".contains(searchQuery, ignoreCase = true) ||
                it.gamificationId.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val birthdayClassmates = remember(classmates) {
        classmates.filter { it.isBirthdayToday }
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
        ) {
            // 1. Class Header Card
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(M3Cookie7Shape(7))
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Groups,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = "Класс ${profile.className}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = profile.schoolName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            Surface(
                                shape = PillShape,
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "${classmates.size} уч.",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }

                        if (classmates.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            val context = androidx.compose.ui.platform.LocalContext.current
                            Button(
                                onClick = {
                                    val sortedList = classmates.sortedWith(
                                        compareBy(String.CASE_INSENSITIVE_ORDER) { 
                                            "${it.lastName} ${it.firstName}".trim() 
                                        }
                                    )
                                    val text = buildString {
                                        appendLine("Список класса ${profile.className} (${sortedList.size} уч.):")
                                        sortedList.forEachIndexed { index, cm ->
                                            val fullName = "${cm.lastName} ${cm.firstName}".trim()
                                            appendLine("${index + 1}. $fullName")
                                        }
                                    }
                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("Список класса", text)
                                    clipboard.setPrimaryClip(clip)
                                    android.widget.Toast.makeText(
                                        context,
                                        "Список класса (${sortedList.size} уч.) скопирован в буфер обмена",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = PillShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Скопировать список класса (А-Я)",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // 2. Birthday Notification if any
            if (birthdayClassmates.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = ExpressiveCardShape,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cake,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Сегодня день рождения!",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = birthdayClassmates.joinToString { "${it.firstName} ${it.lastName}" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Search Field
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Поиск одноклассника...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Очистить")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = PillShape,
                    singleLine = true
                )
            }

            // 4. Classmates List
            if (filteredList.isEmpty()) {
                item {
                    ExpressiveEmptyState(
                        title = "Здесь ничего нет",
                        subtitle = if (searchQuery.isNotEmpty()) "Никого не найдено по запросу '$searchQuery'" else "Список одноклассников загружается...",
                        icon = Icons.Default.PeopleOutline
                    )
                }
            } else {
                items(filteredList, key = { it.profileId }) { classmate ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedClassmateForModal = classmate },
                        shape = ExpressiveCardShape,
                        colors = CardDefaults.cardColors(
                            containerColor = if (classmate.isCurrentUser)
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
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (classmate.isCurrentUser) "${classmate.firstName} ${classmate.lastName} (Вы)" else "${classmate.firstName} ${classmate.lastName}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = if (classmate.isCurrentUser) FontWeight.Bold else FontWeight.SemiBold,
                                            color = if (classmate.isCurrentUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (classmate.isBirthdayToday) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.Cake,
                                                contentDescription = null,
                                                tint = StarGold,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "ID: ${classmate.gamificationId}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (classmate.isCurrentUser) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = if (classmate.isCurrentUser) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }

    if (selectedClassmateForModal != null) {
        ClassmateDetailBottomSheet(
            classmate = selectedClassmateForModal!!,
            onDismiss = { selectedClassmateForModal = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassmateDetailBottomSheet(
    classmate: ClassmateItem,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
            // Header without avatars or numbers
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "${classmate.firstName} ${classmate.lastName}${if (classmate.isCurrentUser) " (Вы)" else ""}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Идентификаторы профиля",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 1. Person ID (GUID)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = ExpressiveCardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
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
                            text = "Person ID (GUID)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = classmate.contingentGuid.ifBlank { "Не указан" },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (classmate.contingentGuid.isNotBlank()) {
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(classmate.contingentGuid))
                                Toast.makeText(context, "Person ID скопирован", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Копировать Person ID",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Gamification ID
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = ExpressiveCardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
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
                            text = "Gamification ID",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = classmate.gamificationId.ifBlank { "Не указан" },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (classmate.gamificationId.isNotBlank()) {
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(classmate.gamificationId))
                                Toast.makeText(context, "Gamification ID скопирован", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Копировать Gamification ID",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            if (classmate.profileId > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                // 3. Profile ID
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ExpressiveCardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
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
                                text = "Profile ID",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${classmate.profileId}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString("${classmate.profileId}"))
                                Toast.makeText(context, "Profile ID скопирован", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Копировать Profile ID",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
