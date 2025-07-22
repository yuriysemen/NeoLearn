package com.neolearn

import android.webkit.WebView
import android.webkit.WebViewClient
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.neolearn.course.Course
import com.neolearn.course.Module
import com.neolearn.course.CourseUnit
import com.neolearn.course.Lesson
import com.neolearn.course.LessonActivity
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.graphics.Color
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException


private fun readAsset(context: Context, path: String): String {
    return context.assets.open(path).bufferedReader().use { it.readText() }
}

fun Color.toHex(): String {
    return String.format("#%02X%02X%02X",
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt())
}

fun copyAssetToCache(context: Context, assetName: String, outputName: String): File {
    val outputFile = File(context.cacheDir, outputName)

    context.assets.open(assetName).use { input ->
        FileOutputStream(outputFile).use { output ->
            input.copyTo(output)
        }
    }

    return outputFile
}

@Composable
fun LessonDetailsScreen(
    coursePath: String,
    modulePath: String,
    unitPath: String,
    lessonPath: String,
    context: Context = LocalContext.current
) {
    val scrollState = rememberScrollState()

    var course by remember { mutableStateOf<Course?>(null) }
    var module by remember { mutableStateOf<Module?>(null) }
    var unit by remember { mutableStateOf<CourseUnit?>(null) }
    var lesson by remember { mutableStateOf<Lesson?>(null) }
    var activity by remember { mutableStateOf<LessonActivity?>(null) }

    var dataLoaded by remember { mutableStateOf(false) }
    var loadingCourceError by remember { mutableStateOf(false) }

    LaunchedEffect(coursePath) {
        try {
            course = CourseLoader.loadCourse(context, coursePath)
            module = CourseLoader.loadModule(context, coursePath, modulePath)
            unit = CourseLoader.loadUnit(context, coursePath, modulePath, unitPath)
            lesson = CourseLoader.loadLesson(context, coursePath, modulePath, unitPath, lessonPath)

            activity = lesson!!.activities.firstOrNull()

            dataLoaded = true
        } catch (e: Exception) {
            Log.e(this.javaClass.name, "Course structure does have an error", e)
            loadingCourceError = true
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        if (loadingCourceError) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Error loading of the data.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
        else if (!dataLoaded)
            Text(
                text = "Loading the data...",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = module!!.title + " :: " + lesson!!.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (activity != null) {
                    val header = readAsset(context, "header.html")
                    val footer = readAsset(context, "footer.html")
                    val body = readAsset(context, "materials/${lesson!!.locatedAt}/${activity!!.path}")

                    var css = copyAssetToCache(context, "katex/katex.min.css", "katex.min.css")
                    var js = copyAssetToCache(context, "katex/katex.min.js", "katex.min.js")
                    var ar = copyAssetToCache(context, "katex/auto-render.min.js", "auto-render.min.js")

                    val header2 = header.replace("{{background}}", MaterialTheme.colorScheme.primaryContainer.toHex())
                        .replace("{{text}}", MaterialTheme.colorScheme.onPrimaryContainer.toHex())
                        .replace("{{math}}", MaterialTheme.colorScheme.secondary.toHex())
                        .replace("{{infoBackground}}", "rgba(255,255,255,0.1)")
                        .replace("{{infoBorder}}", "#fff")
                        .replace("{{info}}", "#03a9f4")
                        .replace("{{warning}}", "#ff9800")
                        .replace("{{task}}", "#4caf50")
                        .replace("{{question}}", "#9575cd")
                        .replace("{{reference}}", "#26c6da")
                        .replace("{{hint}}", "#aed581")
                        .replace("{{term}}", "#f06292")

                    val fullHtml =  header2 + body + footer
                    val baseUrl = context.cacheDir.toURI().toString()

                    val cacheFiles: Array<File> = context.cacheDir.listFiles() ?: emptyArray()

                    for (file in cacheFiles) {
                        Log.d("CacheFiles", "File: ${file.name}")
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        AndroidView(
                            factory = { context ->
                                WebView(context).apply {
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    settings.allowFileAccess = true
                                    settings.allowContentAccess = true
                                    webViewClient = WebViewClient()
                                    loadDataWithBaseURL(
                                        baseUrl,                  // щоб WebView знайшов style.css
                                        fullHtml,                 // HTML контент
                                        "text/html",    // mime type
                                        "utf-8",         // encoding
                                        null             // history url
                                    )
                                }
                            },
                            update = { webView ->
                                webView.loadDataWithBaseURL(
                                    baseUrl,
                                    fullHtml,
                                    "text/html",
                                    "utf-8",
                                    null)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Button(
                    onClick = {
                        activity = lesson!!.activities[1]
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(text = "Почати урок")
                }
            }
        }
    }
}
