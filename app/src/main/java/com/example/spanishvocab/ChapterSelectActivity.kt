package com.example.spanishvocab

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spanishvocab.data.Chapter
import com.example.spanishvocab.data.Level
import com.example.spanishvocab.data.VocabData

class ChapterSelectActivity : AppCompatActivity() {

    private var forTest: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chapter_select)

        forTest = intent.getBooleanExtra("forTest", false)

        findViewById<TextView>(R.id.toolbarTitle).text =
            if (forTest) "챕터 선택 (테스트)" else "챕터 선택"

        val selectedLevel = getSelectedLevelFromPrefs()
        val chapters: List<Chapter> = if (selectedLevel != null) {
            VocabData.getChapters().filter { it.level == selectedLevel }
        } else {
            VocabData.getChapters()
        }

        val rv = findViewById<RecyclerView>(R.id.recyclerViewChapters)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = ChapterPickAdapter(chapters) { chapter ->
            if (forTest) {
                setResult(RESULT_OK, Intent().putExtra("selected_chapter_id", chapter.id))
                finish()
            } else {
                // 필요 시 여기서 일반 모드 행동(예: 해당 챕터 단어 리스트 열기)
                // val i = Intent(this, WordListActivity::class.java)
                // i.putExtra("chapter", chapter)
                // startActivity(i)
            }
        }
    }

    private fun getSelectedLevelFromPrefs(): Level? {
        val name = getSharedPreferences("prefs", MODE_PRIVATE)
            .getString("selected_level", null)
        return name?.let { Level.valueOf(it) }
    }
}

class ChapterPickAdapter(
    private val items: List<Chapter>,
    private val onClick: (Chapter) -> Unit
) : RecyclerView.Adapter<ChapterPickVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterPickVH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chapter, parent, false)
        return ChapterPickVH(v)
    }

    override fun onBindViewHolder(holder: ChapterPickVH, position: Int) {
        holder.bind(items[position], onClick)
    }

    override fun getItemCount(): Int = items.size
}

class ChapterPickVH(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
    private val title = itemView.findViewById<TextView>(R.id.textChapterTitle)
    fun bind(chapter: Chapter, onClick: (Chapter) -> Unit) {
        title.text = chapter.title
        itemView.setOnClickListener { onClick(chapter) }
    }
}