package com.example.spanishvocab

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.spanishvocab.data.VocabData
import com.example.spanishvocab.data.Word
import java.util.*
import kotlin.math.min

class TestActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var toolbarTitle: TextView
    private lateinit var progressText: TextView
    private lateinit var questionText: TextView
    private lateinit var optionsGroup: RadioGroup
    private lateinit var option1: RadioButton
    private lateinit var option2: RadioButton
    private lateinit var option3: RadioButton
    private lateinit var option4: RadioButton
    private lateinit var checkUnsure: CheckBox
    private lateinit var submitButton: Button

    private var tts: TextToSpeech? = null

    private val sourceWords = mutableListOf<Word>()
    private val testWords = mutableListOf<Word>()

    private var currentQuestionIndex = 0
    private var correctAnswers = 0
    private var currentCorrectIndex = -1

    private data class Record(
        val word: Word,
        val isCorrect: Boolean,
        val chosenText: String,
        val correctText: String,
        val isUnsure: Boolean
    ) : java.io.Serializable

    private val records = mutableListOf<Record>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        setupViews()
        setupToolbarTitle()
        setupTTS()
        loadWordsByMode()
        startTest()
    }

    private fun setupViews() {
        toolbarTitle = findViewById(R.id.toolbarTitle)
        progressText = findViewById(R.id.textProgress)
        questionText = findViewById(R.id.textQuestion)
        optionsGroup = findViewById(R.id.optionsGroup)
        option1 = findViewById(R.id.option1)
        option2 = findViewById(R.id.option2)
        option3 = findViewById(R.id.option3)
        option4 = findViewById(R.id.option4)
        checkUnsure = findViewById(R.id.checkUnsure)
        submitButton = findViewById(R.id.buttonSubmit)

        // 눌림(축소-복원) 효과
        fun View.applyPressEffect() {
            isClickable = true
            isFocusable = true
            setOnTouchListener { v, e ->
                when (e.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        v.animate().scaleX(0.98f).scaleY(0.98f).setDuration(70).start()
                        v.alpha = 0.96f
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        v.animate().scaleX(1f).scaleY(1f).setDuration(90).start()
                        v.alpha = 1f
                    }
                }
                false
            }
        }
        submitButton.applyPressEffect()
        option1.applyPressEffect()
        option2.applyPressEffect()
        option3.applyPressEffect()
        option4.applyPressEffect()

        submitButton.setOnClickListener { checkAnswerAndNext() }
    }

    private fun setupToolbarTitle() {
        val mode = intent.getStringExtra("mode") ?: ""
        val title = when (mode) {
            "favorite" -> "즐겨찾기 테스트"
            "level"    -> "레벨별 테스트"
            "chapter"  -> "챕터별 테스트"
            else       -> "단어 테스트"
        }
        toolbarTitle.text = title
    }

    private fun setupTTS() {
        tts = TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale("es", "ES")
        }
    }

    /** 모드에 따라 출제 풀 구성 */
    private fun loadWordsByMode() {
        val mode = intent.getStringExtra("mode") ?: ""

        when (mode) {
            "favorite" -> {
                val favs = VocabData.getFavoriteWords()
                sourceWords.clear()
                sourceWords.addAll(favs)
            }
            "level" -> {
                val levelName = intent.getStringExtra("level")
                val chapters = VocabData.getChapters().filter { c ->
                    val dn = c.level.displayName
                    dn == levelName || dn.replace("DELE ", "") == levelName
                }
                sourceWords.clear()
                chapters.forEach { sourceWords.addAll(it.words) }
            }
            "chapter" -> {
                val chapterId = intent.getIntExtra("chapterId", -1)
                val chapter = VocabData.getChapters().find { it.id == chapterId }
                sourceWords.clear()
                if (chapter != null) sourceWords.addAll(chapter.words)
            }
            else -> {
                sourceWords.clear()
                VocabData.getChapters().forEach { sourceWords.addAll(it.words) }
            }
        }

        if (sourceWords.isEmpty()) {
            Toast.makeText(this, "출제할 단어가 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        testWords.clear()
        val count = min(10, sourceWords.size)
        testWords.addAll(sourceWords.shuffled().take(count))
    }

    private fun startTest() {
        currentQuestionIndex = 0
        correctAnswers = 0
        records.clear()
        showQuestion()
    }

    private fun showQuestion() {
        if (currentQuestionIndex >= testWords.size) {
            showResultDialog()
            return
        }

        val word = testWords[currentQuestionIndex]
        progressText.text = "Question ${currentQuestionIndex + 1} / ${testWords.size}"
        questionText.text = "What does '${word.spanish}' mean?"

        val correct = word.meanings.firstOrNull().orEmpty()

        val allPool = VocabData.getChapters().flatMap { it.words }.distinctBy { it.id }
        val wrongs = allPool
            .filter { it.id != word.id }
            .mapNotNull { it.meanings.firstOrNull() }
            .shuffled()
            .take(3)

        val paddedWrongs = if (wrongs.size < 3) {
            wrongs + List(3 - wrongs.size) { "—" }
        } else wrongs

        val options = (listOf(correct) + paddedWrongs).take(4).shuffled()
        currentCorrectIndex = options.indexOf(correct)

        option1.text = options[0]
        option2.text = options[1]
        option3.text = options[2]
        option4.text = options[3]

        optionsGroup.clearCheck()
        checkUnsure.isChecked = false
        submitButton.text = if (currentQuestionIndex == testWords.size - 1) "Finish" else "Submit"
    }

    private fun checkAnswerAndNext() {
        val selectedId = optionsGroup.checkedRadioButtonId
        if (selectedId == -1) {
            Toast.makeText(this, "정답을 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedIndex = when (selectedId) {
            R.id.option1 -> 0
            R.id.option2 -> 1
            R.id.option3 -> 2
            R.id.option4 -> 3
            else -> -1
        }

        val word = testWords[currentQuestionIndex]
        val options = listOf(
            option1.text.toString(),
            option2.text.toString(),
            option3.text.toString(),
            option4.text.toString()
        )

        val isCorrect = (selectedIndex == currentCorrectIndex)
        if (isCorrect) correctAnswers++

        records += Record(
            word = word,
            isCorrect = isCorrect,
            chosenText = options.getOrNull(selectedIndex).orEmpty(),
            correctText = options.getOrNull(currentCorrectIndex).orEmpty(),
            isUnsure = checkUnsure.isChecked
        )

        currentQuestionIndex++
        showQuestion()
    }

    private fun showResultDialog() {
        val total = testWords.size
        val score = (correctAnswers * 100f / total).toInt()

        val wrongList = records.filter { !it.isCorrect }.map { it.word }.distinctBy { it.id }
        val unsureList = records.filter { it.isUnsure }.map { it.word }.distinctBy { it.id }
        val allList = records.map { it.word }

        val msg = "Correct: $correctAnswers / $total ( $score% )\n" +
                "틀린 문항: ${wrongList.size}개, 체크: ${unsureList.size}개"

        AlertDialog.Builder(this)
            .setTitle("결과")
            .setMessage(msg)
            .setPositiveButton("전체 보기") { _, _ ->
                if (allList.isEmpty()) {
                    Toast.makeText(this, "보기 항목이 없습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    // ⚠ ReviewActivity.start는 (context, title, words) 시그니처만 지원
                    ReviewActivity.start(this, "전체 복습", ArrayList(allList))
                }
            }
            .setNeutralButton("체크한 것만") { _, _ ->
                if (unsureList.isEmpty()) {
                    Toast.makeText(this, "체크한 항목이 없습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    ReviewActivity.start(this, "체크 복습", ArrayList(unsureList))
                }
            }
            .setNegativeButton("틀린 것만") { _, _ ->
                if (wrongList.isEmpty()) {
                    Toast.makeText(this, "틀린 항목이 없습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    ReviewActivity.start(this, "오답 복습", ArrayList(wrongList))
                }
            }
            .setOnDismissListener { finish() }
            .show()
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}