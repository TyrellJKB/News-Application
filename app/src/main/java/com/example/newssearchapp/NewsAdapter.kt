package com.example.newssearchapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NewsAdapter(private var articles: List<NewsArticle>, private val onSpeakClick: (String) -> Unit, private val onViewClick: (String) -> Unit) : RecyclerView.Adapter<NewsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val newsTitle: TextView = view.findViewById(R.id.newsTitle)
        val speakButton: Button = view.findViewById(R.id.speakButton)
        val viewButton: Button = view.findViewById(R.id.viewButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.news_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val article = articles[position]
        holder.newsTitle.text = article.title
        holder.speakButton.setOnClickListener { onSpeakClick(article.description) }
        holder.viewButton.setOnClickListener { onViewClick(article.url) }
    }

    override fun getItemCount() = articles.size

    // Update the articles list and notify the adapter of the change
    fun updateData(newArticles: List<NewsArticle>) {
        articles = newArticles
        notifyDataSetChanged()
    }
}

data class Article(
    val title: String,
    val description: String,
    val url: String
)
