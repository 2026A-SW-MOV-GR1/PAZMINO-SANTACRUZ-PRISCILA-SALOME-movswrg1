package com.example.netflixclone.model

data class MovieSection(
    val id: Int,
    val title: String,
    val movies: List<Movie>
)