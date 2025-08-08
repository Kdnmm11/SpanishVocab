package com.example.spanishvocab

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spanishvocab.adapter.ChapterAdapter
import com.example.spanishvocab.data.Level
import com.example.spanishvocab.data.VocabData
import com.example.spanishvocab.data.Chapter

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: ChapterAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler = view.findViewById(R.id.recyclerViewChapters)

        adapter = ChapterAdapter(listOf()) { chapter: Chapter ->
            val intent = Intent(requireContext(), WordListActivity::class.java)
            intent.putExtra("chapter", chapter)
            startActivity(intent)
        }

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        // 최초 데이터 적용
        applyChaptersForSelectedLevel()
    }

    override fun onResume() {
        super.onResume()
        // 돌아올 때도 현재 선택 레벨 기준으로 갱신
        applyChaptersForSelectedLevel()
    }

    private fun applyChaptersForSelectedLevel() {
        val chapters = VocabData.getChapters()
        val selected = getSelectedLevelFromPrefs()
        val filtered = selected?.let { lvl -> chapters.filter { it.level == lvl } } ?: chapters
        adapter.updateChapters(filtered)
    }

    private fun getSelectedLevelFromPrefs(): Level? {
        val prefs = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val name = prefs.getString("selected_level", null)
        return name?.let { Level.valueOf(it) }
    }
}