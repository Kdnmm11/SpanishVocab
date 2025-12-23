package com.example.spanishvocab

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spanishvocab.adapter.WordAdapter
import com.example.spanishvocab.data.Word
import com.example.spanishvocab.repository.WordRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class SearchActivity : AppCompatActivity() {

    private lateinit var adapter: WordAdapter
    private lateinit var tts: TextToSpeech
    private lateinit var repo: WordRepository
    private var allWords: List<Word> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        repo = WordRepository(this)

        // 1. 툴바 설정
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // 2. 리스트 설정
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewSearch)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = WordAdapter(
            words = emptyList(),
            onPronounceClick = { word -> speakOut(word.spanish) },
            onFavoriteClick = { word -> toggleFavorite(word) },
            onItemClick = { word ->
                val intent = Intent(this, WordDetailActivity::class.java)
                intent.putExtra("word", word)
                intent.putParcelableArrayListExtra("words", ArrayList(adapter.currentItems))
                startActivity(intent)
            }
        )
        recyclerView.adapter = adapter

        // 3. 검색창 설정 (EditText로 변경됨)
        val editSearch = findViewById<EditText>(R.id.editSearch)
        editSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { filter(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        initTTS()
        loadAllWords()
    }

    private fun loadAllWords() {
        CoroutineScope(Dispatchers.IO).launch {
            val words = repo.getAllWords()
            allWords = words

            withContext(Dispatchers.Main) {
                if (words.isNotEmpty()) {
                    adapter.updateWords(allWords)
                } else {
                    Toast.makeText(this@SearchActivity, "단어 데이터가 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun filter(text: String) {
        if (text.isBlank()) {
            adapter.updateWords(allWords)
            return
        }

        val filtered = allWords.filter {
            it.spanish.contains(text, true) || it.meanings.contains(text, true)
        }
        adapter.updateWords(filtered)
    }

    private fun toggleFavorite(word: Word) {
        word.isFavorite = !word.isFavorite
        adapter.notifyDataSetChanged()
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