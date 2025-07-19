package com.neolearn

import android.content.Context
import androidx.compose.runtime.Composable
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun SecondScreen() {

    fun buildMaterial(context: Context, sourceFileName: String): String {
        val header = context.assets.open("header.html").bufferedReader().use { it.readText() }
        val body = context.assets.open("materials/$sourceFileName").bufferedReader().use { it.readText() }
        val footer = context.assets.open("footer.html").bufferedReader().use { it.readText() }

        return header + body + footer
    }

//    val sourceFile = "matem 6/010 Aryphmetyka/010 Naturalni chysla/010_Explanation what is that.html"
    val sourceFile = "matem 6/010 Aryphmetyka/010 Naturalni chysla/010CheckLearn001 what is that.html"
    val fullHtml = buildMaterial(LocalContext.current, sourceFile)

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                webViewClient = WebViewClient()
                loadDataWithBaseURL(null, fullHtml, "text/html", "utf-8", null)
            }
        }
    )
}