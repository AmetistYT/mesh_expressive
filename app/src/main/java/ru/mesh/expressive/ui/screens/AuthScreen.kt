package ru.mesh.expressive.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.http.SslError
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch
import ru.mesh.expressive.data.local.RootTokenExtractor
import ru.mesh.expressive.ui.components.M3CircularWavyLoader
import ru.mesh.expressive.ui.components.M3WavyProgressIndicator
import ru.mesh.expressive.ui.theme.*
import ru.mesh.expressive.ui.viewmodel.MeshMainViewModel

private const val MOS_RU_AUTH_URL = "https://login.mos.ru/sps/oauth/ae?client_id=dnevnik.mos.ru&redirect_uri=https://school.mos.ru/&response_type=code&scope=openid+profile+snils+contacts+birthday"

enum class AuthScreenState {
    WELCOME_LANDING, WEB_MOS_RU, ROOT_AND_MANUAL_TOKEN
}

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: MeshMainViewModel,
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    var screenState by remember { mutableStateOf(AuthScreenState.WELCOME_LANDING) }
    var manualToken by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var webProgress by remember { mutableFloatStateOf(0.1f) }
    var currentUrl by remember { mutableStateOf(MOS_RU_AUTH_URL) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var isExtractingRoot by remember { mutableStateOf(false) }
    var rootDialogMessage by remember { mutableStateOf<String?>(null) }
    var detectedWebToken by remember { mutableStateOf<String?>(null) }
    var showMosRuGuideDialog by remember { mutableStateOf(false) }
    var showHowItWorksDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            if (screenState != AuthScreenState.WELCOME_LANDING) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = when (screenState) {
                                AuthScreenState.WEB_MOS_RU -> "Вход через mos.ru"
                                AuthScreenState.ROOT_AND_MANUAL_TOKEN -> "Импорт токена"
                                else -> "Вход в МЭШ"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { screenState = AuthScreenState.WELCOME_LANDING }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                        }
                    },
                    actions = {
                        if (screenState == AuthScreenState.WEB_MOS_RU) {
                            IconButton(onClick = { webViewInstance?.reload() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Обновить")
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            AnimatedContent(
                targetState = screenState,
                label = "AuthScreenStateAnimation"
            ) { targetState ->
                when (targetState) {
                    AuthScreenState.WELCOME_LANDING -> {
                        WelcomeLandingView(
                            onLoginMosRuClick = {
                                screenState = AuthScreenState.WEB_MOS_RU
                                showMosRuGuideDialog = true
                            },
                            onRootTokenClick = { screenState = AuthScreenState.ROOT_AND_MANUAL_TOKEN },
                            onContinueDemoClick = onAuthSuccess
                        )
                    }

                    AuthScreenState.WEB_MOS_RU -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White)
                        ) {
                            AndroidView(
                                factory = { ctx ->
                                    WebView(ctx).apply {
                                        setBackgroundColor(android.graphics.Color.WHITE)
                                        layoutParams = ViewGroup.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.MATCH_PARENT
                                        )

                                        val cookieManager = CookieManager.getInstance()
                                        cookieManager.setAcceptCookie(true)
                                        cookieManager.setAcceptThirdPartyCookies(this, true)

                                        settings.apply {
                                            javaScriptEnabled = true
                                            domStorageEnabled = true
                                            databaseEnabled = true
                                            allowFileAccess = true
                                            allowContentAccess = true
                                            loadWithOverviewMode = true
                                            useWideViewPort = true
                                            setSupportMultipleWindows(false)
                                            javaScriptCanOpenWindowsAutomatically = true
                                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                            cacheMode = WebSettings.LOAD_DEFAULT
                                            userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
                                        }

                                        webChromeClient = object : WebChromeClient() {
                                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                                webProgress = newProgress / 100f
                                                if (newProgress >= 100) isLoading = false
                                            }
                                        }

                                        webViewClient = object : WebViewClient() {
                                            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                                                handler?.proceed()
                                            }

                                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                                val url = request?.url?.toString() ?: return false
                                                if (url.startsWith("http://") || url.startsWith("https://")) {
                                                    view?.loadUrl(url)
                                                    return true
                                                }
                                                return false
                                            }

                                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                                super.onPageStarted(view, url, favicon)
                                                isLoading = true
                                                url?.let { currentUrl = it }
                                            }

                                            override fun onPageFinished(view: WebView?, url: String?) {
                                                super.onPageFinished(view, url)
                                                isLoading = false
                                                url?.let {
                                                    currentUrl = it
                                                    val c1 = cookieManager.getCookie("https://school.mos.ru/") ?: ""
                                                    val c2 = cookieManager.getCookie("https://mos.ru/") ?: ""
                                                    val c3 = cookieManager.getCookie(it) ?: ""
                                                    val allCookies = "$c1; $c2; $c3"

                                                    val extracted = extractValidTokenFromCookies(allCookies)
                                                    if (!extracted.isNullOrBlank()) {
                                                        detectedWebToken = extracted
                                                    }
                                                }
                                            }

                                            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                                                super.onReceivedError(view, request, error)
                                                isLoading = false
                                            }
                                        }
                                        loadUrl(MOS_RU_AUTH_URL)
                                        webViewInstance = this
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )

                            if (isLoading) {
                                M3WavyProgressIndicator(
                                    progress = webProgress,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.TopCenter)
                                )
                            }

                            // Persistent Confirmation Button
                            Button(
                                onClick = {
                                    val cm = CookieManager.getInstance()
                                    val c1 = cm.getCookie("https://school.mos.ru/") ?: ""
                                    val c2 = cm.getCookie("https://mos.ru/") ?: ""
                                    val c3 = cm.getCookie(currentUrl) ?: ""
                                    val allCookies = "$c1; $c2; $c3"

                                    val token = detectedWebToken ?: extractValidTokenFromCookies(allCookies)
                                    coroutineScope.launch {
                                        if (!token.isNullOrBlank()) {
                                            viewModel.saveAuthToken(token)
                                        }
                                        onAuthSuccess()
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(20.dp)
                                    .fillMaxWidth(),
                                shape = PillShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Подтвердить вход в дневник")
                            }
                        }
                    }

                    AuthScreenState.ROOT_AND_MANUAL_TOKEN -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Root Extractor Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = ExpressiveCardShape,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(M3Cookie7Shape(7))
                                                .background(MaterialTheme.colorScheme.primary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AdminPanelSettings,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Импорт токена через Root",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Text(
                                                text = "Извлекает активную сессию из официального ru.mes.dnevnik",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    if (isExtractingRoot) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            M3CircularWavyLoader(size = 32.dp, color = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text("Поиск токена в ru.mes.dnevnik...")
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                coroutineScope.launch {
                                                    isExtractingRoot = true
                                                    val res = RootTokenExtractor.extractTokenFromOfficialApp(context.cacheDir)
                                                    isExtractingRoot = false
                                                    if (res.isSuccess && !res.token.isNullOrBlank()) {
                                                        manualToken = res.token
                                                        viewModel.saveAuthToken(res.token)
                                                        rootDialogMessage = "Токен успешно извлечен через Root! Загружаю дневник..."
                                                    } else {
                                                        rootDialogMessage = res.message
                                                    }
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = PillShape,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary
                                            )
                                        ) {
                                            Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Извлечь токен через Root (Авто)")
                                        }
                                    }
                                }
                            }

                            // Manual Input Section
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = ExpressiveCardShape,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Ручной ввод токена",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Вставьте JWT токен (Bearer eyJ...) или session-cookie вручную.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = manualToken,
                                        onValueChange = { manualToken = it },
                                        label = { Text("Токен МЭШ (Bearer / JWT)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        minLines = 2,
                                        maxLines = 4
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            if (manualToken.isNotBlank()) {
                                                coroutineScope.launch {
                                                    viewModel.saveAuthToken(manualToken.trim())
                                                    onAuthSuccess()
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = PillShape,
                                        enabled = manualToken.isNotBlank()
                                    ) {
                                        Text("Авторизоваться")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (rootDialogMessage != null) {
        AlertDialog(
            onDismissRequest = {
                val msg = rootDialogMessage
                rootDialogMessage = null
                if (msg?.contains("успешно") == true) {
                    onAuthSuccess()
                }
            },
            icon = { Icon(Icons.Default.Done, contentDescription = null, tint = ScoreGreen, modifier = Modifier.size(32.dp)) },
            title = { Text("Импорт через Root", fontWeight = FontWeight.Bold) },
            text = { Text(rootDialogMessage!!) },
            confirmButton = {
                Button(
                    onClick = {
                        val msg = rootDialogMessage
                        rootDialogMessage = null
                        if (msg?.contains("успешно") == true) {
                            onAuthSuccess()
                        }
                    },
                    shape = PillShape
                ) {
                    Text("OK")
                }
            }
        )
    }

    if (showMosRuGuideDialog) {
        AlertDialog(
            onDismissRequest = { showMosRuGuideDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.VpnKey,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Вход через mos.ru",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "1. Войдите в свой аккаунт на странице mos.ru.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "2. Как только появится страница дневника или белый экран — нажмите кнопку «Подтвердить вход в дневник» внизу экрана.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.VpnLock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Если не грузится — отключите VPN (нужно только для входа).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = { showHowItWorksDialog = true },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Как это работает?",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showMosRuGuideDialog = false },
                    shape = PillShape,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Понятно")
                }
            }
        )
    }

    if (showHowItWorksDialog) {
        AlertDialog(
            onDismissRequest = { showHowItWorksDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Как это работает?",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HowItWorksInfoItem(
                        number = "1",
                        title = "Вход в свой аккаунт",
                        description = "Вы вводите логин и пароль прямо на официальной странице mos.ru через государственную систему СУДИР. Пароли не передаются приложению."
                    )
                    HowItWorksInfoItem(
                        number = "2",
                        title = "Страница дневника или белый экран",
                        description = "После ввода пароля и СМС mos.ru перенаправит вас на школьный портал. Страница может остаться белой или показать дневник — это значит, что сессия успешно создана. Просто нажмите внизу кнопку «Подтвердить вход в дневник»."
                    )
                    HowItWorksInfoItem(
                        number = "3",
                        title = "Почему мешает VPN?",
                        description = "Серверы mos.ru блокируют зарубежные IP-адреса для защиты от атак. Если страница зависла или выдает ошибку, выключите VPN только на время авторизации. Сразу после входа VPN можно включить обратно."
                    )
                    HowItWorksInfoItem(
                        number = "4",
                        title = "0 трекеров и открытость",
                        description = "Приложение сохраняет полученный ключ сессии только на вашем телефоне. Никакой аналитики Яндекса, рекламы или слежки."
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showHowItWorksDialog = false },
                    shape = PillShape,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Понятно")
                }
            }
        )
    }
}

