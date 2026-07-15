package com.example.netflixclone.model

data class Movie(
    val id: Int,
    val title: String,
    val subtitle: String,
    val description: String,
    val imageUrl: String = "",
    val videoRawName: String = "",
    val isTop10: Boolean = false,
    val progress: Int = 0
)