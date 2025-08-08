package com.example.spanishvocab

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spanishvocab.adapter.WordAdapter
import com.example.spanishvocab.data.VocabData
import com.example.spanishvocab.data.Word
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Locale

class FavoritesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var studyFab: FloatingActionButton
    private lateinit var wordAdapter: WordAdapter
    private var favoriteWords = mutableListOf<Word>()

    // TTS: 즐겨찾기 리스트에서도 발음 재생 지원
    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        findViewById<TextView>(R.id.toolbarTitle).text = "즐겨찾기"

        recyclerView = findViewById(R.id.recyclerViewFavorites)
        emptyView = findViewById(R.id.textEmpty)
        studyFab = findViewById(R.id.fabStudy)

        // TTS 준비
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("es", "ES")
            }
        }

        studyFab.setOnClickListener { startStudyMode() }

        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        loadFavorites()
        wordAdapter.updateWords(favoriteWords)
        updateEmptyView()
    }

    private fun loadFavorites() {
        favoriteWords.clear()
        favoriteWords.addAll(
            VocabData.getChapters().flatMap { it.words }.filter { it.isFavorite }
        )
    }

    private fun setupRecyclerView() {
        wordAdapter = WordAdapter(
            favoriteWords,
            onWordClick = { word -> openWordDetail(word) },
            onFavoriteClick = { word ->
                word.isFavorite = !word.isFavorite
                loadFavorites()
                wordAdapter.updateWords(favoriteWords)
                updateEmptyView()
            },
            onPronounceClick = { word ->
                tts?.speak(word.spanish, TextToSpeech.QUEUE_FLUSH, null, null)
            }
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

    private fun startStudyMode() {
        if (favoriteWords.isNotEmpty()) {
            val intent = Intent(this, StudyActivity::class.java)
            intent.putExtra("words", ArrayList(favoriteWords))
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}