@Composable
private fun HowItWorksInfoItem(
    number: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = number,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WelcomeLandingView(
    onLoginMosRuClick: () -> Unit,
    onRootTokenClick: () -> Unit,
    onContinueDemoClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // App Emblem
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(M3Cookie7Shape(7))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Дневник МЭШ",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Современный клиент электронного дневника в стиле Material 3 Expressive",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Feature Highlights
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                WelcomeFeatureRow(
                    icon = Icons.Default.DoneAll,
                    title = "Авто-сдача заданий за звезды",
                    subtitle = "Мгновенное выполнение ЦДЗ и получение наград в 1 клик"
                )
                WelcomeFeatureRow(
                    icon = Icons.Default.Shield,
                    title = "0 трекеров и чистый трафик",
                    subtitle = "Без AppMetrica, геотрекинга «Спутник» и телеметрии"
                )
                WelcomeFeatureRow(
                    icon = Icons.Default.Palette,
                    title = "Material 3 Expressive & Monet",
                    subtitle = "Адаптивная тема обоев, пружинная физика и чистый UI"
                )
                WelcomeFeatureRow(
                    icon = Icons.Default.Restaurant,
                    title = "Москвёнок и Рейтинг щедрости",
                    subtitle = "Точный баланс карты, лимиты и рейтинг потраченных звезд"
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onLoginMosRuClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = PillShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Войти через mos.ru",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            OutlinedButton(
                onClick = onRootTokenClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = PillShape
            ) {
                Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Импорт через Root / Токен",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }

            TextButton(
                onClick = onContinueDemoClick,
                modifier = Modifier.fillMaxWidth(),
                shape = PillShape
            ) {
                Text(
                    text = "Открыть в демонстрационном режиме",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "semi vibecoded by gemini",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun WelcomeFeatureRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = ExpressiveCardShape,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun extractValidTokenFromCookies(cookies: String): String? {
    val map = cookies.split(";").mapNotNull {
        val parts = it.split("=")
        if (parts.size >= 2) parts[0].trim() to parts[1].trim() else null
    }.toMap()

    val token = map["aupd_token"] ?: map["auth_token"] ?: map["mes_session"]
    if (!token.isNullOrBlank() && token.length > 20) {
        return token
    }
    return null
}
