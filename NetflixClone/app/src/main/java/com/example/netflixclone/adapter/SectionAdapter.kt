package com.example.netflixclone.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.netflixclone.R
import com.example.netflixclone.model.Movie
import com.example.netflixclone.model.MovieSection

class SectionAdapter(
    private val onMovieClick: (Movie) -> Unit
) : ListAdapter<MovieSection, SectionAdapter.SectionViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<MovieSection>() {
        override fun areItemsTheSame(oldItem: MovieSection, newItem: MovieSection): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MovieSection, newItem: MovieSection): Boolean {
            return oldItem == newItem
        }
    }

    inner class SectionViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_section, parent, false)
    ) {
        private val tvSectionTitle: TextView = itemView.findViewById(R.id.tvSectionTitle)
        private val rvMovies: RecyclerView = itemView.findViewById(R.id.rvMovies)
        private val moviePosterAdapter = MoviePosterAdapter(onMovieClick)

        init {
            rvMovies.layoutManager = LinearLayoutManager(
                itemView.context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            rvMovies.adapter = moviePosterAdapter
            rvMovies.setHasFixedSize(true)
        }

        fun bind(section: MovieSection) {
            tvSectionTitle.text = section.title
            moviePosterAdapter.submitList(section.movies)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionViewHolder {
        return SectionViewHolder(parent)
    }

    override fun onBindViewHolder(holder: SectionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}