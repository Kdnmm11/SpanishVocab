package com.example.spanishvocab

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment

class TestSelectFragment : Fragment(R.layout.fragment_test_select) {

    // 챕터 선택 결과 받기
    private val chapterPickLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val chapterId = result.data?.getIntExtra("selected_chapter_id", -1) ?: -1
            if (chapterId != -1) {
                startActivity(Intent(requireContext(), TestActivity::class.java).apply {
                    putExtra("mode", "chapter")
                    putExtra("chapterId", chapterId)
                })
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 툴바 제목 설정
        requireActivity().findViewById<TextView>(R.id.toolbarTitle)?.text = "테스트 모드"

        // ★ [중요] 여기에 있던 showLevelSelector(false) 코드를 삭제했습니다.
        // (MainActivity에서 해당 함수를 지웠으므로, 여기서 호출하면 에러가 납니다.)

        // 1. 챕터별 퀴즈
        view.findViewById<CardView>(R.id.cardChapterQuiz).setOnClickListener {
            val intent = Intent(requireContext(), ChapterSelectActivity::class.java)
            intent.putExtra("forTest", true)
            chapterPickLauncher.launch(intent)
        }

        // 2. 즐겨찾기 퀴즈
        view.findViewById<CardView>(R.id.cardFavoriteQuiz).setOnClickListener {
            startActivity(Intent(requireContext(), TestActivity::class.java).apply {
                putExtra("mode", "favorite")
            })
        }

        // 3. 딕테이션 레벨 선택 (A2, B1, B2 - 예문 난이도)
        val onDictationClick = View.OnClickListener { v ->
            // 버튼의 텍스트(A2, B1, B2)를 그대로 가져와서 DictationActivity로 넘깁니다.
            val level = (v as Button).text.toString()
            startActivity(Intent(requireContext(), DictationActivity::class.java).apply {
                putExtra("level", level)
            })
        }

        // XML의 버튼 ID와 상관없이 클릭 시 텍스트를 가져오도록 연결
        view.findViewById<Button>(R.id.btnLevelA1).setOnClickListener(onDictationClick)
        view.findViewById<Button>(R.id.btnLevelA2).setOnClickListener(onDictationClick)
        view.findViewById<Button>(R.id.btnLevelB1).setOnClickListener(onDictationClick)
    }
}