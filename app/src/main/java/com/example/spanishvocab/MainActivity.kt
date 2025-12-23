package com.example.spanishvocab

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private val TAG_HOME = "home"
    private val TAG_FAV  = "fav"
    private val TAG_TEST = "test"

    private lateinit var toolbarTitle: TextView
    private lateinit var btnSync: ImageButton
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 뷰 참조
        toolbarTitle = findViewById(R.id.toolbarTitle)
        btnSync      = findViewById(R.id.btnSync)
        bottomNav    = findViewById(R.id.bottomNavigation)
        fab          = findViewById(R.id.fabSearch)

        // 툴바 초기 설정
        setToolbarTitle("Spanish Vocab")

        // 상태바 인셋 적용 (기존 코드 유지)
        applyStatusBarInsets()

        if (savedInstanceState == null) {
            // 초기 화면: 홈 프래그먼트
            supportFragmentManager.beginTransaction()
                .add(R.id.mainContainer, HomeFragment(), TAG_HOME)
                .commitNow()

            setFabVisible(true)
            // 홈에서는 동기화 버튼 활성화 (HomeFragment에서 리스너 연결)
            setSyncButton(true, null)
        }

        // 검색 FAB 클릭
        fab.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        // 하단 탭 전환 리스너
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    switchTo(TAG_HOME) { HomeFragment() }
                    setToolbarTitle("Spanish Vocab")
                    setFabVisible(true)
                    // 홈 탭: 동기화 버튼 보이기
                    btnSync.visibility = View.VISIBLE
                    true
                }
                R.id.nav_favorites -> {
                    switchTo(TAG_FAV) { FavoritesFragment() } // 또는 액티비티 이동 시 수정
                    setToolbarTitle("즐겨찾기")
                    setFabVisible(false)
                    btnSync.visibility = View.GONE
                    true
                }
                R.id.nav_test -> {
                    // ★ TestSelectFragment로 프래그먼트 교체
                    switchTo(TAG_TEST) { TestSelectFragment() }
                    setToolbarTitle("테스트 모드")
                    setFabVisible(false)
                    btnSync.visibility = View.GONE
                    true
                }
                else -> false
            }
        }
    }

    private fun applyStatusBarInsets() {
        val toolbar = findViewById<View>(R.id.toolbar)
        val root    = findViewById<View>(R.id.root)
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
                val sysBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.statusBars())
                toolbar.updatePadding(top = sysBars.top)
                insets
            }
        }
    }

    // ====== 외부 호출용 함수들 (기존 구조 유지) ======

    fun setToolbarTitle(title: String) {
        toolbarTitle.text = title
    }

    // ★ 홈 프래그먼트에서 동기화 버튼을 제어하기 위한 함수
    fun setSyncButton(show: Boolean, onClick: (() -> Unit)?) {
        btnSync.visibility = if (show) View.VISIBLE else View.GONE
        if (show && onClick != null) {
            btnSync.setOnClickListener { onClick() }
        }
    }

    private fun setFabVisible(visible: Boolean) {
        fab.visibility = if (visible) View.VISIBLE else View.GONE
    }

    // 프래그먼트 전환 헬퍼 함수 (기존 구조 유지)
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
}