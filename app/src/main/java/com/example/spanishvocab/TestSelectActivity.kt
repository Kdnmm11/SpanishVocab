package com.example.spanishvocab

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.spanishvocab.data.Level
import com.google.android.material.bottomnavigation.BottomNavigationView

class TestSelectActivity : AppCompatActivity() {

    // 레벨 선택 후 돌아왔을 때, 곧바로 TestActivity(mode=level) 실행하기 위한 플래그
    private var pendingLevelTest = false

    // 챕터 선택 결과 받기 (selected_chapter_id)
    private val chapterPickLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val chapterId = result.data?.getIntExtra("selected_chapter_id", -1) ?: -1
            if (chapterId != -1) {
                startActivity(
                    Intent(this, TestActivity::class.java).apply {
                        putExtra("mode", "chapter")
                        putExtra("chapterId", chapterId)
                    }
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_select)

        // 툴바 타이틀
        findViewById<TextView>(R.id.toolbarTitle).text = "테스트 방식 선택"

        // 카드: 레벨별 테스트
        findViewById<androidx.cardview.widget.CardView>(R.id.cardLevel).setOnClickListener {
            // LevelSelectActivity는 보통 prefs("selected_level")에 저장하고 finish함.
            // 다른 파일 수정 없이 동작하게, 복귀 후 onResume에서 바로 TestActivity를 띄우도록 플래그만 설정.
            pendingLevelTest = true
            startActivity(Intent(this, LevelSelectActivity::class.java))
        }

        // 카드: 챕터별 테스트
        findViewById<androidx.cardview.widget.CardView>(R.id.cardChapter).setOnClickListener {
            // 현재 선택된 레벨의 챕터 목록을 보여주는 화면으로 이동(결과로 chapter id를 돌려받음)
            val i = Intent(this, ChapterSelectActivity::class.java)
            i.putExtra("forTest", true)
            chapterPickLauncher.launch(i)
        }

        // 카드: 즐겨찾기 테스트
        findViewById<androidx.cardview.widget.CardView>(R.id.cardFavorite).setOnClickListener {
            startActivity(
                Intent(this, TestActivity::class.java).apply {
                    putExtra("mode", "favorite") // TestActivity에서 정확히 인식
                }
            )
        }

        // 하단 네비게이션
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_test
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_favorites -> {
                    startActivity(Intent(this, FavoritesActivity::class.java))
                    true
                }
                R.id.nav_test -> true // 현재 화면
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 레벨 선택에서 돌아온 경우에만 동작
        if (pendingLevelTest) {
            val displayName = getSelectedLevelDisplayName() // 예: "DELE A1"
            if (displayName != null) {
                startActivity(
                    Intent(this, TestActivity::class.java).apply {
                        putExtra("mode", "level")
                        putExtra("level", displayName) // TestActivity.loadWordsByMode()에서 사용
                    }
                )
            }
            pendingLevelTest = false
        }
    }

    // prefs에서 현재 선택된 레벨의 displayName("DELE A1" 등) 가져오기
    private fun getSelectedLevelDisplayName(): String? {
        val name = getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .getString("selected_level", null) ?: return null
        val level = runCatching { Level.valueOf(name) }.getOrNull() ?: return null
        return level.displayName
    }
}