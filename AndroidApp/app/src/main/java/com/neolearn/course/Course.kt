package com.neolearn.course

data class CourseMetadata(
    val author: String,
    val version: String,
    val duration: String,
    val tags: List<String>,
)

data class Course(
    val id: String,
    val title: String,
    val description: String,
    val grade: Int,
    val subject: String,
    val language: String,
    val metadata: CourseMetadata,

    var locatedAt: String?,
)
