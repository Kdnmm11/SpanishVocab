package com.example.spanishvocab

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spanishvocab.adapter.WordAdapter
import com.example.spanishvocab.data.VocabData
import com.example.spanishvocab.data.Word
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FavoritesActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var studyFab: FloatingActionButton
    private lateinit var wordAdapter: WordAdapter
    private var favoriteWords = mutableListOf<Word>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        setupViews()
        loadFavorites()
        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        loadFavorites()
        wordAdapter.updateWords(favoriteWords)
        updateEmptyView()
    }

    private fun setupViews() {
        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.recyclerViewFavorites)
        emptyView = findViewById(R.id.textEmpty)
        studyFab = findViewById(R.id.fabStudy)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Favorites"

        studyFab.setOnClickListener {
            startStudyMode()
        }
    }

    private fun loadFavorites() {
        favoriteWords.clear()
        val chapters = VocabData.getChapters()
        chapters.forEach { chapter ->
            favoriteWords.addAll(chapter.words.filter { it.isFavorite })
        }
    }

    private fun setupRecyclerView() {
        wordAdapter = WordAdapter(
            words = favoriteWords,
            onWordClick = { word -> openWordDetail(word) },
            onFavoriteClick = { word -> toggleFavorite(word) }
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@FavoritesActivity)
            adapter = wordAdapter
        }

        updateEmptyView()
    }

    private fun updateEmptyView() {
        if (favoriteWords.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
            studyFab.hide()
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
            studyFab.show()
        }
    }

    private fun openWordDetail(word: Word) {
        val intent = Intent(this, WordDetailActivity::class.java)
        intent.putExtra("word", word)
        intent.putExtra("words", ArrayList(favoriteWords))
        startActivity(intent)
    }

    private fun toggleFavorite(word: Word) {
        word.isFavorite = !word.isFavorite
        loadFavorites()
        wordAdapter.updateWords(favoriteWords)
        updateEmptyView()
    }

    private fun startStudyMode() {
        if (favoriteWords.isNotEmpty()) {
            val intent = Intent(this, StudyActivity::class.java)
            intent.putExtra("words", ArrayList(favoriteWords))
            startActivity(intent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
