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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import ru.mesh.expressive.data.local.SessionManager
import ru.mesh.expressive.data.repository.MeshRepository
import ru.mesh.expressive.ui.components.expressiveBounceClick
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
    val showOnboardingGuide by viewModel.showOnboardingGuide.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val profile by viewModel.studentProfile.collectAsState()
    val classmates by viewModel.classmates.collectAsState()

    var menuTargetRect by remember { mutableStateOf<Rect?>(null) }
    var profileTargetRect by remember { mutableStateOf<Rect?>(null) }
    var dockTargetRect by remember { mutableStateOf<Rect?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
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
                            IconButton(
                                onClick = { coroutineScope.launch { drawerState.open() } },
                                modifier = Modifier.onGloballyPositioned { menuTargetRect = it.boundsInRoot() }
                            ) {
                                Icon(Icons.Default.Menu, contentDescription = "Меню")
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = { viewModel.selectTab(MainTab.SETTINGS) },
                                modifier = Modifier.onGloballyPositioned { profileTargetRect = it.boundsInRoot() }
                            ) {
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
            containerColor = MaterialTheme.colorScheme.surface
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = if (currentTab == MainTab.AUTH) 0.dp else paddingValues.calculateTopPadding())
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

                if (currentTab != MainTab.AUTH) {
                    ExpressiveFloatingDock(
                        currentTab = currentTab,
                        onTabSelected = { viewModel.selectTab(it) },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .onGloballyPositioned { dockTargetRect = it.boundsInRoot() }
                    )
                }

                }
            }
        }

        if (showOnboardingGuide && currentTab != MainTab.AUTH) {
            ru.mesh.expressive.ui.components.OnboardingGuideOverlay(
                menuRect = menuTargetRect,
                profileRect = profileTargetRect,
                dockRect = dockTargetRect,
                onDismiss = { viewModel.completeOnboardingGuide() }
            )
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

@Composable
fun ExpressiveFloatingDock(
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = PillShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f),
            shadowElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tabs = listOf(
                    Triple(MainTab.DASHBOARD, Icons.Filled.Home to Icons.Outlined.Home, "Главная"),
                    Triple(MainTab.SCHEDULE, Icons.Filled.CalendarToday to Icons.Outlined.CalendarToday, "Расписание"),
                    Triple(MainTab.HOMEWORK, Icons.Filled.EditNote to Icons.Outlined.EditNote, "Задания"),
                    Triple(MainTab.MARKS, Icons.Filled.Grade to Icons.Outlined.Grade, "Оценки"),
                    Triple(MainTab.GIFTS, Icons.Filled.CardGiftcard to Icons.Outlined.CardGiftcard, "Подарки")
                )

                tabs.forEach { (tab, icons, label) ->
                    val isSelected = currentTab == tab
                    val animBgColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.75f, stiffness = 400f),
                        label = "dockItemBg"
                    )
                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        label = "dockItemColor"
                    )

                    Surface(
                        shape = PillShape,
                        color = animBgColor,
                        modifier = Modifier
                            .clip(PillShape)
                            .expressiveBounceClick { onTabSelected(tab) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = if (isSelected) 14.dp else 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isSelected) icons.first else icons.second,
                                contentDescription = label,
                                tint = contentColor,
                                modifier = Modifier.size(22.dp)
                            )
                            AnimatedVisibility(
                                visible = isSelected,
                                enter = fadeIn() + expandHorizontally(),
                                exit = fadeOut() + shrinkHorizontally()
                            ) {
                                Row {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = contentColor,
                                        maxLines = 1
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
