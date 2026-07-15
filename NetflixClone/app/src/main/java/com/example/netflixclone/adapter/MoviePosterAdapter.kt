package com.example.netflixclone.adapter

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.netflixclone.R
import com.example.netflixclone.model.Movie

class MoviePosterAdapter(
    private val onMovieClick: (Movie) -> Unit
) : ListAdapter<Movie, MoviePosterAdapter.MovieViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<Movie>() {
        override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem == newItem
        }
    }

    inner class MovieViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_movie_poster, parent, false)
    ) {
        private val imgPoster: ImageView = itemView.findViewById(R.id.imgPoster)
        private val tvPosterTitle: TextView = itemView.findViewById(R.id.tvPosterTitle)
        private val tvPosterSubtitle: TextView = itemView.findViewById(R.id.tvPosterSubtitle)
        private val tvTopBadge: TextView = itemView.findViewById(R.id.tvTopBadge)

        fun bind(movie: Movie) {
            tvPosterTitle.text = movie.title
            tvPosterSubtitle.text = movie.subtitle

            val placeholder = ColorDrawable(itemView.context.getColor(R.color.netflix_dark))
            val error = ColorDrawable(itemView.context.getColor(R.color.netflix_card))

            Glide.with(itemView.context)
                .load(movie.imageUrl)
                .centerCrop()
                .placeholder(placeholder)
                .error(error)
                .into(imgPoster)

            tvTopBadge.visibility = if (movie.isTop10) View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                itemView.animate()
                    .scaleX(0.94f)
                    .scaleY(0.94f)
                    .setDuration(80)
                    .withEndAction {
                        itemView.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(80)
                            .start()
                    }
                    .start()

                onMovieClick(movie)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        return MovieViewHolder(parent)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}