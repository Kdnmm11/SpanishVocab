package com.example.spanishvocab

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spanishvocab.adapter.WordAdapter
import com.example.spanishvocab.data.Chapter
import com.example.spanishvocab.data.Word
import com.google.android.material.floatingactionbutton.FloatingActionButton

class WordListActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var chapterTitle: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var wordAdapter: WordAdapter
    private lateinit var studyFab: FloatingActionButton
    private lateinit var chapter: Chapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_list)

        chapter = intent.getParcelableExtra("chapter") ?: return

        setupViews()
        setupRecyclerView()
    }

    private fun setupViews() {
        toolbar = findViewById(R.id.toolbar)
        chapterTitle = findViewById(R.id.textChapterTitle)
        recyclerView = findViewById(R.id.recyclerViewWords)
        studyFab = findViewById(R.id.fabStudy)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        chapterTitle.text = chapter.title

        studyFab.setOnClickListener {
            startStudyMode()
        }
    }

    private fun setupRecyclerView() {
        wordAdapter = WordAdapter(
            words = chapter.words,
            onWordClick = { word -> openWordDetail(word) },
            onFavoriteClick = { word -> toggleFavorite(word) }
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@WordListActivity)
            adapter = wordAdapter
        }
    }

    private fun openWordDetail(word: Word) {
        val intent = Intent(this, WordDetailActivity::class.java)
        intent.putExtra("word", word)
        intent.putExtra("words", ArrayList(chapter.words))
        startActivity(intent)
    }

    private fun toggleFavorite(word: Word) {
        word.isFavorite = !word.isFavorite
        wordAdapter.notifyDataSetChanged()
    }

    private fun startStudyMode() {
        val intent = Intent(this, StudyActivity::class.java)
        intent.putExtra("words", ArrayList(chapter.words))
        startActivity(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
