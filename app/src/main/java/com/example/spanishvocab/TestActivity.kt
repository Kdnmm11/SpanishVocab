package com.example.spanishvocab

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.spanishvocab.data.Word
import com.example.spanishvocab.repository.WordRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Collections

class TestActivity : AppCompatActivity() {

    private lateinit var repo: WordRepository
    private var wordList: List<Word> = emptyList()
    private var wrongAnswerPool: List<String> = emptyList()

    private var currentIndex = 0
    private var correctCount = 0
    private lateinit var currentWord: Word
    private var correctAnswerIndex: Int = 0

    // UI Views
    private lateinit var textQuestion: TextView
    private lateinit var textProgress: TextView
    private lateinit var optionsGroup: RadioGroup
    private lateinit var optionButtons: List<RadioButton>
    private lateinit var checkUnsure: CheckBox
    private lateinit var buttonSubmit: Button
    private lateinit var cardQuestion: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        repo = WordRepository(this)

        // 툴바 설정 (기존 뒤로가기 화살표 제거, 타이틀만 사용)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        initViews()
        loadData()
    }

    private fun initViews() {
        textQuestion = findViewById(R.id.textQuestion)
        textProgress = findViewById(R.id.textProgress)
        optionsGroup = findViewById(R.id.optionsGroup)
        checkUnsure  = findViewById(R.id.checkUnsure)
        buttonSubmit = findViewById(R.id.buttonSubmit)
        cardQuestion = findViewById(R.id.cardQuestion)

        // 커스텀 뒤로가기 버튼 연결
        findViewById<View>(R.id.btnBack).setOnClickListener {
            finish()
        }

        optionButtons = listOf(
            findViewById(R.id.option1),
            findViewById(R.id.option2),
            findViewById(R.id.option3),
            findViewById(R.id.option4)
        )

        buttonSubmit.setOnClickListener { checkAnswer() }
    }

    private fun loadData() {
        val mode = intent.getStringExtra("mode") ?: "favorite"

        CoroutineScope(Dispatchers.IO).launch {
            val fetchedWords = when (mode) {
                "chapter" -> {
                    val chapterId = intent.getIntExtra("chapterId", -1)
                    repo.getWords(chapterId)
                }
                "level" -> repo.getAllWordsRandom() // 레벨 모드지만 임시로 전체 랜덤 (필요시 구현)
                "favorite" -> repo.getFavoriteWords()
                else -> emptyList()
            }

            // 오답 보기용 풀 생성
            val allWords = repo.getAllWordsRandom()
            wrongAnswerPool = allWords.map { it.meanings }

            withContext(Dispatchers.Main) {
                if (fetchedWords.isNotEmpty()) {
                    // ★ 랜덤 20문제만 추출
                    wordList = fetchedWords.shuffled().take(20)
                    currentIndex = 0
                    correctCount = 0
                    showQuestion()
                } else {
                    Toast.makeText(this@TestActivity, "테스트할 단어가 없습니다.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun showQuestion() {
        if (currentIndex >= wordList.size) {
            showResult()
            return
        }

        currentWord = wordList[currentIndex]

        textProgress.text = "Question ${currentIndex + 1} / ${wordList.size}"
        textQuestion.text = "What does '${currentWord.spanish}' mean?"

        optionsGroup.clearCheck()
        optionsGroup.visibility = View.VISIBLE
        checkUnsure.isChecked = false
        buttonSubmit.isEnabled = true
        buttonSubmit.text = "제출"

        // 보기 색상 초기화
        optionButtons.forEach {
            it.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
            it.buttonTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary_blue))
        }

        // 보기 생성 (정답 1 + 오답 3)
        val options = ArrayList<String>()
        options.add(currentWord.meanings)
        val wrongOptions = wrongAnswerPool.filter { it != currentWord.meanings }.shuffled().take(3)
        options.addAll(wrongOptions)
        while (options.size < 4) options.add("-")
        options.shuffle()

        correctAnswerIndex = options.indexOf(currentWord.meanings)

        for (i in 0 until 4) {
            optionButtons[i].text = options[i]
        }
        // ★ 스피커 재생 코드 없음
    }

    private fun checkAnswer() {
        val selectedId = optionsGroup.checkedRadioButtonId

        if (selectedId == -1 && !checkUnsure.isChecked) {
            Toast.makeText(this, "정답을 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // 정답 버튼은 초록색으로 표시
        val correctButton = optionButtons[correctAnswerIndex]
        correctButton.setTextColor(Color.parseColor("#4CAF50"))
        correctButton.buttonTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50"))

        if (checkUnsure.isChecked) {
            // 모를 때는 정답만 보여주고 넘어감
        } else {
            val selectedButton = findViewById<RadioButton>(selectedId)
            val selectedIndex = optionButtons.indexOf(selectedButton)

            if (selectedIndex == correctAnswerIndex) {
                correctCount++
            } else {
                // 틀린 버튼은 빨간색으로 표시
                selectedButton.setTextColor(Color.parseColor("#F44336"))
                selectedButton.buttonTintList = ColorStateList.valueOf(Color.parseColor("#F44336"))
            }
        }

        currentIndex++

        // ★ 0.4초 뒤 다음 문제 (빠른 진행)
        cardQuestion.postDelayed({
            showQuestion()
        }, 400)
    }

    private fun showResult() {
        textQuestion.text = "테스트 완료!\n점수: $correctCount / ${wordList.size}"
        optionsGroup.visibility = View.GONE
        checkUnsure.visibility = View.GONE
        buttonSubmit.text = "종료"
        buttonSubmit.setOnClickListener { finish() }
    }
}