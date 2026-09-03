package ru.mesh.expressive

import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import kotlin.math.roundToInt
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
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

        val imageLoader = coil.ImageLoader.Builder(this)
            .components {
                if (android.os.Build.VERSION.SDK_INT >= 28) {
                    add(coil.decode.ImageDecoderDecoder.Factory())
                } else {
                    add(coil.decode.GifDecoder.Factory())
                }
            }
            .memoryCache {
                coil.memory.MemoryCache.Builder(this)
                    .maxSizePercent(0.35)
                    .build()
            }
            .diskCache {
                coil.disk.DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(100L * 1024 * 1024)
                    .build()
            }
            .respectCacheHeaders(false)
            .crossfade(false)
            .build()
        coil.Coil.setImageLoader(imageLoader)

        // Prefetch all 18 real MEsh gift animations into memory/disk cache immediately
        ru.mesh.expressive.data.repository.DemoMockDataProvider.rewards.forEach { gift ->
            val url = gift.animationUrl ?: gift.iconName
            if (!url.isNullOrBlank()) {
                val req = coil.request.ImageRequest.Builder(this)
                    .data(url)
                    .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                    .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                    .build()
                imageLoader.enqueue(req)
            }
        }

        setContent {
            var isMonetEnabled by remember { mutableStateOf(sessionManager.isMonetEnabled) }
            val mainViewModel: MeshMainViewModel = viewModel(
                factory = MeshMainViewModel.Factory(repository, sessionManager)
            )

            val themeMode by mainViewModel.themeMode.collectAsState()
            val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
            val useDarkTheme = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemDark
            }

            val enableSpringPhysics by mainViewModel.enableSpringPhysics.collectAsState()
            val hapticFeedbackEnabled by mainViewModel.hapticFeedbackEnabled.collectAsState()

            MeshExpressiveTheme(darkTheme = useDarkTheme, dynamicColor = isMonetEnabled) {
                CompositionLocalProvider(
                    ru.mesh.expressive.ui.components.LocalSpringPhysicsEnabled provides enableSpringPhysics,
                    ru.mesh.expressive.ui.components.LocalHapticFeedbackEnabled provides hapticFeedbackEnabled
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
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
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MeshMainApp(
    viewModel: MeshMainViewModel,
    isMonetEnabled: Boolean,
    onToggleMonet: (Boolean) -> Unit
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val isGiftSendActive by viewModel.isGiftSendActive.collectAsState()
    val showOnboardingGuide by viewModel.showOnboardingGuide.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val profile by viewModel.studentProfile.collectAsState()
    val view = LocalView.current

    // Dock tabs that support horizontal swipe gestures
    val dockTabs = remember {
        listOf(
            MainTab.DASHBOARD,
            MainTab.SCHEDULE,
            MainTab.HOMEWORK,
            MainTab.MARKS
        )
    }

    val initialDockIndex = remember {
        val savedTabStr = viewModel.sessionManager.startTab
        val savedTab = try { MainTab.valueOf(savedTabStr) } catch (_: Exception) { MainTab.DASHBOARD }
        val idx = dockTabs.indexOf(savedTab)
        if (idx >= 0) idx else 0
    }
    val pagerState = rememberPagerState(
        initialPage = initialDockIndex,
        pageCount = { dockTabs.size }
    )

    LaunchedEffect(Unit) {
        val savedTabStr = viewModel.sessionManager.startTab
        val savedTab = try { MainTab.valueOf(savedTabStr) } catch (_: Exception) { MainTab.DASHBOARD }
        if (savedTab != MainTab.DASHBOARD) {
            viewModel.selectTab(savedTab)
        }
    }

    var isDockClickAnimating by remember { mutableStateOf(false) }

    // Sync pager when currentTab changes (e.g. from drawer or external navigation)
    LaunchedEffect(currentTab) {
        val targetIndex = dockTabs.indexOf(currentTab)
        if (targetIndex >= 0 && pagerState.currentPage != targetIndex && !isDockClickAnimating) {
            pagerState.scrollToPage(targetIndex)
        }
    }

    // Real-time zero-lag sync when user swipes between tabs
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            if (!isDockClickAnimating && page in dockTabs.indices) {
                val targetTab = dockTabs[page]
                if (viewModel.currentTab.value in dockTabs && viewModel.currentTab.value != targetTab) {
                    viewModel.selectTab(targetTab)
                }
            }
        }
    }

    data class DrawerEntry(
        val tab: MainTab,
        val label: String,
        val icon: ImageVector
    )

    val schoolServices = remember {
        listOf(
            DrawerEntry(MainTab.MEALS, "Питание «Москвёнок»", Icons.Default.Restaurant),
            DrawerEntry(MainTab.ATTENDANCE, "Посещаемость и ЕМИАС", Icons.Default.CheckCircle),
            DrawerEntry(MainTab.CLASSMATES, "Мой класс", Icons.Default.Groups)
        )
    }

    val achievementServices = remember {
        listOf(
            DrawerEntry(MainTab.PORTFOLIO, "Портфолио и Олимпиады", Icons.Default.MilitaryTech),
            DrawerEntry(MainTab.GIFTS, "Подарки и Звёзды", Icons.Default.CardGiftcard),
            DrawerEntry(MainTab.RATING, "Рейтинг успеваемости", Icons.Default.EmojiEvents)
        )
    }

    val systemServices = remember(viewModel.isLoggedIn) {
        listOf(
            DrawerEntry(MainTab.SETTINGS, "Настройки", Icons.Default.Settings),
            DrawerEntry(MainTab.AUTH, if (viewModel.isLoggedIn) "Сменить аккаунт" else "Вход mos.ru", Icons.Default.AccountCircle)
        )
    }

    val allScrubEntries = remember(schoolServices, achievementServices, systemServices) {
        schoolServices + achievementServices + systemServices
    }

    var isScrubbing by remember { mutableStateOf(false) }
    var scrubY by remember { mutableFloatStateOf(0f) }
    var currentScrubbedTab by remember { mutableStateOf<MainTab?>(null) }
    val itemYMap = remember { mutableStateMapOf<MainTab, Float>() }

    fun findEntryAtY(y: Float): DrawerEntry? {
        if (itemYMap.isEmpty()) return null
        val closestTab = itemYMap.minByOrNull { Math.abs(it.value - y) }?.key ?: return null
        return allScrubEntries.find { it.tab == closestTab }
    }

    var menuTargetRect by remember { mutableStateOf<Rect?>(null) }
    var profileTargetRect by remember { mutableStateOf<Rect?>(null) }
    var dockTargetRect by remember { mutableStateOf<Rect?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = !isScrubbing,
            scrimColor = if (isScrubbing) Color.Transparent else DrawerDefaults.scrimColor,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier
                        .width(320.dp)
                        .fillMaxHeight(),
                    drawerContainerColor = if (isScrubbing) Color.Transparent else MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(allScrubEntries) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { offset ->
                                        isScrubbing = true
                                        scrubY = offset.y
                                        val entry = findEntryAtY(offset.y)
                                        currentScrubbedTab = entry?.tab
                                        entry?.let {
                                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                            viewModel.selectTab(it.tab)
                                        }
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        scrubY += dragAmount.y
                                        val entry = findEntryAtY(scrubY)
                                        if (entry != null && entry.tab != currentScrubbedTab) {
                                            currentScrubbedTab = entry.tab
                                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                            viewModel.selectTab(entry.tab)
                                        }
                                    },
                                    onDragEnd = {
                                        isScrubbing = false
                                        currentScrubbedTab = null
                                        coroutineScope.launch { drawerState.close() }
                                    },
                                    onDragCancel = {
                                        isScrubbing = false
                                        currentScrubbedTab = null
                                        coroutineScope.launch { drawerState.close() }
                                    }
                                )
                            }
                    ) {
                        // Standard drawer content (fades to alpha 0 when scrubbing)
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { alpha = if (isScrubbing) 0f else 1f }
                                .verticalScroll(rememberScrollState())
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))

                            // Profile Header Card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                                    .clickable {
                                        viewModel.selectTab(MainTab.SETTINGS)
                                        coroutineScope.launch { drawerState.close() }
                                    },
                                shape = ExpressiveCardShape,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(M3Cookie7Shape(7))
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (!profile.avatarUrl.isNullOrBlank()) {
                                            AsyncImage(
                                                model = profile.avatarUrl,
                                                contentDescription = "Аватар",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            val initials = "${profile.firstName.firstOrNull() ?: 'М'}${profile.lastName.firstOrNull() ?: 'Э'}"
                                            Text(
                                                text = initials,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${profile.firstName} ${profile.lastName}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (profile.className.isNotBlank()) "${profile.className} • МЭШ" else "МЭШ",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (profile.gpa > 0.0) {
                                        Surface(
                                            shape = PillShape,
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        ) {
                                            Text(
                                                text = String.format(java.util.Locale.US, "%.2f", profile.gpa),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Category: Школьные сервисы
                            Text(
                                text = "ШКОЛЬНЫЕ СЕРВИСЫ",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 24.dp, top = 8.dp, bottom = 4.dp)
                            )
                            schoolServices.forEach { entry ->
                                DrawerMenuItem(
                                    icon = entry.icon,
                                    label = entry.label,
                                    isSelected = currentTab == entry.tab,
                                    modifier = Modifier.onGloballyPositioned { coords ->
                                        itemYMap[entry.tab] = coords.boundsInRoot().top + coords.size.height / 2f
                                    },
                                    onClick = {
                                        viewModel.selectTab(entry.tab)
                                        coroutineScope.launch { drawerState.close() }
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Category: Активность и награды
                            Text(
                                text = "АКТИВНОСТЬ И НАГРАДЫ",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 24.dp, top = 8.dp, bottom = 4.dp)
                            )
                            achievementServices.forEach { entry ->
                                DrawerMenuItem(
                                    icon = entry.icon,
                                    label = entry.label,
                                    isSelected = currentTab == entry.tab,
                                    modifier = Modifier.onGloballyPositioned { coords ->
                                        itemYMap[entry.tab] = coords.boundsInRoot().top + coords.size.height / 2f
                                    },
                                    onClick = {
                                        viewModel.selectTab(entry.tab)
                                        coroutineScope.launch { drawerState.close() }
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))
                            Spacer(modifier = Modifier.height(4.dp))

                            // Category: Система
                            Text(
                                text = "СИСТЕМА",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 24.dp, top = 8.dp, bottom = 4.dp)
                            )
                            systemServices.forEach { entry ->
                                DrawerMenuItem(
                                    icon = entry.icon,
                                    label = entry.label,
                                    isSelected = currentTab == entry.tab,
                                    modifier = Modifier.onGloballyPositioned { coords ->
                                        itemYMap[entry.tab] = coords.boundsInRoot().top + coords.size.height / 2f
                                    },
                                    onClick = {
                                        viewModel.selectTab(entry.tab)
                                        coroutineScope.launch { drawerState.close() }
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        // Floating Scrub Peek HUD
                        if (isScrubbing && currentScrubbedTab != null) {
                            val activeEntry = allScrubEntries.find { it.tab == currentScrubbedTab }
                            if (activeEntry != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 24.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .offset {
                                                IntOffset(
                                                    x = 16.dp.roundToPx(),
                                                    y = (scrubY - 24.dp.toPx()).toInt().coerceIn(60, 2000)
                                                )
                                            },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = activeEntry.icon,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Text(
                                            text = activeEntry.label,
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        ) {
        Scaffold(
            topBar = {
                if (currentTab != MainTab.AUTH && !isGiftSendActive) {
                    Surface(
                        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shadowElevation = 3.dp
                    ) {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    text = when (currentTab) {
                                        MainTab.DASHBOARD -> "Дневник МЭШ"
                                        MainTab.SCHEDULE -> "Расписание"
                                        MainTab.HOMEWORK -> "Задания"
                                        MainTab.MARKS -> "Оценки"
                                        MainTab.PORTFOLIO -> "Портфолио и Олимпиады"
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
                                val isOffline by viewModel.isOffline.collectAsState()
                                if (isOffline) {
                                    Surface(
                                        shape = PillShape,
                                        color = MaterialTheme.colorScheme.errorContainer,
                                        modifier = Modifier.padding(end = 6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.CloudOff,
                                                contentDescription = null,
                                                modifier = Modifier.size(13.dp),
                                                tint = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                "Офлайн",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.selectTab(MainTab.SETTINGS) },
                                    modifier = Modifier.onGloballyPositioned { profileTargetRect = it.boundsInRoot() }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(M3Cookie7Shape(7))
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (!profile.avatarUrl.isNullOrBlank()) {
                                            AsyncImage(
                                                model = profile.avatarUrl,
                                                contentDescription = "Аватар",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else if (profile.firstName.isNotBlank() && profile.lastName.isNotBlank()) {
                                            val initials = "${profile.firstName.first()}${profile.lastName.first()}"
                                            Text(
                                                text = initials,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.AccountCircle,
                                                contentDescription = "Профиль",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(26.dp)
                                            )
                                        }
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = Color.Transparent
                            )
                        )
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = if (currentTab == MainTab.AUTH || isGiftSendActive) 0.dp else paddingValues.calculateTopPadding())
            ) {
                if (isGiftSendActive) {
                    GiftSendScreen(viewModel = viewModel)
                } else if (currentTab in dockTabs) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        when (dockTabs[page]) {
                            MainTab.DASHBOARD -> DashboardScreen(
                                viewModel = viewModel,
                                onNavigate = { viewModel.selectTab(it) }
                            )
                            MainTab.SCHEDULE -> ScheduleScreen(viewModel = viewModel)
                            MainTab.HOMEWORK -> HomeworkScreen(viewModel = viewModel)
                            MainTab.MARKS -> MarksScreen(viewModel = viewModel)
                            else -> {}
                        }
                    }
                } else {
                    AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = {
                            fadeIn(animationSpec = androidx.compose.animation.core.spring(stiffness = 500f)) togetherWith
                            fadeOut(animationSpec = androidx.compose.animation.core.spring(stiffness = 500f))
                        },
                        label = "SecondaryScreen"
                    ) { tab ->
                        when (tab) {
                            MainTab.PORTFOLIO -> PortfolioScreen(viewModel = viewModel)
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
                            else -> {}
                        }
                    }
                }

                if (currentTab != MainTab.AUTH && !isGiftSendActive) {
                    val isHaptic = ru.mesh.expressive.ui.components.LocalHapticFeedbackEnabled.current
                    val haptic = LocalHapticFeedback.current
                    ExpressiveFloatingDock(
                        currentTab = currentTab,
                        onTabSelected = { tab ->
                            if (isHaptic) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.selectTab(tab)
                            val targetIndex = dockTabs.indexOf(tab)
                            if (targetIndex >= 0 && pagerState.currentPage != targetIndex) {
                                coroutineScope.launch {
                                    pagerState.scrollToPage(targetIndex)
                                }
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .onGloballyPositioned { dockTargetRect = it.boundsInRoot() }
                    )
                }

                }
            }
        }

        ru.mesh.expressive.ui.components.LessonDetailBottomSheet(viewModel = viewModel)
        ru.mesh.expressive.ui.components.MarkDetailBottomSheet(viewModel = viewModel)
        ru.mesh.expressive.ui.components.HomeworkDetailBottomSheet(viewModel = viewModel)
        ru.mesh.expressive.ui.components.TestExecutionWebViewDialog(viewModel = viewModel)

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
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        label = {
            Text(
                text = label,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
        },
        selected = isSelected,
        onClick = onClick,
        modifier = modifier.padding(horizontal = 12.dp, vertical = 2.dp),
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
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    val tabs = remember {
        listOf(
            Triple(MainTab.DASHBOARD, Icons.Filled.Home to Icons.Outlined.Home, "Главная"),
            Triple(MainTab.SCHEDULE, Icons.Filled.CalendarToday to Icons.Outlined.CalendarToday, "Расписание"),
            Triple(MainTab.HOMEWORK, Icons.Filled.EditNote to Icons.Outlined.EditNote, "Задания"),
            Triple(MainTab.MARKS, Icons.Filled.Grade to Icons.Outlined.Grade, "Оценки")
        )
    }

    val currentTabState by rememberUpdatedState(currentTab)
    val onTabSelectedState by rememberUpdatedState(onTabSelected)

    val coroutineScope = rememberCoroutineScope()
    val textMeasurer = androidx.compose.ui.text.rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)

    val dropletWidthPx = density.run { 68.dp.toPx() }
    val dropletHeightPx = density.run { 42.dp.toPx() }

    // Precalculate deterministic resting expanded width for every tab
    val fullTabWidthsPx = remember(density) {
        tabs.associate { (tab, _, label) ->
            val tw = textMeasurer.measure(label, labelStyle).size.width
            val totalPx = density.run { 56.dp.toPx() } + tw
            tab to totalPx
        }
    }

    val animDropletLeft = remember { Animatable(0f) }
    val settleProgress = remember { Animatable(1f) }

    var isDragging by remember { mutableStateOf(false) }
    var isSettling by remember { mutableStateOf(false) }
    val isDropletActive = isDragging || isSettling

    var releaseLeft by remember { mutableFloatStateOf(0f) }
    var releaseWidth by remember { mutableFloatStateOf(0f) }

    var fingerX by remember { mutableFloatStateOf(0f) }
    var lastDeltaX by remember { mutableFloatStateOf(0f) }
    var dockWidth by remember { mutableFloatStateOf(0f) }

    val activeIndex = remember(currentTab) { tabs.indexOfFirst { it.first == currentTab }.coerceAtLeast(0) }
    val targetLeft = density.run { (activeIndex * 50).dp.toPx() }
    val targetWidth = fullTabWidthsPx[currentTab] ?: dropletWidthPx

    val curDropletLeft = if (isDragging) {
        animDropletLeft.value
    } else {
        releaseLeft + (targetLeft - releaseLeft) * settleProgress.value
    }

    val curDropletWidth = if (isDragging) {
        dropletWidthPx
    } else {
        releaseWidth + (targetWidth - releaseWidth) * settleProgress.value
    }

    val curScale = if (isDragging) {
        1.12f
    } else {
        1.12f - (0.12f * settleProgress.value)
    }

    val curElevation = if (isDragging) {
        density.run { 8.dp.toPx() }
    } else {
        density.run { 8.dp.toPx() } * (1f - settleProgress.value)
    }

    // Liquid squish & stretch physics (active only during active drag)
    val stretchX = if (isDragging) {
        1f + (lastDeltaX.coerceIn(-20f, 20f) / 90f).let { kotlin.math.abs(it) }
    } else 1.0f
    val stretchY = 1f / kotlin.math.sqrt(stretchX)

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
            Box(
                modifier = Modifier
                    .onGloballyPositioned { coords ->
                        dockWidth = coords.size.width.toFloat()
                    }
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                val curTab = currentTabState
                                val curIndex = tabs.indexOfFirst { it.first == curTab }.coerceAtLeast(0)
                                val activeLeft = density.run { (curIndex * 50).dp.toPx() }
                                val activeW = fullTabWidthsPx[curTab] ?: dropletWidthPx

                                val isOverActive = down.position.x >= activeLeft - 20.dp.toPx() &&
                                    down.position.x <= activeLeft + activeW + 20.dp.toPx()

                                if (!isOverActive) {
                                    continue
                                }

                                val touchStartX = down.position.x
                                fingerX = touchStartX
                                lastDeltaX = 0f

                                var dragActive = false

                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break

                                    if (!change.pressed) {
                                        break
                                    }

                                    val dx = change.position.x - touchStartX
                                    val uptime = change.uptimeMillis - down.uptimeMillis

                                    // Hold threshold for liquid drag
                                    if (!dragActive && (uptime > 150L || kotlin.math.abs(dx.toDouble()) > 14.0)) {
                                        dragActive = true
                                        isDragging = true
                                        isSettling = false
                                        fingerX = change.position.x
                                        val curDropX = (fingerX - dropletWidthPx / 2f).coerceIn(4f, (dockWidth - dropletWidthPx - 4f).coerceAtLeast(4f))
                                        coroutineScope.launch {
                                            animDropletLeft.snapTo(curDropX)
                                            settleProgress.snapTo(0f)
                                        }
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }

                                    if (dragActive) {
                                        change.consume()
                                        lastDeltaX = change.position.x - fingerX
                                        fingerX = change.position.x
                                        val curDropX = (fingerX - dropletWidthPx / 2f).coerceIn(4f, (dockWidth - dropletWidthPx - 4f).coerceAtLeast(4f))

                                        coroutineScope.launch {
                                            animDropletLeft.snapTo(curDropX)
                                        }

                                        // Use static slot boundaries to prevent hysteresis vibration
                                        if (dockWidth > 0f) {
                                            val slotWidth = dockWidth / tabs.size.toFloat()
                                            val slotIndex = (fingerX / slotWidth).toInt().coerceIn(0, tabs.size - 1)
                                            val closest = tabs[slotIndex].first

                                            if (closest != currentTabState) {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                onTabSelectedState(closest)
                                            }
                                        }
                                    }
                                }

                                if (dragActive) {
                                    releaseLeft = animDropletLeft.value
                                    releaseWidth = dropletWidthPx
                                    isDragging = false
                                    isSettling = true
                                    lastDeltaX = 0f

                                    coroutineScope.launch {
                                        settleProgress.snapTo(0f)
                                        settleProgress.animateTo(
                                            targetValue = 1f,
                                            animationSpec = spring(
                                                dampingRatio = 0.76f,
                                                stiffness = 320f
                                            )
                                        )
                                        isSettling = false
                                    }
                                }
                            }
                        }
                    }
                    .padding(horizontal = 6.dp, vertical = 6.dp)
            ) {
                // 1. Fluid Droplet Selection Card — RENDERED WHILE DRAGGING OR SETTLING!
                if (isDropletActive) {
                    val wDp = density.run { curDropletWidth.toDp() }
                    val hDp = density.run { dropletHeightPx.toDp() }
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(curDropletLeft.roundToInt(), 0) }
                            .size(width = wDp, height = hDp)
                            .graphicsLayer {
                                this.scaleX = curScale * stretchX
                                this.scaleY = curScale * stretchY
                                this.shadowElevation = curElevation
                                this.shape = PillShape
                            }
                            .clip(PillShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    )
                }

                // 2. Tab Items Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEach { (tab, icons, label) ->
                        val isSelected = currentTab == tab

                        // Normal tap background: starts fading in on release, perfectly matching the droplet
                        val animBgColor by animateColorAsState(
                            targetValue = if (isSelected && !isDragging) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
                            label = "dockItemBg"
                        )
                        val contentColor by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
                            label = "dockItemColor"
                        )

                        Surface(
                            shape = PillShape,
                            color = animBgColor,
                            modifier = Modifier
                                .clip(PillShape)
                                .clickable { onTabSelected(tab) }
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
}
