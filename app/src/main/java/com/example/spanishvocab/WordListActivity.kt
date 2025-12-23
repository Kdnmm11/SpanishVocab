package com.example.spanishvocab

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spanishvocab.adapter.WordAdapter
import com.example.spanishvocab.data.Chapter
import com.example.spanishvocab.data.Word
import com.example.spanishvocab.repository.WordRepository
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class WordListActivity : AppCompatActivity() {

    private lateinit var repo: WordRepository
    private lateinit var adapter: WordAdapter
    private lateinit var tts: TextToSpeech
    private var currentWordList: ArrayList<Word> = arrayListOf()

    // 챕터 정보를 onResume에서도 쓰기 위해 전역 변수로 선언
    private var currentChapter: Chapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_list)

        repo = WordRepository(this)

        // Intent로 전달받은 챕터 정보 저장
        currentChapter = intent.getParcelableExtra("chapter")
        if (currentChapter == null) {
            finish()
            return
        }

        // 툴바 설정
        findViewById<TextView>(R.id.textChapterTitle).text = currentChapter?.title
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        // 리스트 설정
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerWordList)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = WordAdapter(
            words = emptyList(),
            onPronounceClick = { word -> speakOut(word.spanish) },

            // ★ [수정 1] 리스트에서 즐겨찾기 누르면 실제 DB에 저장
            onFavoriteClick = { word ->
                word.isFavorite = !word.isFavorite // 상태 반전

                // DB 업데이트 (비동기)
                CoroutineScope(Dispatchers.IO).launch {
                    repo.updateFavorite(word)
                }

                // 화면 갱신 (해당 아이템만 깜빡임 없이 업데이트)
                adapter.notifyItemChanged(adapter.currentItems.indexOf(word))
            },

            onItemClick = { word ->
                // 카드 클릭 -> 상세 화면
                val intent = Intent(this, WordDetailActivity::class.java)
                intent.putExtra("word", word)
                intent.putParcelableArrayListExtra("words", currentWordList)
                startActivity(intent)
            }
        )
        recyclerView.adapter = adapter

        // 공부하기 FAB 버튼
        val fabStudy = findViewById<FloatingActionButton>(R.id.fabStudy)
        fabStudy.setOnClickListener {
            if (currentWordList.isNotEmpty()) {
                val intent = Intent(this, StudyActivity::class.java)
                intent.putParcelableArrayListExtra("words", currentWordList)
                startActivity(intent)
            } else {
                Toast.makeText(this, "학습할 단어가 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        initTTS()
    }

    // ★ [수정 2] 화면이 다시 보일 때마다(상세화면 갔다가 돌아올 때) 데이터 새로고침
    override fun onResume() {
        super.onResume()
        currentChapter?.let { loadWords(it.id) }
    }

    private fun loadWords(chapterId: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            // DB에서 최신 데이터 가져오기
            val words = repo.getWords(chapterId)

            if (words.isNotEmpty()) {
                currentWordList = ArrayList(words)
                adapter.updateWords(words)
            } else {
                Toast.makeText(this@WordListActivity, "단어가 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
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