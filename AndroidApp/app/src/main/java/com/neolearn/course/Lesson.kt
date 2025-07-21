package com.neolearn.course


data class Material(
    val fileType: String,
    val materialType: String,
    val title: String,
    val path: String,
)


data class Lesson(
    val id: String,
    val title: String,
    val description: String,

    val materials: List<Material>,

    var locatedAt: String?,
)
