package ru.mesh.expressive.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.http.SslError
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.mesh.expressive.data.local.TokenUtils
import ru.mesh.expressive.ui.theme.ExpressiveHeroShape
import ru.mesh.expressive.ui.theme.PillShape

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BackgroundTokenRefreshWebView(
    onTokenAcquired: (String) -> Unit,
    onError: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var hasFinished by remember { mutableStateOf(false) }

    // 20-second safety timeout
    LaunchedEffect(Unit) {
        delay(20_000L)
        if (!hasFinished) {
            hasFinished = true
            webViewRef?.let { wv ->
                wv.stopLoading()
                wv.loadUrl("about:blank")
                wv.clearHistory()
                wv.removeAllViews()
                wv.destroy()
            }
            webViewRef = null
            onError()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewRef?.let { wv ->
                wv.stopLoading()
                wv.loadUrl("about:blank")
                wv.clearHistory()
                wv.removeAllViews()
                wv.destroy()
            }
            webViewRef = null
        }
    }

    Box(
        modifier = Modifier
            .size(1.dp)
            .background(Color.Transparent)
    ) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(1, 1)

                    val cookieManager = CookieManager.getInstance()
                    cookieManager.setAcceptCookie(true)
                    cookieManager.setAcceptThirdPartyCookies(this, true)

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        allowFileAccess = true
                        allowContentAccess = true
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        userAgentString = "Mozilla/5.0 (Linux; Android 13; Pixel 7 Pro Build/TD1A.220804.031) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36"
                    }

                    fun checkCookiesAndFinish(currentUrl: String) {
                        if (hasFinished) return
                        val c1 = cookieManager.getCookie("https://school.mos.ru/") ?: ""
                        val c2 = cookieManager.getCookie("https://mos.ru/") ?: ""
                        val c3 = cookieManager.getCookie(currentUrl) ?: ""
                        val allCookies = "$c1; $c2; $c3"
                        val extracted = TokenUtils.extractValidTokenFromCookies(allCookies)
                        if (!extracted.isNullOrBlank()) {
                            hasFinished = true
                            cookieManager.flush()
                            post {
                                stopLoading()
                                loadUrl("about:blank")
                                clearHistory()
                                removeAllViews()
                                destroy()
                                webViewRef = null
                            }
                            onTokenAcquired(extracted)
                        }
                    }

                    webViewClient = object : WebViewClient() {
                        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                            handler?.proceed()
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            if (hasFinished || url == null) return
                            checkCookiesAndFinish(url)
                            if (hasFinished) return

                            // If on school.mos.ru landing page, wait 1.5 seconds then click "Войти" button or navigate to sudir login
                            if (url.contains("school.mos.ru") && !url.contains("sudir") && !url.contains("oauth")) {
                                coroutineScope.launch {
                                    delay(1500L)
                                    if (hasFinished) return@launch
                                    val clickScript = """
                                        (function() {
                                            try {
                                                var all = document.querySelectorAll('button, div, a, span');
                                                for (var i = 0; i < all.length; i++) {
                                                    var el = all[i];
                                                    if (el.children.length === 0 && el.textContent && el.textContent.trim() === 'Войти') {
                                                        el.click();
                                                        return 'clicked';
                                                    }
                                                }
                                                window.location.href = 'https://school.mos.ru/v3/auth/sudir/login';
                                                return 'redirect';
                                            } catch(e) {
                                                window.location.href = 'https://school.mos.ru/v3/auth/sudir/login';
                                                return 'err';
                                            }
                                        })();
                                    """.trimIndent()
                                    evaluateJavascript(clickScript, null)
                                }
                            }
                        }

                        override fun onLoadResource(view: WebView?, url: String?) {
                            super.onLoadResource(view, url)
                            if (hasFinished || url == null) return
                            if (url.contains("school.mos.ru") || url.contains("mos.ru")) {
                                checkCookiesAndFinish(url)
                            }
                        }
                    }

                    loadUrl("https://school.mos.ru/")
                    webViewRef = this
                }
            },
            modifier = Modifier.size(1.dp)
        )
    }
}

@Composable
fun TokenRefreshOverlay(
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(0.9f),
            shape = ExpressiveHeroShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top row with close button (крестик: посмотреть кэш)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Посмотреть кэш",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                M3CircularWavyLoader(
                    modifier = Modifier.size(60.dp),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Обновление сессии",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Подождите, обновляем ваш токен...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    shape = PillShape,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Посмотреть кэш")
                }
            }
        }
    }
}

@Composable
fun ReAuthDialog(
    onDismiss: () -> Unit,
    onReAuth: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.LockReset,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text("Сессия истекла", fontWeight = FontWeight.Bold)
        },
        text = {
            Text("Срок действия сессии mos.ru завершился. Пожалуйста, выполните повторный вход через mos.ru.")
        },
        confirmButton = {
            Button(
                onClick = onReAuth,
                shape = PillShape
            ) {
                Text("Войти")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Позже (кэш)")
            }
        }
    )
}
