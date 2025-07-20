package com.neolearn.course

data class Course(
    val id: String,
    val title: String,
    val description: String,
    val grade: Int,
    val subject: String,
    val language: String,
    val metadata: Metadata,

    var locatedAt: String?
)
