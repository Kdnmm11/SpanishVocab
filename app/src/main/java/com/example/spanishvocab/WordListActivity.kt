package com.example.spanishvocab

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spanishvocab.adapter.WordAdapter
import com.example.spanishvocab.data.Chapter
import com.example.spanishvocab.data.VocabData
import com.example.spanishvocab.data.Word
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Locale

class WordListActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var wordAdapter: WordAdapter
    private lateinit var fabStudy: FloatingActionButton
    private var words = mutableListOf<Word>()

    private var tts: TextToSpeech? = null

    private val detailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val changedWords = result.data?.getParcelableArrayListExtra<Word>("words")
            if (changedWords != null && changedWords.isNotEmpty()) {
                words.clear()
                words.addAll(changedWords)
                wordAdapter.updateWords(words)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_list)

        findViewById<TextView>(R.id.toolbarTitle).text = "단어 리스트"

        recyclerView = findViewById(R.id.recyclerViewWords)
        fabStudy = findViewById(R.id.fabStudy)

        // TTS 준비
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("es", "ES")
            }
        }

        val chapter = intent.getParcelableExtra<Chapter>("chapter")
        words = if (chapter != null) ArrayList(chapter.words) else mutableListOf()

        wordAdapter = WordAdapter(
            words,
            onWordClick = { word ->
                val intent = Intent(this, WordDetailActivity::class.java)
                intent.putExtra("word", word)
                intent.putExtra("words", ArrayList(words))
                detailLauncher.launch(intent)
            },
            onFavoriteClick = { word ->
                word.isFavorite = !word.isFavorite
                updateFavoriteInVocabData(word)
                wordAdapter.updateWords(words)
            },
            onPronounceClick = { word ->
                tts?.speak(word.spanish, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@WordListActivity)
            adapter = wordAdapter
            setHasFixedSize(true)
        }

        fabStudy.setOnClickListener {
            if (words.isNotEmpty()) {
                val intent = Intent(this, StudyActivity::class.java)
                intent.putExtra("words", ArrayList(words))
                startActivity(intent)
            }
        }
        updateFabStudy()
    }

    override fun onResume() {
        super.onResume()
        wordAdapter.updateWords(words)
        updateFabStudy()
    }

    private fun updateFavoriteInVocabData(word: Word) {
        VocabData.getChapters().forEach { chapter ->
            chapter.words.find { it.id == word.id }?.isFavorite = word.isFavorite
        }
    }

    private fun updateFabStudy() {
        if (words.isEmpty()) fabStudy.hide() else fabStudy.show()
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}