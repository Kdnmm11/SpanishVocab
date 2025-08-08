package com.example.spanishvocab

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spanishvocab.adapter.WordAdapter
import com.example.spanishvocab.data.VocabData
import com.example.spanishvocab.data.Word
import com.google.android.material.textfield.TextInputEditText
import java.util.Locale

class SearchActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var wordAdapter: WordAdapter
    private lateinit var allWords: List<Word>
    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        findViewById<TextView>(R.id.toolbarTitle).text = "검색"

        // 모든 단어 로드
        allWords = VocabData.getChapters().flatMap { it.words }

        // TTS 준비
        tts = TextToSpeech(this, this)

        // 어댑터: 카드 탭 → 상세, 하트 탭 → 즐겨찾기, 스피커 탭 → 발음 재생
        wordAdapter = WordAdapter(
            words = allWords,
            onWordClick = { word ->
                val current = ArrayList(wordAdapter.currentItems())
                val i = Intent(this, WordDetailActivity::class.java)
                i.putExtra("word", word)
                i.putExtra("words", current)
                startActivity(i)
            },
            onFavoriteClick = { word ->
                word.isFavorite = !word.isFavorite
                // 전역 데이터 동기화
                VocabData.getChapters().forEach { ch ->
                    ch.words.find { it.id == word.id }?.isFavorite = word.isFavorite
                }
                // 현재 목록 새로고침(필터 유지)
                wordAdapter.updateWords(wordAdapter.currentItems())
            },
            onPronounceClick = { word ->
                tts?.speak(word.spanish, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        )

        val recycler = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerViewSearch)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = wordAdapter

        val editSearch = findViewById<TextInputEditText>(R.id.editSearch)
        editSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = filterWords(s?.toString())
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale("es", "ES")
        }
    }

    // 🔎 단어/발음/뜻만 검색(예문/해석 제외)
    private fun filterWords(query: String?) {
        val text = query?.trim()?.lowercase() ?: ""
        val filtered = if (text.isEmpty()) {
            allWords
        } else {
            allWords.filter { w ->
                w.spanish.lowercase().contains(text) ||                       // 스페인어 단어
                        (w.pronunciation?.lowercase()?.contains(text) == true) ||     // 발음
                        w.meanings.any { it.lowercase().contains(text) }              // 뜻
            }
        }
        wordAdapter.updateWords(filtered)
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}