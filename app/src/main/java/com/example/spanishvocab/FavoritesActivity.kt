package com.example.spanishvocab

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spanishvocab.adapter.WordAdapter
import com.example.spanishvocab.data.Word
import com.example.spanishvocab.repository.WordRepository
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class FavoritesActivity : AppCompatActivity() {

    private lateinit var adapter: WordAdapter
    private lateinit var tts: TextToSpeech
    private lateinit var repo: WordRepository
    private var favoriteWords: ArrayList<Word> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        repo = WordRepository(this)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewFavorites)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = WordAdapter(
            words = emptyList(),
            onPronounceClick = { word -> speakOut(word.spanish) },
            onFavoriteClick = { word ->
                toggleFavorite(word)
            },
            onItemClick = { word ->
                val intent = Intent(this, WordDetailActivity::class.java)
                intent.putExtra("word", word)
                intent.putParcelableArrayListExtra("words", favoriteWords)
                startActivity(intent)
            }
        )
        recyclerView.adapter = adapter

        // 스터디 모드 버튼
        findViewById<FloatingActionButton>(R.id.fabStudy).setOnClickListener {
            if (favoriteWords.isNotEmpty()) {
                val intent = Intent(this, StudyActivity::class.java)
                intent.putParcelableArrayListExtra("words", favoriteWords)
                startActivity(intent)
            } else {
                Toast.makeText(this, "즐겨찾기한 단어가 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        initTTS()
    }

    override fun onResume() {
        super.onResume()
        loadFavorites() // 화면 돌아올 때마다 목록 갱신
    }

    private fun loadFavorites() {
        CoroutineScope(Dispatchers.Main).launch {
            val allWords = repo.getAllWords()
            favoriteWords = ArrayList(allWords.filter { it.isFavorite })

            adapter.updateWords(favoriteWords)

            // 데이터 없음 표시
            findViewById<TextView>(R.id.textEmpty).visibility =
                if (favoriteWords.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun toggleFavorite(word: Word) {
        word.isFavorite = !word.isFavorite

        // ★ DB 업데이트
        CoroutineScope(Dispatchers.IO).launch {
            repo.updateFavorite(word)
            // 목록 갱신은 onResume이나 loadFavorites() 호출로 처리
            launch(Dispatchers.Main) {
                loadFavorites()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initTTS() {
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) tts.setLanguage(Locale("es", "ES"))
        }
    }

    private fun speakOut(text: String) {
        if (::tts.isInitialized) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    override fun onDestroy() {
        if (::tts.isInitialized) { tts.stop(); tts.shutdown() }
        super.onDestroy()
    }
}