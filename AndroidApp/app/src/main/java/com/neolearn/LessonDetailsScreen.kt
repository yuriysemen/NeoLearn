package com.neolearn

import CourseLoader.prepareActivity
import android.webkit.WebView
import android.webkit.WebViewClient
import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.neolearn.course.Course
import com.neolearn.course.Module
import com.neolearn.course.CourseUnit
import com.neolearn.course.Lesson
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import java.io.File
import java.io.FileOutputStream


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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LessonDetailsScreen(
    coursePath: String,
    modulePath: String,
    unitPath: String,
    lessonPath: String,
    context: Context = LocalContext.current
) {
    var course by remember { mutableStateOf<Course?>(null) }
    var module by remember { mutableStateOf<Module?>(null) }
    var unit by remember { mutableStateOf<CourseUnit?>(null) }
    var lesson by remember { mutableStateOf<Lesson?>(null) }
    var activityNumber by remember { mutableIntStateOf(0) }

    var dataLoaded by remember { mutableStateOf(false) }
    var loadingCourceError by remember { mutableStateOf(false) }

    LaunchedEffect(coursePath) {
        try {
            course = CourseLoader.loadCourse(context, coursePath)
            module = CourseLoader.loadModule(context, coursePath, modulePath)
            unit = CourseLoader.loadUnit(context, coursePath, modulePath, unitPath)
            lesson = CourseLoader.loadLesson(context, coursePath, modulePath, unitPath, lessonPath)

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
                    .padding(24.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = module!!.title + " :: " + lesson!!.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (lesson != null) {
                    val activityPath = "materials/${lesson!!.locatedAt}/${lesson!!.activities[activityNumber].path}"
                    val (fullHtml, baseUrl) = prepareActivity(context, activityPath);

                    Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        data class QuizResult(val correct: Int, val total: Int, val passed: Boolean)
                        class WebAppBridge {

                            @JavascriptInterface
                            fun sendMessage(message: String) {
                                // message приходить у форматі JSON:
                                // {"correct":2,"total":2,"passed":true}

                                Log.e(this.javaClass.name, message)
                            }
                        }
                        AndroidView(
                            factory = { context ->
                                WebView(context).apply {
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    settings.allowFileAccess = true
                                    settings.allowContentAccess = true
                                    settings.setSupportZoom(false)
                                    settings.builtInZoomControls = false
                                    settings.displayZoomControls = false
                                    webViewClient = WebViewClient()
                                    addJavascriptInterface(WebAppBridge(), "AndroidBridge")
//                                    evaluateJavascript("sendMessage") { result ->
//                                        Log.d("WebView", "JS повернув: $result")
//                                    }
                                    loadDataWithBaseURL(
                                        baseUrl,
                                        fullHtml,
                                        "text/html",
                                        "utf-8",
                                        null
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
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInteropFilter { false }
                        )
                    }
                }

                if (activityNumber <= lesson!!.activities.size) {
                    Button(
                        onClick = {
                            if (activityNumber != lesson!!.activities.size)
                                activityNumber += 1
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(text = "Наступний крок")
                    }
                }
            }
        }
    }
}
