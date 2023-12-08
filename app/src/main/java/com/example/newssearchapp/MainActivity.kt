package com.example.newssearchapp

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchIcon: ImageView
    private lateinit var searchField: EditText
    private val apiKey = "710119f4520a4c25b4ab12e46322e7db"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize TextToSpeech
        textToSpeech = TextToSpeech(this, this)

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.newsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize the adapter with an empty list
        newsAdapter = NewsAdapter(emptyList(), onSpeakClick = { text ->
            speakOut(text)
        }, onViewClick = { url ->
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra(WebViewActivity.EXTRA_URL, url)
            startActivity(intent)
        })
        recyclerView.adapter = newsAdapter

        // Initialize the search field and icon
        searchField = findViewById(R.id.searchField)
        searchIcon = findViewById(R.id.searchIcon)
        searchIcon.setOnClickListener {
            val searchQuery = searchField.text.toString()
            if (searchQuery.isNotEmpty()) {
                fetchNews(searchQuery, apiKey)
            } else {
                Toast.makeText(this, "Please enter a search query", Toast.LENGTH_SHORT).show()
            }
        }

        // Fetch news with a default keyword on app start
        val defaultKeyword = "technology"
        fetchNews(defaultKeyword, apiKey)
    }


    private fun speakOut(text: String) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech.setLanguage(Locale.US)
            if (result != TextToSpeech.LANG_COUNTRY_AVAILABLE && result != TextToSpeech.LANG_AVAILABLE) {
                //TODO Handle error in setting the language
            }
        } else {
            //TODO Handle initialization failure
        }
    }

    override fun onDestroy() {
        // Shut down TextToSpeech
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onDestroy()
    }



    // Retrofit and API integration
    private fun fetchNews(keyword: String, apiKey: String) {
        // Create a Retrofit instance
        val retrofit = Retrofit.Builder()
            .baseUrl("https://newsapi.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Create an instance of the API interface
        val newsApi = retrofit.create(NewsApiService::class.java)

        // Format the current date to pass to the API
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Enqueue the network call
        newsApi.getNews(keyword, currentDate, language = "en", apiKey).enqueue(object : Callback<NewsResponse> {
            override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                if (response.isSuccessful) {
                    val newsArticles = response.body()?.articles ?: emptyList()
                    // Update the adapter with the new data
                    newsAdapter.updateData(newsArticles)
                } else {
                    // Handle unsuccessful responses (e.g., API limit reached, server issues)
                    Toast.makeText(this@MainActivity, "Failed to fetch news: ${response.errorBody()?.string()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                // Handle network failures or xceptions during the API call
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}

// Retrofit API service interface
interface NewsApiService {
    @GET("v2/everything")
    fun getNews(
        @Query("q") keyword: String,
        @Query("from") from: String,
        @Query("language") language: String,
        @Query("apiKey") apiKey: String
    ): Call<NewsResponse>
}

// Data models
data class NewsResponse(
    val status: String,
    val totalResults: Int,
    val articles: List<NewsArticle>
)

data class NewsArticle(
    val source: Source,
    val author: String?,
    val title: String,
    val description: String,
    val url: String,
    val urlToImage: String?,
    val publishedAt: String,
    val content: String?
)

data class Source(
    val id: String?,
    val name: String
)

