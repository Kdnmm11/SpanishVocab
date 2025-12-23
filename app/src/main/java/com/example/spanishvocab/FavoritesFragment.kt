package com.example.spanishvocab

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spanishvocab.adapter.WordAdapter
import com.example.spanishvocab.data.Word
import com.example.spanishvocab.repository.WordRepository
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class FavoritesFragment : Fragment(R.layout.fragment_favorites) {

    private lateinit var adapter: WordAdapter
    private lateinit var tts: TextToSpeech
    private lateinit var repo: WordRepository
    private var favoriteWords: List<Word> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repo = WordRepository(requireContext())

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewFavorites)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = WordAdapter(
            words = emptyList(),
            onPronounceClick = { word -> speakOut(word.spanish) },
            onFavoriteClick = { word ->
                // 즐겨찾기 해제 시 리스트에서 제거하는 효과
                word.isFavorite = !word.isFavorite
                loadFavorites() // 목록 새로고침
            },
            onItemClick = { word ->
                val intent = Intent(requireContext(), WordDetailActivity::class.java)
                intent.putExtra("word", word)
                intent.putParcelableArrayListExtra("words", ArrayList(favoriteWords))
                startActivity(intent)
            }
        )
        recyclerView.adapter = adapter

        // ★ [추가됨] 스터디 모드 버튼 (FAB) 연결
        // (XML에 id가 fabStudy로 되어있는지 확인하세요. 만약 없으면 추가 필요)
        val fabStudy = view.findViewById<FloatingActionButton>(R.id.fabStudy)
        fabStudy?.setOnClickListener {
            if (favoriteWords.isNotEmpty()) {
                val intent = Intent(requireContext(), StudyActivity::class.java)
                intent.putParcelableArrayListExtra("words", ArrayList(favoriteWords))
                startActivity(intent)
            } else {
                Toast.makeText(context, "즐겨찾기한 단어가 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        initTTS()
    }

    override fun onResume() {
        super.onResume()
        loadFavorites()
    }

    private fun loadFavorites() {
        CoroutineScope(Dispatchers.Main).launch {
            // 전체 단어 중 isFavorite가 true인 것만 필터링 (임시 방법)
            // 나중에 DB에 isFavorite 컬럼이 생기면 쿼리로 처리하는 게 더 빠릅니다.
            val allWords = repo.getAllWords()
            favoriteWords = allWords.filter { it.isFavorite }

            adapter.updateWords(favoriteWords)

            // 빈 화면 처리 (텍스트뷰가 있다면)
            // view?.findViewById<TextView>(R.id.textEmpty)?.visibility =
            //    if (favoriteWords.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun initTTS() {
        tts = TextToSpeech(requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS) tts.setLanguage(Locale("es", "ES"))
        }
    }

    private fun speakOut(text: String) {
        if (::tts.isInitialized) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    override fun onDestroyView() {
        if (::tts.isInitialized) { tts.stop(); tts.shutdown() }
        super.onDestroyView()
    }
}