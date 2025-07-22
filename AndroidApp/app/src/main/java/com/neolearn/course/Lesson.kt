package com.neolearn.course


data class LessonActivity(
    val fileType: String,
    val activityType: String,
    val comment: String?,
    val path: String,
    val completionCriteria: List<String>?,
)


data class Lesson(
    val id: String,
    val title: String,
    val description: String?,
    val objectives: List<String>,
    val activities: List<LessonActivity>,

    var locatedAt: String?,
)
