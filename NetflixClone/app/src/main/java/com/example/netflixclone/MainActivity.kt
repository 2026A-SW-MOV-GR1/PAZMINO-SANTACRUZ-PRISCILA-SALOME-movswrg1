package com.example.netflixclone

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.netflixclone.adapter.ChipAdapter
import com.example.netflixclone.adapter.NewsAdapter
import com.example.netflixclone.adapter.SectionAdapter
import com.example.netflixclone.data.FakeNetflixData
import com.example.netflixclone.model.CategoryChip
import com.example.netflixclone.model.Movie
import com.example.netflixclone.model.MovieSection
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private lateinit var chipAdapter: ChipAdapter
    private lateinit var sectionAdapter: SectionAdapter
    private lateinit var newsAdapter: NewsAdapter

    private lateinit var btnPlay: MaterialButton
    private lateinit var btnMyList: MaterialButton

    private var selectedMovie: Movie? = null
    private val myListIds = mutableSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupAdapters()
        setupRecyclerViews()
        setupButtons()
        loadFakeData()
    }

    private fun setupAdapters() {
        chipAdapter = ChipAdapter { chip ->
            handleChipClick(chip)
        }

        sectionAdapter = SectionAdapter { movie ->
            updateHero(movie)
            Toast.makeText(this, "${movie.title} seleccionado", Toast.LENGTH_SHORT).show()
        }

        newsAdapter = NewsAdapter { movie ->
            updateHero(movie)
            Toast.makeText(this, "${movie.subtitle} seleccionado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerViews() {
        val rvTopMenu = findViewById<RecyclerView>(R.id.rvTopMenu)
        val rvSections = findViewById<RecyclerView>(R.id.rvSections)
        val rvNews = findViewById<RecyclerView>(R.id.rvNews)

        rvTopMenu.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        rvTopMenu.adapter = chipAdapter
        rvTopMenu.setHasFixedSize(true)

        rvSections.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        rvSections.adapter = sectionAdapter
        rvSections.setHasFixedSize(false)

        rvNews.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        rvNews.adapter = newsAdapter
        rvNews.setHasFixedSize(false)
    }

    private fun setupButtons() {
        btnPlay = findViewById(R.id.btnPlay)
        btnMyList = findViewById(R.id.btnMyList)

        btnPlay.setOnClickListener {
            animateButton(btnPlay)

            val movie = selectedMovie

            if (movie != null) {
                openPlayer(movie)
            } else {
                Toast.makeText(this, "Selecciona una película", Toast.LENGTH_SHORT).show()
            }
        }

        btnMyList.setOnClickListener {
            animateButton(btnMyList)

            val movie = selectedMovie

            if (movie != null) {
                toggleMyList(movie)
            } else {
                Toast.makeText(this, "Selecciona una película", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadFakeData() {
        chipAdapter.submitList(FakeNetflixData.chips)
        sectionAdapter.submitList(FakeNetflixData.sections)
        newsAdapter.submitList(FakeNetflixData.news)

        val firstMovie = getAllMovies().firstOrNull { it.title == "Stranger Things" }
            ?: getAllMovies().firstOrNull()

        if (firstMovie != null) {
            updateHero(firstMovie)
        }
    }

    private fun updateHero(movie: Movie) {
        selectedMovie = movie

        val imgHero = findViewById<ImageView>(R.id.imgHero)
        val tvHeroTitle = findViewById<TextView>(R.id.tvHeroTitle)
        val tvHeroSubtitle = findViewById<TextView>(R.id.tvHeroSubtitle)

        tvHeroTitle.text = movie.title
        tvHeroSubtitle.text = "${movie.subtitle} • Recomendado para ti"

        val placeholder = ColorDrawable(getColor(R.color.netflix_dark))
        val error = ColorDrawable(getColor(R.color.netflix_card))

        Glide.with(this)
            .load(movie.imageUrl)
            .centerCrop()
            .placeholder(placeholder)
            .error(error)
            .into(imgHero)

        updateMyListButtonState(movie)
    }

    private fun openPlayer(movie: Movie) {
        if (movie.videoRawName.isBlank()) {
            Toast.makeText(
                this,
                "Este contenido no tiene demo disponible",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        try {
            val intent = Intent(this, PlayerActivity::class.java).apply {
                putExtra("title", movie.title)
                putExtra("subtitle", movie.subtitle)
                putExtra("description", movie.description)
                putExtra("imageUrl", movie.imageUrl)
                putExtra("videoRawName", movie.videoRawName)
            }

            startActivity(intent)

        } catch (e: Exception) {
            Toast.makeText(
                this,
                "No se pudo abrir el reproductor: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun toggleMyList(movie: Movie) {
        if (myListIds.contains(movie.id)) {
            myListIds.remove(movie.id)
            Toast.makeText(this, "${movie.title} eliminado de Mi lista", Toast.LENGTH_SHORT).show()
        } else {
            myListIds.add(movie.id)
            Toast.makeText(this, "${movie.title} agregado a Mi lista", Toast.LENGTH_SHORT).show()
        }

        updateMyListButtonState(movie)
    }

    private fun updateMyListButtonState(movie: Movie) {
        if (!::btnMyList.isInitialized) return

        if (myListIds.contains(movie.id)) {
            btnMyList.text = "✓ En mi lista"
            btnMyList.backgroundTintList =
                ColorStateList.valueOf(getColor(R.color.netflix_red))
        } else {
            btnMyList.text = "+ Mi lista"
            btnMyList.backgroundTintList =
                ColorStateList.valueOf(getColor(R.color.netflix_gray_dark))
        }
    }

    private fun handleChipClick(chip: CategoryChip) {
        val allMovies = getAllMovies()

        when (chip.name) {
            "Series" -> {
                val filtered = allMovies.filter {
                    it.title != "Avatar"
                }

                sectionAdapter.submitList(
                    listOf(
                        MovieSection(201, "Series recomendadas", filtered),
                        MovieSection(202, "También te puede gustar", filtered.shuffled())
                    )
                )

                Toast.makeText(this, "Filtro aplicado: Series", Toast.LENGTH_SHORT).show()
            }

            "Películas" -> {
                val filtered = allMovies.filter {
                    it.title in listOf("Avatar", "The Witcher", "One Piece")
                }

                sectionAdapter.submitList(
                    listOf(
                        MovieSection(301, "Películas destacadas", filtered),
                        MovieSection(302, "Aventura y fantasía", filtered.shuffled())
                    )
                )

                Toast.makeText(this, "Filtro aplicado: Películas", Toast.LENGTH_SHORT).show()
            }

            "Categorías" -> {
                sectionAdapter.submitList(FakeNetflixData.sections.toList())
                Toast.makeText(this, "Mostrando todas las categorías", Toast.LENGTH_SHORT).show()
            }

            "Mi lista" -> {
                val savedMovies = allMovies.filter {
                    myListIds.contains(it.id)
                }

                if (savedMovies.isEmpty()) {
                    Toast.makeText(this, "Tu lista está vacía", Toast.LENGTH_SHORT).show()
                } else {
                    sectionAdapter.submitList(
                        listOf(
                            MovieSection(401, "Mi lista", savedMovies)
                        )
                    )

                    Toast.makeText(this, "Mostrando Mi lista", Toast.LENGTH_SHORT).show()
                }
            }

            "Novedades" -> {
                val scrollView = findViewById<NestedScrollView>(R.id.main)
                val newsHeader = findViewById<TextView>(R.id.tvNewsHeader)

                scrollView.post {
                    scrollView.smoothScrollTo(0, newsHeader.top)
                }

                Toast.makeText(this, "Bajando a novedades", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getAllMovies(): List<Movie> {
        return FakeNetflixData.sections
            .flatMap { it.movies }
            .distinctBy { it.id }
    }

    private fun animateButton(button: MaterialButton) {
        button.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(80)
            .withEndAction {
                button.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(80)
                    .start()
            }
            .start()
    }
}