package com.example.spanishvocab

import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.spanishvocab.data.VocabData
import com.example.spanishvocab.data.Word

class TestActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var progressText: TextView
    private lateinit var questionText: TextView
    private lateinit var optionsGroup: RadioGroup
    private lateinit var option1: RadioButton
    private lateinit var option2: RadioButton
    private lateinit var option3: RadioButton
    private lateinit var option4: RadioButton
    private lateinit var submitButton: Button

    private val allWords = mutableListOf<Word>()
    private val testWords = mutableListOf<Word>()
    private var currentQuestionIndex = 0
    private var correctAnswers = 0
    private var currentCorrectAnswer = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        setupViews()
        loadWords()
        startTest()
    }

    private fun setupViews() {
        toolbar = findViewById(R.id.toolbar)
        progressText = findViewById(R.id.textProgress)
        questionText = findViewById(R.id.textQuestion)
        optionsGroup = findViewById(R.id.optionsGroup)
        option1 = findViewById(R.id.option1)
        option2 = findViewById(R.id.option2)
        option3 = findViewById(R.id.option3)
        option4 = findViewById(R.id.option4)
        submitButton = findViewById(R.id.buttonSubmit)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Test"

        submitButton.setOnClickListener {
            checkAnswer()
        }
    }

    private fun loadWords() {
        val chapters = VocabData.getChapters()
        chapters.forEach { chapter ->
            allWords.addAll(chapter.words)
        }

        // 테스트용 단어 10개 랜덤 선택
        testWords.addAll(allWords.shuffled().take(10))
    }

    private fun startTest() {
        currentQuestionIndex = 0
        correctAnswers = 0
        showQuestion()
    }

    private fun showQuestion() {
        if (currentQuestionIndex >= testWords.size) {
            showResults()
            return
        }

        val currentWord = testWords[currentQuestionIndex]
        progressText.text = "Question ${currentQuestionIndex + 1} / ${testWords.size}"
        questionText.text = "What does '${currentWord.spanish}' mean?"

        // 정답과 오답 옵션 생성
        val correctMeaning = currentWord.meanings.first()
        val wrongMeanings = allWords
            .filter { it.id != currentWord.id }
            .map { it.meanings.first() }
            .shuffled()
            .take(3)

        val allOptions = (listOf(correctMeaning) + wrongMeanings).shuffled()
        currentCorrectAnswer = allOptions.indexOf(correctMeaning)

        option1.text = allOptions[0]
        option2.text = allOptions[1]
        option3.text = allOptions[2]
        option4.text = allOptions[3]

        optionsGroup.clearCheck()
        submitButton.text = "Submit"
    }

    private fun checkAnswer() {
        val selectedId = optionsGroup.checkedRadioButtonId
        if (selectedId == -1) {
            return // 아무것도 선택하지 않음
        }

        val selectedIndex = when (selectedId) {
            R.id.option1 -> 0
            R.id.option2 -> 1
            R.id.option3 -> 2
            R.id.option4 -> 3
            else -> -1
        }

        if (selectedIndex == currentCorrectAnswer) {
            correctAnswers++
        }

        currentQuestionIndex++

        if (currentQuestionIndex < testWords.size) {
            showQuestion()
        } else {
            showResults()
        }
    }

    private fun showResults() {
        val score = (correctAnswers.toFloat() / testWords.size * 100).toInt()
        val message = "Test completed!\n\nCorrect answers: $correctAnswers / ${testWords.size}\nScore: $score%"

        AlertDialog.Builder(this)
            .setTitle("Test Results")
            .setMessage(message)
            .setPositiveButton("OK") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
