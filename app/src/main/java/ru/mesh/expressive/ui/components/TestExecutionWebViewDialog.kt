package ru.mesh.expressive.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.mesh.expressive.ui.viewmodel.MeshMainViewModel

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestExecutionWebViewDialog(viewModel: MeshMainViewModel) {
    val activeUrl by viewModel.activeTestExecutionUrl.collectAsState()
    val activeTitle by viewModel.activeTestExecutionTitle.collectAsState()
    val profile by viewModel.studentProfile.collectAsState()
    val context = LocalContext.current

    if (activeUrl != null) {
        val targetUrl = activeUrl!!
        var progress by remember { mutableIntStateOf(0) }
        var isPageLoading by remember { mutableStateOf(true) }
        var webViewRef by remember { mutableStateOf<WebView?>(null) }

        Dialog(
            onDismissRequest = { viewModel.closeTestExecution() },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            androidx.activity.compose.BackHandler {
                viewModel.closeTestExecution()
            }
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = activeTitle ?: "Выполнение задания",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { viewModel.closeTestExecution() }) {
                                Icon(Icons.Default.Close, contentDescription = "Закрыть")
                            }
                        },
                        actions = {
                            IconButton(onClick = { webViewRef?.reload() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Обновить")
                            }
                            IconButton(onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }) {
                                Icon(Icons.Default.OpenInBrowser, contentDescription = "В браузере")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            WebView(ctx).apply {
                                webViewRef = this
                                layoutParams = android.view.ViewGroup.LayoutParams(
                                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                                )

                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    databaseEnabled = true
                                    loadWithOverviewMode = true
                                    useWideViewPort = true
                                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                    userAgentString = settings.userAgentString + " MeshClient/2.0 MobileApp"
                                }

                                // Setup cookies with student session
                                val cookieManager = CookieManager.getInstance()
                                cookieManager.setAcceptCookie(true)
                                cookieManager.setAcceptThirdPartyCookies(this, true)

                                val token = viewModel.sessionManager.authToken?.replace("Bearer ", "") ?: ""
                                val contingentGuid = profile.contingentGuid.ifBlank { "3473f068-8ec0-47a1-920a-a18e75d6c389" }

                                val cookieTargets = listOf("https://school.mos.ru", "https://uchebnik.mos.ru", "https://mos.ru")
                                val cookieList = listOf(
                                    "auth_token=$token",
                                    "aupd_token=$token",
                                    "student_person_id=$contingentGuid",
                                    "aupd_current_role=2:1",
                                    "profile_id=${profile.profileId}",
                                    "token=$token"
                                )
                                cookieTargets.forEach { targetUrlStr ->
                                    cookieList.forEach { cookie ->
                                        cookieManager.setCookie(targetUrlStr, "$cookie; Domain=.mos.ru; Path=/; Secure; SameSite=None")
                                    }
                                }
                                cookieManager.flush()

                                webViewClient = object : WebViewClient() {
                                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                        return false
                                    }

                                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                        isPageLoading = true
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        isPageLoading = false
                                    }
                                }

                                webChromeClient = object : WebChromeClient() {
                                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                        progress = newProgress
                                        if (newProgress >= 100) isPageLoading = false
                                    }
                                }

                                if (targetUrl.contains("school.mos.ru") && !targetUrl.contains("launcher")) {
                                    val headers = mutableMapOf<String, String>()
                                    if (token.isNotBlank()) {
                                        headers["Authorization"] = "Bearer $token"
                                        headers["x-mes-subsystem"] = "familymp"
                                        headers["client-type"] = "diary-mobile"
                                    }
                                    loadUrl(targetUrl, headers)
                                } else {
                                    loadUrl(targetUrl)
                                }
                            }
                        }
                    )

                    if (isPageLoading && progress < 100) {
                        LinearProgressIndicator(
                            progress = { progress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
