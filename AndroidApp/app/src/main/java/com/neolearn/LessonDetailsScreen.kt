package com.neolearn

import CourseLoader.prepareActivity
import android.app.Activity
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
import androidx.compose.ui.draw.alpha
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
import com.google.gson.Gson
import com.neolearn.course.AnswerData
import com.neolearn.course.TestDataParsing
import com.neolearn.course.UserAnswer
import com.neolearn.course.Variant
import java.io.File
import java.io.FileOutputStream
import java.util.Optional


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
    var loadingCourseError by remember { mutableStateOf(false) }

    var showNextButton by remember { mutableStateOf(true) }
    val webViewState = remember { mutableStateOf<WebView?>(null) }

    val testDataParser = TestDataParsing()

    LaunchedEffect(coursePath) {
        try {
            course = CourseLoader.loadCourse(context, coursePath)
            module = CourseLoader.loadModule(context, coursePath, modulePath)
            unit = CourseLoader.loadUnit(context, coursePath, modulePath, unitPath)
            lesson = CourseLoader.loadLesson(context, coursePath, modulePath, unitPath, lessonPath)

            dataLoaded = true
        } catch (e: Exception) {
            Log.e(this.javaClass.name, "Course structure does have an error", e)
            loadingCourseError = true
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        if (loadingCourseError) {
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

                        class WebAppBridge {
                            @JavascriptInterface
                            fun showNextBtn(showBtn: Boolean) {
                                showNextButton = showBtn;
                                Log.i(this.javaClass.name, "showNextBtn($showBtn)")
                            }

                            @JavascriptInterface
                            fun showPageFromStart() {
                                Log.i(this.javaClass.name, "showPageFromStart()")
                                (context as Activity).runOnUiThread {
                                    webViewState.value?.evaluateJavascript(
                                        "window.scrollTo(0, 0);",
                                        null
                                    )
                                }
                            }

                            @JavascriptInterface
                            fun sendMaterialTestData(message: String): String {
                                val testData = testDataParser.getTestDataFromString(fullHtml)

                                val response: String = try {
                                    var totalAcrossAllQuestion = 0
                                    var totalCorrectAnswers = 0
                                    var userAnswers = mutableListOf<UserAnswer>()

                                    val gson = Gson()
                                    val messageJson = gson.fromJson(message, AnswerData::class.java)

                                    val selectedVariant: Optional<Variant> = testData.stream().filter{ variant -> variant.variantId == "variant${messageJson.variantId}"}.findFirst()

                                    if (selectedVariant.isEmpty) throw IllegalStateException("Variant is not found.")

                                    for (question in selectedVariant.get().questions) {
                                        if (question.type == "radio" || question.type == "checkbox") {
                                            if (!messageJson.answers.containsKey(question.questionId)) {
                                                Log.e(
                                                    this.javaClass.name,
                                                    "Request from webPage does not contain answers for give question id."
                                                )
                                                continue
                                            }

                                            var answersForQuestion = messageJson.answers[question.questionId]

                                            if (answersForQuestion == null || answersForQuestion.isEmpty()) {
                                                Log.e(this.javaClass.name, "Request from webPage does not contain answers for given question.")
                                                continue
                                            }

                                            if ((question.type == "radio") && (answersForQuestion.size > 1)) {
                                                Log.e(
                                                    this.javaClass.name,
                                                    "Request from webPage does contain more than 1 answers for given question."
                                                )
                                                continue
                                            }

                                            var takenPoints = 0
                                            var possiblePoints = 0
                                            val userAnswer = UserAnswer(question, messageJson.answers[question.questionId]!!)

                                            for (options in question.options) {
                                                if (options.dataCorrect) possiblePoints += 1

                                                for (answer in messageJson.answers[question.questionId]!!) {
                                                    if (answer == options.value) {
                                                        if (options.dataCorrect) {
                                                            takenPoints += 1
                                                        }
                                                    }
                                                }
                                            }

                                            totalAcrossAllQuestion += 1
                                            if (takenPoints == possiblePoints) {
                                                totalCorrectAnswers += 1
                                            }
                                            userAnswer.points = takenPoints.toFloat()
                                            userAnswer.maxPoints = possiblePoints.toFloat()
                                            userAnswers.add(userAnswer);
                                        }
                                    }

                                    Log.i(this.javaClass.name, message)
                                    """{
                                        |   "result": ${totalCorrectAnswers},
                                        |   "max": ${totalAcrossAllQuestion},
                                        |   "passed": "${totalCorrectAnswers > totalAcrossAllQuestion * 0.8}",
                                        |   "userAnswers": "${gson.toJson(userAnswers)}"
                                        | }""".trimMargin()
                                }
                                catch (_: Exception) {
                                    """{
                                        |   "result": 0,
                                        |   "max": 0,
                                        |   "passed": "false"
                                        | }""".trimMargin()
                                }

                                return response
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
                                    loadDataWithBaseURL(
                                        baseUrl,
                                        fullHtml,
                                        "text/html",
                                        "utf-8",
                                        null
                                    )
                                    webViewState.value = this
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
                            .height(56.dp)
                            .alpha(if (showNextButton) 1f else 0f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(text = "Наступний крок")
                    }
                }
            }
        }
    }
}
