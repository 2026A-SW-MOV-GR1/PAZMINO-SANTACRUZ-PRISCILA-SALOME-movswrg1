package com.example.netflixclone.adapter

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.netflixclone.R
import com.example.netflixclone.model.Movie

class NewsAdapter(
    private val onNewsClick: (Movie) -> Unit
) : ListAdapter<Movie, NewsAdapter.NewsViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<Movie>() {
        override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem == newItem
        }
    }

    inner class NewsViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
    ) {
        private val imgNews: ImageView = itemView.findViewById(R.id.imgNews)
        private val tvNewsTitle: TextView = itemView.findViewById(R.id.tvNewsTitle)
        private val tvNewsDescription: TextView = itemView.findViewById(R.id.tvNewsDescription)

        fun bind(movie: Movie) {
            tvNewsTitle.text = movie.title
            tvNewsDescription.text = "${movie.subtitle} • ${movie.description}"

            val placeholder = ColorDrawable(itemView.context.getColor(R.color.netflix_dark))
            val error = ColorDrawable(itemView.context.getColor(R.color.netflix_card))

            Glide.with(itemView.context)
                .load(movie.imageUrl)
                .centerCrop()
                .placeholder(placeholder)
                .error(error)
                .into(imgNews)

            itemView.setOnClickListener {
                itemView.animate()
                    .alpha(0.75f)
                    .setDuration(80)
                    .withEndAction {
                        itemView.animate()
                            .alpha(1f)
                            .setDuration(80)
                            .start()
                    }
                    .start()

                onNewsClick(movie)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        return NewsViewHolder(parent)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}