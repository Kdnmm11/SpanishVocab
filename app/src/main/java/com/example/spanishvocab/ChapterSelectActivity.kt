package com.example.spanishvocab

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spanishvocab.adapter.ChapterAdapter
import com.example.spanishvocab.repository.WordRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChapterSelectActivity : AppCompatActivity() {

    private lateinit var repo: WordRepository
    private lateinit var adapter: ChapterAdapter
    private var isTestMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_list)
        // 주의: activity_word_list 레이아웃을 재사용하거나,
        // RecyclerView만 있는 별도 레이아웃(activity_chapter_select)을 쓰셔도 됩니다.
        // 여기서는 리스트 화면 구조(툴바+리사이클러뷰)가 같다면 재사용한다고 가정합니다.

        repo = WordRepository(this)

        // 테스트 모드인지 확인
        isTestMode = intent.getBooleanExtra("forTest", false)

        // 툴바 설정
        findViewById<TextView>(R.id.textChapterTitle).text = if(isTestMode) "테스트 챕터 선택" else "챕터 목록"
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        // 리스트 설정
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerWordList)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ChapterAdapter(emptyList()) { chapter ->
            if (isTestMode) {
                // [테스트 모드일 때] 선택한 ID를 돌려주고 종료
                val resultIntent = Intent()
                resultIntent.putExtra("selected_chapter_id", chapter.id)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } else {
                // [일반 모드일 때] 단어장으로 이동 (기존 로직)
                val intent = Intent(this, WordListActivity::class.java)
                intent.putExtra("chapter", chapter)
                startActivity(intent)
            }
        }
        recyclerView.adapter = adapter

        loadChapters()
    }

    private fun loadChapters() {
        CoroutineScope(Dispatchers.Main).launch {
            val chapters = repo.getChaptersFromDB()
            adapter.updateChapters(chapters)
        }
    }
}