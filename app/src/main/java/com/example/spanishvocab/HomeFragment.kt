package com.example.spanishvocab

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spanishvocab.adapter.ChapterAdapter
import com.example.spanishvocab.repository.WordRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var recycler: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: ChapterAdapter
    private lateinit var repo: WordRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repo = WordRepository(requireContext())
        recycler = view.findViewById(R.id.recyclerViewChapters)
        progressBar = view.findViewById(R.id.progressBar)

        adapter = ChapterAdapter(listOf()) { chapter ->
            val intent = Intent(requireContext(), WordListActivity::class.java)
            intent.putExtra("chapter", chapter)
            startActivity(intent)
        }

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        loadChaptersFromDB()
    }

    override fun onResume() {
        super.onResume()
        loadChaptersFromDB()

        // ★ [핵심] MainActivity에 있는 버튼 활성화 & 클릭 이벤트 연결
        if (activity is MainActivity) {
            (activity as MainActivity).setSyncButton(true) {
                // 버튼 눌렀을 때 실행할 내용
                syncData()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // 다른 화면(즐겨찾기 등)으로 갈 때 꼬이지 않게 일단 리스너 해제
        // (MainActivity에서 탭 전환 시 숨기긴 하지만 안전장치)
        if (activity is MainActivity) {
            (activity as MainActivity).setSyncButton(false, null)
        }
    }

    private fun loadChaptersFromDB() {
        CoroutineScope(Dispatchers.Main).launch {
            val chapters = repo.getChaptersFromDB()
            if (chapters.isNotEmpty()) {
                adapter.updateChapters(chapters)
            }
        }
    }

    private fun syncData() {
        progressBar.visibility = View.VISIBLE
        Toast.makeText(context, "동기화 시작...", Toast.LENGTH_SHORT).show()

        CoroutineScope(Dispatchers.IO).launch {
            val result = repo.syncWithGoogleSheet()

            withContext(Dispatchers.Main) {
                progressBar.visibility = View.GONE

                result.onSuccess { msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    loadChaptersFromDB()
                }.onFailure { e ->
                    Toast.makeText(context, "실패: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}