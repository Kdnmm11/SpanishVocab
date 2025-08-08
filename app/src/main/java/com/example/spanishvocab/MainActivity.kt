package com.example.spanishvocab

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.example.spanishvocab.data.Level
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private val TAG_HOME = "home"
    private val TAG_FAV  = "fav"
    private val TAG_TEST = "test"

    private lateinit var toolbarTitle: TextView
    private lateinit var levelText: TextView
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 항상 레이아웃 세팅(뒤에 LevelSelect를 덮어 띄우더라도 Main이 안전하게 준비됨)
        setContentView(R.layout.activity_main)

        // 뷰 참조
        toolbarTitle = findViewById(R.id.toolbarTitle)
        levelText    = findViewById(R.id.textCurrentLevel)
        bottomNav    = findViewById(R.id.bottomNavigation)
        fab          = findViewById(R.id.fabSearch)

        // 툴바 타이틀/레벨
        setToolbarTitle("Spanish Vocab")
        levelText.text = getLevelDisplayName()
        levelText.setOnClickListener {
            startActivity(Intent(this, LevelSelectActivity::class.java))
        }

        // 상태바 인셋 적용
        applyStatusBarInsets()

        // 최초 진입: 홈 프래그먼트
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.mainContainer, HomeFragment(), TAG_HOME)
                .commitNow()
            setFabVisible(true)
            showLevelSelector(true)
        }

        // FAB 검색
        fab.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        // 하단 네비 전환
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    switchTo(TAG_HOME) { HomeFragment() }
                    setToolbarTitle("Spanish Vocab")
                    setFabVisible(true)
                    showLevelSelector(true)
                    true
                }
                R.id.nav_favorites -> {
                    switchTo(TAG_FAV) { FavoritesFragment() }
                    setToolbarTitle("즐겨찾기")
                    setFabVisible(false)
                    showLevelSelector(false)
                    true
                }
                R.id.nav_test -> {
                    switchTo(TAG_TEST) { TestSelectFragment() }
                    setToolbarTitle("테스트 방식 선택")
                    setFabVisible(false)
                    showLevelSelector(false)
                    true
                }
                else -> false
            }
        }

        // ★ 레벨 미선택 시 선택 화면을 "덮어" 띄우기(메인은 남겨둠 → 복귀 시 홈으로 자연 귀환)
        if (!hasLevel()) {
            startActivity(Intent(this, LevelSelectActivity::class.java))
            // finish() 절대 호출하지 않음
        }
    }

    override fun onResume() {
        super.onResume()
        // 레벨 텍스트 최신화
        levelText.text = getLevelDisplayName()
    }

    // 상태바(노치 포함)만큼 툴바를 내려주기
    private fun applyStatusBarInsets() {
        val toolbar = findViewById<View>(R.id.toolbar)
        val root    = findViewById<View>(R.id.root)

        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val sysBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.statusBars())
            toolbar.updatePadding(top = sysBars.top)
            insets
        }
    }

    // ====== 외부(프래그먼트)에서도 호출 가능 ======
    fun setToolbarTitle(title: String) {
        toolbarTitle.text = title
    }

    fun showLevelSelector(show: Boolean) {
        levelText.visibility = if (show) View.VISIBLE else View.GONE
    }
    // ===========================================

    private fun setFabVisible(visible: Boolean) {
        fab.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun switchTo(tag: String, factory: () -> Fragment) {
        val fm = supportFragmentManager
        val current = fm.fragments.firstOrNull { it.isVisible }
        val target = fm.findFragmentByTag(tag) ?: factory()

        fm.beginTransaction().apply {
            if (!target.isAdded) add(R.id.mainContainer, target, tag)
            current?.let { hide(it) }
            show(target)
        }.commit()
    }

    // ===== 레벨 유틸 =====
    private fun getLevel(): Level? {
        val name = getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .getString("selected_level", null)
        return name?.let { Level.valueOf(it) }
    }

    private fun hasLevel(): Boolean {
        return getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .contains("selected_level")
    }

    // "DELE A1" → "A1"
    private fun getLevelDisplayName(): String {
        val level = getLevel()
        return level?.displayName?.replace("DELE ", "") ?: "A1"
    }
}