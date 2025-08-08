package com.example.spanishvocab

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.spanishvocab.data.Level

class TestSelectFragment : Fragment(R.layout.fragment_test_select) {

    // 레벨 선택 결과 받기: intent에 "selected_level" 로 enum 이름(DELE_A1 등) 반환
    private val levelPickLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val enumName = result.data?.getStringExtra("selected_level") ?: return@registerForActivityResult
            // enum 이름 → 표시명 변환 (예: DELE_A1 → "DELE A1")
            val displayName = runCatching { Level.valueOf(enumName).displayName }
                .getOrNull() ?: enumName
            startActivity(
                Intent(requireContext(), TestActivity::class.java).apply {
                    putExtra("mode", "level")
                    putExtra("level", displayName)
                }
            )
        }
    }

    // 챕터 선택 결과 받기: intent에 "selected_chapter_id" 로 챕터 id 반환
    private val chapterPickLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val chapterId = result.data?.getIntExtra("selected_chapter_id", -1) ?: -1
            if (chapterId != -1) {
                startActivity(
                    Intent(requireContext(), TestActivity::class.java).apply {
                        putExtra("mode", "chapter")
                        putExtra("chapterId", chapterId)
                    }
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 툴바 타이틀 보정 및 레벨 선택 UI 숨김(메인 정책과 동일)
        requireActivity().findViewById<TextView>(R.id.toolbarTitle)?.text = "테스트 방식 선택"
        (requireActivity() as? MainActivity)?.showLevelSelector(false)

        // 카드: 레벨별 테스트
        view.findViewById<CardView>(R.id.cardLevel).setOnClickListener {
            val i = Intent(requireContext(), LevelSelectActivity::class.java)
            i.putExtra("forTest", true) // ★ 테스트 모드로 실행 (선택 결과만 반환)
            levelPickLauncher.launch(i)
        }

        // 카드: 챕터별 테스트
        view.findViewById<CardView>(R.id.cardChapter).setOnClickListener {
            val i = Intent(requireContext(), ChapterSelectActivity::class.java)
            i.putExtra("forTest", true) // ★ 테스트용 챕터 선택
            chapterPickLauncher.launch(i)
        }

        // 카드: 즐겨찾기 테스트
        view.findViewById<CardView>(R.id.cardFavorite).setOnClickListener {
            startActivity(
                Intent(requireContext(), TestActivity::class.java).apply {
                    putExtra("mode", "favorite")
                }
            )
        }
    }
}