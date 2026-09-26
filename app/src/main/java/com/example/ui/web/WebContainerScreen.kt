package com.example.ui.web

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.KMartPrimary
import com.example.ui.theme.KMartPrimaryDark

enum class WebViewportMode(val title: String) {
    ADAPTIVE("Adaptive"),
    DESKTOP("Desktop (1200px)"),
    MOBILE("Mobile (390px)")
}

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebContainerScreen(
    onSwitchToNative: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var viewportMode by remember { mutableStateOf(WebViewportMode.ADAPTIVE) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var pageTitle by remember { mutableStateOf("JSR KMart | 10-Minute Grocery Delivery") }
    var isLoading by remember { mutableStateOf(false) }
    var currentUrl by remember { mutableStateOf("https://jsrkmart.in") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // Browser Chrome / Top Bar
        Surface(
            color = Color(0xFF1E293B),
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Top control row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Browser Title & Native Return Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = KMartPrimary,
                            modifier = Modifier
                                .clickable { onSwitchToNative() }
                                .padding(end = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Native App",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Native App",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Text(
                            text = "🌐 Responsive Web App",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Viewport Toggle Controls
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Mobile View Button
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (viewportMode == WebViewportMode.MOBILE) KMartPrimary else Color(0xFF334155),
                            modifier = Modifier.clickable { viewportMode = WebViewportMode.MOBILE }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Smartphone,
                                    contentDescription = "Mobile View",
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("Mobile", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Desktop View Button
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (viewportMode == WebViewportMode.DESKTOP) KMartPrimary else Color(0xFF334155),
                            modifier = Modifier.clickable { viewportMode = WebViewportMode.DESKTOP }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Computer,
                                    contentDescription = "Desktop View",
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("Desktop", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Adaptive View Button
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (viewportMode == WebViewportMode.ADAPTIVE) KMartPrimary else Color(0xFF334155),
                            modifier = Modifier.clickable { viewportMode = WebViewportMode.ADAPTIVE }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Devices,
                                    contentDescription = "Adaptive View",
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("Full", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Simulated Address / URL Bar
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF0F172A),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "SSL Secure",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "https://jsrkmart.in",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = KMartPrimary
                            )
                        } else {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Reload",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable {
                                        webViewInstance?.reload()
                                    }
                            )
                        }
                    }
                }
            }
        }

        // Viewport content container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            when (viewportMode) {
                WebViewportMode.MOBILE -> {
                    // Mobile device frame emulator
                    Surface(
                        modifier = Modifier
                            .width(390.dp)
                            .fillMaxHeight()
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        shape = RoundedCornerShape(28.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(4.dp, Color(0xFF334155)),
                        shadowElevation = 16.dp
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Phone top notch bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E293B))
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(70.dp)
                                        .height(4.dp)
                                        .background(Color(0xFF64748B), RoundedCornerShape(2.dp))
                                )
                            }

                            // WebView
                            AndroidView(
                                factory = { ctx ->
                                    createConfiguredWebView(ctx, "mobile") { wb ->
                                        webViewInstance = wb
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                WebViewportMode.DESKTOP -> {
                    // Desktop view container
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                createConfiguredWebView(ctx, "desktop") { wb ->
                                    webViewInstance = wb
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                WebViewportMode.ADAPTIVE -> {
                    // Adaptive fill container
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                createConfiguredWebView(ctx, "adaptive") { wb ->
                                    webViewInstance = wb
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun createConfiguredWebView(
    context: android.content.Context,
    mode: String,
    onWebViewCreated: (WebView) -> Unit
): WebView {
    return WebView(context).apply {
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            useWideViewPort = true
            loadWithOverviewMode = true
            builtInZoomControls = true
            displayZoomControls = false
            cacheMode = WebSettings.LOAD_DEFAULT

            if (mode == "desktop") {
                userAgentString = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            }
        }

        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return false
            }
        }

        webChromeClient = WebChromeClient()

        loadUrl("file:///android_asset/web/index.html")
        onWebViewCreated(this)
    }
}
