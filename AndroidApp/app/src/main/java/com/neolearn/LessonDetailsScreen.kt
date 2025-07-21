package com.neolearn

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
                unit?.let {
                    Text(
                        text = module!!.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = it.title,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    Text(
                        text = it.description,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Уроки:",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    lessons.forEach { lesson ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable { onUnitClick(course!!, module!!, unit!!, lesson) },
                            shape = RoundedCornerShape(8.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = lesson.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = lesson.description,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
