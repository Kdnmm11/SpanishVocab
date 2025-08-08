package com.example.spanishvocab

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spanishvocab.adapter.WordAdapter
import com.example.spanishvocab.data.VocabData
import com.example.spanishvocab.data.Word
import java.util.Locale

class FavoritesFragment : Fragment(R.layout.fragment_favorites) {

    private lateinit var emptyText: TextView
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: WordAdapter
    private val words = mutableListOf<Word>()

    private var tts: TextToSpeech? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        emptyText = view.findViewById(R.id.textEmpty)
        recycler = view.findViewById(R.id.recyclerViewFavorites)

        // TTS 준비
        tts = TextToSpeech(requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("es", "ES")
            }
        }

        adapter = WordAdapter(
            words,
            onWordClick = { word ->
                val it = Intent(requireContext(), WordDetailActivity::class.java).apply {
                    putExtra("word", word)
                    putParcelableArrayListExtra("words", ArrayList(words))
                }
                startActivity(it)
            },
            onFavoriteClick = { word ->
                word.isFavorite = !word.isFavorite
                updateGlobalFavorite(word)
                refreshData()
            },
            onPronounceClick = { word ->
                tts?.speak(word.spanish, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        )

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        refreshData()
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun refreshData() {
        words.clear()
        words.addAll(VocabData.getFavoriteWords())
        adapter.updateWords(words)
        emptyText.visibility = if (words.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun updateGlobalFavorite(word: Word) {
        VocabData.getChapters().forEach { ch ->
            ch.words.find { it.id == word.id }?.isFavorite = word.isFavorite
        }
    }

    override fun onDestroyView() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroyView()
    }
}