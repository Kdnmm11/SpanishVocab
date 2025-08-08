package com.example.spanishvocab

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spanishvocab.adapter.ChapterAdapter
import com.example.spanishvocab.data.Level
import com.example.spanishvocab.data.VocabData
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var chapterAdapter: ChapterAdapter
    private var selectedLevel: Level? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // "레벨을 선택 안한 경우에만" 레벨 선택 화면으로 이동
        if (!hasLevel()) {
            startActivity(Intent(this, LevelSelectActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = "Spanish Vocab"
        setupRecyclerView()
        setupBottomNavigation()
        findViewById<FloatingActionButton>(R.id.fabSearch).setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        selectedLevel = getLevel()
        updateChaptersByLevel()
    }

    private fun setupRecyclerView() {
        chapterAdapter = ChapterAdapter(listOf()) { chapter -> openChapter(chapter) }
        val recycler = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerViewChapters)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = chapterAdapter
    }

    private fun updateChaptersByLevel() {
        val chapters = VocabData.getChapters()
        chapterAdapter.updateChapters(chapters.filter { it.level == selectedLevel })
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_favorites -> {
                    startActivity(Intent(this, FavoritesActivity::class.java)); true
                }
                R.id.nav_test -> {
                    startActivity(Intent(this, TestActivity::class.java)); true
                }
                else -> false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add("레벨 변경").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.title == "레벨 변경") {
            startActivity(Intent(this, LevelSelectActivity::class.java))
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getLevel(): Level? {
        val name = getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .getString("selected_level", null)
        return name?.let { Level.valueOf(it) }
    }
    private fun hasLevel(): Boolean {
        return getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .contains("selected_level")
    }
    private fun openChapter(chapter: com.example.spanishvocab.data.Chapter) {
        val intent = Intent(this, WordListActivity::class.java)
        intent.putExtra("chapter", chapter)
        startActivity(intent)
    }
}
