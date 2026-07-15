package com.example.netflixclone.data

import com.example.netflixclone.model.CategoryChip
import com.example.netflixclone.model.Movie
import com.example.netflixclone.model.MovieSection

object FakeNetflixData {

    val chips = listOf(
        CategoryChip(1, "Series"),
        CategoryChip(2, "Películas"),
        CategoryChip(3, "Categorías"),
        CategoryChip(4, "Mi lista"),
        CategoryChip(5, "Novedades")
    )

    private val movies = listOf(
        Movie(
            id = 1,
            title = "Stranger Things",
            subtitle = "Ciencia ficción",
            description = "Un grupo de amigos descubre secretos extraños en su ciudad.",
            imageUrl = "https://image.tmdb.org/t/p/w500/49WJfeN0moxb9IPfGn8AIqMGskD.jpg",
            videoRawName = "stranger_things",
            isTop10 = true,
            progress = 65
        ),
        Movie(
            id = 2,
            title = "Dark",
            subtitle = "Misterio",
            description = "Cuatro familias enfrentan viajes en el tiempo y secretos oscuros.",
            imageUrl = "https://image.tmdb.org/t/p/w500/apbrbWs8M9lyOpJYU5WXrpFbk1Z.jpg",
            videoRawName = "dark",
            isTop10 = true,
            progress = 40
        ),
        Movie(
            id = 3,
            title = "The Witcher",
            subtitle = "Fantasía",
            description = "Un cazador de monstruos busca su lugar en un mundo peligroso.",
            imageUrl = "https://image.tmdb.org/t/p/w500/zrPpUlehQaBf8YX2NrVrKK8IEpf.jpg",
            videoRawName = "",
            progress = 25
        ),
        Movie(
            id = 4,
            title = "La Casa de Papel",
            subtitle = "Acción",
            description = "Un grupo de atracadores ejecuta un plan imposible.",
            imageUrl = "https://image.tmdb.org/t/p/w500/reEMJA1uzscCbkpeRJeTT2bjqUp.jpg",
            videoRawName = "",
            isTop10 = true,
            progress = 80
        ),
        Movie(
            id = 5,
            title = "Wednesday",
            subtitle = "Comedia oscura",
            description = "Wednesday Addams investiga misterios en su nueva escuela.",
            imageUrl = "https://image.tmdb.org/t/p/w500/9PFonBhy4cQy7Jz20NpMygczOkv.jpg",
            videoRawName = "",
            progress = 15
        ),
        Movie(
            id = 6,
            title = "You",
            subtitle = "Suspenso",
            description = "Una historia de obsesión, secretos y manipulación.",
            imageUrl = "https://image.tmdb.org/t/p/w500/oANi0vEE92nuijiZQgPZ88FSxqQ.jpg",
            videoRawName = "",
            progress = 50
        ),
        Movie(
            id = 7,
            title = "One Piece",
            subtitle = "Aventura",
            description = "Un joven pirata busca el tesoro más grande del mundo.",
            imageUrl = "https://image.tmdb.org/t/p/w500/rVX05xRKS5JhEYQFObCi4lAnZT4.jpg",
            videoRawName = "",
            isTop10 = true,
            progress = 35
        ),
        Movie(
            id = 8,
            title = "Avatar",
            subtitle = "Fantasía",
            description = "Una aventura visual en un mundo lleno de vida.",
            imageUrl = "https://image.tmdb.org/t/p/w500/jRXYjXNq0Cs2TcJjLkki24MLp7u.jpg",
            videoRawName = "avatar",
            progress = 0
        ),
        Movie(
            id = 9,
            title = "El Juego del Calamar",
            subtitle = "Drama",
            description = "Personas endeudadas compiten en juegos mortales.",
            imageUrl = "https://image.tmdb.org/t/p/w500/dDlEmu3EZ0Pgg93K2SVNLCjCSvE.jpg",
            videoRawName = "squid_game",
            isTop10 = true,
            progress = 90
        ),
        Movie(
            id = 10,
            title = "Breaking Bad",
            subtitle = "Drama criminal",
            description = "Un profesor de química entra al mundo del crimen.",
            imageUrl = "https://image.tmdb.org/t/p/w500/ztkUQFLlC19CCMYHW9o1zWhJRNq.jpg",
            videoRawName = "",
            progress = 70
        )
    )

    val sections = listOf(
        MovieSection(
            id = 1,
            title = "Continuar viendo",
            movies = movies.shuffled()
        ),
        MovieSection(
            id = 2,
            title = "Top 10 en Ecuador",
            movies = movies.filter { it.isTop10 }
        ),
        MovieSection(
            id = 3,
            title = "Tendencias ahora",
            movies = movies.shuffled()
        ),
        MovieSection(
            id = 4,
            title = "Recomendadas para ti",
            movies = movies.shuffled()
        )
    )

    val news = listOf(
        Movie(
            id = 101,
            title = "Nueva temporada disponible",
            subtitle = "Stranger Things",
            description = "Continúa viendo el misterio que atrapó a millones de usuarios.",
            imageUrl = "https://image.tmdb.org/t/p/w500/49WJfeN0moxb9IPfGn8AIqMGskD.jpg",
            videoRawName = "stranger_things"
        ),
        Movie(
            id = 102,
            title = "Estreno recomendado",
            subtitle = "Avatar",
            description = "Una aventura visual recomendada para los amantes de la fantasía.",
            imageUrl = "https://image.tmdb.org/t/p/w500/jRXYjXNq0Cs2TcJjLkki24MLp7u.jpg",
            videoRawName = "avatar"
        ),
        Movie(
            id = 103,
            title = "Popular esta semana",
            subtitle = "El Juego del Calamar",
            description = "Un drama intenso que sigue entre los contenidos más vistos.",
            imageUrl = "https://image.tmdb.org/t/p/w500/dDlEmu3EZ0Pgg93K2SVNLCjCSvE.jpg",
            videoRawName = "squid_game"
        ),
        Movie(
            id = 104,
            title = "Porque viste misterio",
            subtitle = "Dark",
            description = "Una recomendación basada en tus gustos recientes.",
            imageUrl = "https://image.tmdb.org/t/p/w500/apbrbWs8M9lyOpJYU5WXrpFbk1Z.jpg",
            videoRawName = "dark"
        )
    )
}