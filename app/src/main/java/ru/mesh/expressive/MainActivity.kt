package ru.mesh.expressive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import ru.mesh.expressive.data.local.SessionManager
import ru.mesh.expressive.data.repository.MeshRepository
import ru.mesh.expressive.ui.screens.*
import ru.mesh.expressive.ui.theme.*
import ru.mesh.expressive.ui.viewmodel.MainTab
import ru.mesh.expressive.ui.viewmodel.MeshMainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sessionManager = SessionManager(applicationContext)
        val repository = MeshRepository(sessionManager)

        setContent {
            var isMonetEnabled by remember { mutableStateOf(sessionManager.isMonetEnabled) }

            MeshExpressiveTheme(dynamicColor = isMonetEnabled) {
                val mainViewModel: MeshMainViewModel = viewModel(
                    factory = MeshMainViewModel.Factory(repository, sessionManager)
                )
                MeshMainApp(
                    viewModel = mainViewModel,
                    isMonetEnabled = isMonetEnabled,
                    onToggleMonet = {
                        isMonetEnabled = it
                        sessionManager.isMonetEnabled = it
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeshMainApp(
    viewModel: MeshMainViewModel,
    isMonetEnabled: Boolean,
    onToggleMonet: (Boolean) -> Unit
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val profile by viewModel.studentProfile.collectAsState()
    val classmates by viewModel.classmates.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                // Drawer Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val initials = "${profile.firstName.firstOrNull() ?: 'М'}${profile.lastName.firstOrNull() ?: 'Э'}"
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(M3Cookie7Shape(7))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "${profile.firstName} ${profile.lastName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${profile.className} • МЭШ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))

                DrawerMenuItem(
                    icon = Icons.Default.Home,
                    label = "Главная",
                    isSelected = currentTab == MainTab.DASHBOARD,
                    onClick = {
                        viewModel.selectTab(MainTab.DASHBOARD)
                        coroutineScope.launch { drawerState.close() }
                    }
                )
                DrawerMenuItem(
                    icon = Icons.Default.CalendarToday,
                    label = "Расписание",
                    isSelected = currentTab == MainTab.SCHEDULE,
                    onClick = {
                        viewModel.selectTab(MainTab.SCHEDULE)
                        coroutineScope.launch { drawerState.close() }
                    }
                )
                DrawerMenuItem(
                    icon = Icons.Default.EditNote,
                    label = "Задания (ДЗ)",
                    isSelected = currentTab == MainTab.HOMEWORK,
                    onClick = {
                        viewModel.selectTab(MainTab.HOMEWORK)
                        coroutineScope.launch { drawerState.close() }
                    }
                )
                DrawerMenuItem(
                    icon = Icons.Default.Grade,
                    label = "Оценки и средний балл",
                    isSelected = currentTab == MainTab.MARKS,
                    onClick = {
                        viewModel.selectTab(MainTab.MARKS)
                        coroutineScope.launch { drawerState.close() }
                    }
                )
                DrawerMenuItem(
                    icon = Icons.Default.CardGiftcard,
                    label = "Подарки и Звезды",
                    isSelected = currentTab == MainTab.GIFTS,
                    onClick = {
                        viewModel.selectTab(MainTab.GIFTS)
                        coroutineScope.launch { drawerState.close() }
                    }
                )
                DrawerMenuItem(
                    icon = Icons.Default.Groups,
                    label = "Мой класс (${if (classmates.isNotEmpty()) "${classmates.size}" else profile.className})",
                    isSelected = currentTab == MainTab.CLASSMATES,
                    onClick = {
                        viewModel.selectTab(MainTab.CLASSMATES)
                        coroutineScope.launch { drawerState.close() }
                    }
                )
                DrawerMenuItem(
                    icon = Icons.Default.EmojiEvents,
                    label = "Рейтинг успеваемости",
                    isSelected = currentTab == MainTab.RATING,
                    onClick = {
                        viewModel.selectTab(MainTab.RATING)
                        coroutineScope.launch { drawerState.close() }
                    }
                )
                DrawerMenuItem(
                    icon = Icons.Default.CheckCircle,
                    label = "Посещаемость и ЕМИАС",
                    isSelected = currentTab == MainTab.ATTENDANCE,
                    onClick = {
                        viewModel.selectTab(MainTab.ATTENDANCE)
                        coroutineScope.launch { drawerState.close() }
                    }
                )
                DrawerMenuItem(
                    icon = Icons.Default.Restaurant,
                    label = "Питание «Москвёнок»",
                    isSelected = currentTab == MainTab.MEALS,
                    onClick = {
                        viewModel.selectTab(MainTab.MEALS)
                        coroutineScope.launch { drawerState.close() }
                    }
                )
                DrawerMenuItem(
                    icon = Icons.Default.AccountCircle,
                    label = if (viewModel.isLoggedIn) "Сменить аккаунт mos.ru" else "Вход в аккаунт mos.ru",
                    isSelected = currentTab == MainTab.AUTH,
                    onClick = {
                        viewModel.selectTab(MainTab.AUTH)
                        coroutineScope.launch { drawerState.close() }
                    }
                )
                DrawerMenuItem(
                    icon = Icons.Default.Settings,
                    label = "Настройки и Приватность",
                    isSelected = currentTab == MainTab.SETTINGS,
                    onClick = {
                        viewModel.selectTab(MainTab.SETTINGS)
                        coroutineScope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (currentTab != MainTab.AUTH) {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = when (currentTab) {
                                    MainTab.DASHBOARD -> "Дневник МЭШ"
                                    MainTab.SCHEDULE -> "Расписание"
                                    MainTab.HOMEWORK -> "Задания"
                                    MainTab.MARKS -> "Оценки"
                                    MainTab.GIFTS -> "Подарки и Звезды"
                                    MainTab.CLASSMATES -> "Мой класс"
                                    MainTab.RATING -> "Рейтинг"
                                    MainTab.ATTENDANCE -> "Посещаемость"
                                    MainTab.MEALS -> "Москвёнок"
                                    MainTab.SETTINGS -> "Настройки"
                                    MainTab.AUTH -> "Авторизация"
                                },
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Меню")
                            }
                        },
                        actions = {
                            IconButton(onClick = { viewModel.selectTab(MainTab.SETTINGS) }) {
                                if (profile.firstName.isNotBlank() && profile.lastName.isNotBlank()) {
                                    val initials = "${profile.firstName.first()}${profile.lastName.first()}"
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(M3Cookie7Shape(7))
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = initials,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = "Профиль и Настройки",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            },
            bottomBar = {
                if (currentTab != MainTab.AUTH) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        tonalElevation = 8.dp
                    ) {
                        NavigationBarItem(
                            selected = currentTab == MainTab.DASHBOARD,
                            onClick = { viewModel.selectTab(MainTab.DASHBOARD) },
                            icon = { Icon(if (currentTab == MainTab.DASHBOARD) Icons.Filled.Home else Icons.Outlined.Home, contentDescription = "Главная") },
                            label = { Text("Главная", style = MaterialTheme.typography.labelSmall) }
                        )
                        NavigationBarItem(
                            selected = currentTab == MainTab.SCHEDULE,
                            onClick = { viewModel.selectTab(MainTab.SCHEDULE) },
                            icon = { Icon(if (currentTab == MainTab.SCHEDULE) Icons.Filled.CalendarToday else Icons.Outlined.CalendarToday, contentDescription = "Расписание") },
                            label = { Text("Расписание", style = MaterialTheme.typography.labelSmall) }
                        )
                        NavigationBarItem(
                            selected = currentTab == MainTab.HOMEWORK,
                            onClick = { viewModel.selectTab(MainTab.HOMEWORK) },
                            icon = { Icon(if (currentTab == MainTab.HOMEWORK) Icons.Filled.EditNote else Icons.Outlined.EditNote, contentDescription = "Задания") },
                            label = { Text("Задания", style = MaterialTheme.typography.labelSmall) }
                        )
                        NavigationBarItem(
                            selected = currentTab == MainTab.MARKS,
                            onClick = { viewModel.selectTab(MainTab.MARKS) },
                            icon = { Icon(if (currentTab == MainTab.MARKS) Icons.Filled.Grade else Icons.Outlined.Grade, contentDescription = "Оценки") },
                            label = { Text("Оценки", style = MaterialTheme.typography.labelSmall) }
                        )
                        NavigationBarItem(
                            selected = currentTab == MainTab.GIFTS,
                            onClick = { viewModel.selectTab(MainTab.GIFTS) },
                            icon = { Icon(if (currentTab == MainTab.GIFTS) Icons.Filled.CardGiftcard else Icons.Outlined.CardGiftcard, contentDescription = "Подарки") },
                            label = { Text("Подарки", style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (currentTab == MainTab.AUTH) PaddingValues(0.dp) else paddingValues)
            ) {
                Crossfade(targetState = currentTab, label = "TabCrossfade") { tab ->
                    when (tab) {
                        MainTab.DASHBOARD -> DashboardScreen(
                            viewModel = viewModel,
                            onNavigate = { viewModel.selectTab(it) }
                        )
                        MainTab.SCHEDULE -> ScheduleScreen(viewModel = viewModel)
                        MainTab.HOMEWORK -> HomeworkScreen(viewModel = viewModel)
                        MainTab.MARKS -> MarksScreen(viewModel = viewModel)
                        MainTab.GIFTS -> GiftsScreen(viewModel = viewModel)
                        MainTab.CLASSMATES -> ClassmatesScreen(viewModel = viewModel)
                        MainTab.RATING -> RatingScreen(viewModel = viewModel)
                        MainTab.ATTENDANCE -> AttendanceScreen(viewModel = viewModel)
                        MainTab.MEALS -> MealsScreen(viewModel = viewModel)
                        MainTab.AUTH -> AuthScreen(
                            viewModel = viewModel,
                            onAuthSuccess = { viewModel.selectTab(MainTab.DASHBOARD) }
                        )
                        MainTab.SETTINGS -> SettingsScreen(
                            viewModel = viewModel,
                            isMonetEnabled = isMonetEnabled,
                            onToggleMonet = onToggleMonet,
                            onNavigateToAuth = { viewModel.selectTab(MainTab.AUTH) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerMenuItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = { Icon(icon, contentDescription = null) },
        label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
        selected = isSelected,
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
        shape = PillShape,
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}
