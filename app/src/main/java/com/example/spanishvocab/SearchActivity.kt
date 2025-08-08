package com.example.spanishvocab

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spanishvocab.adapter.WordAdapter
import com.example.spanishvocab.data.VocabData
import com.example.spanishvocab.data.Word

class SearchActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var searchEdit: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var wordAdapter: WordAdapter
    private val allWords = mutableListOf<Word>()
    private var filteredWords = mutableListOf<Word>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        setupViews()
        loadAllWords()
        setupRecyclerView()
        setupSearch()
    }

    private fun setupViews() {
        toolbar = findViewById(R.id.toolbar)
        searchEdit = findViewById(R.id.editSearch)
        recyclerView = findViewById(R.id.recyclerViewSearch)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Search Words"

        searchEdit.requestFocus()
    }

    private fun loadAllWords() {
        val chapters = VocabData.getChapters()
        chapters.forEach { chapter ->
            allWords.addAll(chapter.words)
        }
        filteredWords.addAll(allWords)
    }

    private fun setupRecyclerView() {
        wordAdapter = WordAdapter(
            words = filteredWords,
            onWordClick = { word -> openWordDetail(word) },
            onFavoriteClick = { word -> toggleFavorite(word) }
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = wordAdapter
        }
    }

    private fun setupSearch() {
        searchEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterWords(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterWords(query: String) {
        filteredWords.clear()

        if (query.isEmpty()) {
            filteredWords.addAll(allWords)
        } else {
            allWords.forEach { word ->
                if (word.spanish.contains(query, ignoreCase = true) ||
                    word.meanings.any { it.contains(query, ignoreCase = true) } ||
                    word.pronunciation.contains(query, ignoreCase = true)) {
                    filteredWords.add(word)
                }
            }
        }

        wordAdapter.updateWords(filteredWords)
    }

    private fun openWordDetail(word: Word) {
        val intent = Intent(this, WordDetailActivity::class.java)
        intent.putExtra("word", word)
        intent.putExtra("words", ArrayList(allWords))
        startActivity(intent)
    }

    private fun toggleFavorite(word: Word) {
        word.isFavorite = !word.isFavorite
        wordAdapter.notifyDataSetChanged()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
