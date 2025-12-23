package com.example.spanishvocab

import android.graphics.Color
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.spanishvocab.data.Word
import com.example.spanishvocab.repository.WordRepository
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class DictationActivity : AppCompatActivity() {

    private lateinit var repo: WordRepository
    private var sentenceList: List<Pair<String, String>> = emptyList()
    private var currentIndex = 0

    private lateinit var tts: TextToSpeech

    private lateinit var textTitle: TextView
    private lateinit var textProgress: TextView
    private lateinit var editInput: TextInputEditText
    private lateinit var textResult: TextView
    private lateinit var textTranslation: TextView
    private lateinit var btnCheck: Button
    private lateinit var btnGiveUp: Button
    private lateinit var btnPlay: ImageButton

    private lateinit var radioSpeed05: RadioButton
    private lateinit var radioSpeed08: RadioButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dictation)

        repo = WordRepository(this)
        initViews()
        initTTS()
        loadSentences()
    }

    private fun initViews() {
        textTitle = findViewById(R.id.textTitle)
        textProgress = findViewById(R.id.textProgress)
        editInput = findViewById(R.id.editInput)
        textResult = findViewById(R.id.textResult)
        textTranslation = findViewById(R.id.textTranslation)
        btnCheck = findViewById(R.id.btnCheck)
        btnGiveUp = findViewById(R.id.btnGiveUp)
        btnPlay = findViewById(R.id.btnPlay)

        radioSpeed05 = findViewById(R.id.radioSpeed05)
        radioSpeed08 = findViewById(R.id.radioSpeed08)

        val level = intent.getStringExtra("level") ?: "A2"
        textTitle.text = "딕테이션 ($level)"

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        btnPlay.setOnClickListener { speakCurrentSentence() }

        btnCheck.setOnClickListener {
            if (btnCheck.text == "다음 문제") {
                nextQuestion()
            } else {
                checkAnswer()
            }
        }

        btnGiveUp.setOnClickListener {
            showCorrectAnswer(true)
        }
    }

    private fun loadSentences() {
        val selectedLevel = intent.getStringExtra("level") ?: "A2"

        val targetPrefix = when (selectedLevel) {
            "A2" -> "1."
            "B1" -> "2."
            "B2" -> "3."
            else -> "1."
        }

        CoroutineScope(Dispatchers.IO).launch {
            val words = repo.getAllWordsRandom()
            val extracted = ArrayList<Pair<String, String>>()

            for (word in words) {
                if (word.example.isNotBlank()) {
                    val lines = word.example.split("\n").map { it.trim() }.filter { it.isNotBlank() }

                    for (i in 0 until lines.size - 1) {
                        val line = lines[i]
                        val nextLine = lines[i+1]

                        if (line.startsWith(targetPrefix)) {
                            val cleanSpanish = line.removePrefix(targetPrefix).trim()
                            val isSpanish = !cleanSpanish.any { it.code in 0xAC00..0xD7A3 }

                            if (isSpanish) {
                                extracted.add(Pair(cleanSpanish, nextLine))
                            }
                        }
                    }
                }
            }

            withContext(Dispatchers.Main) {
                if (extracted.isNotEmpty()) {
                    sentenceList = extracted.shuffled().take(15)
                    startQuestion()
                } else {
                    Toast.makeText(this@DictationActivity, "$selectedLevel ($targetPrefix)에 해당하는 예문이 없습니다.", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }

    private fun startQuestion() {
        if (currentIndex >= sentenceList.size) {
            Toast.makeText(this, "테스트 완료!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        textProgress.text = "${currentIndex + 1} / ${sentenceList.size}"
        editInput.text = null
        textResult.visibility = View.INVISIBLE
        textTranslation.visibility = View.INVISIBLE
        btnCheck.text = "정답 확인"
        btnGiveUp.visibility = View.VISIBLE
        btnCheck.isEnabled = true
        editInput.isEnabled = true
    }

    private fun speakCurrentSentence() {
        if (!::tts.isInitialized) return
        val text = sentenceList[currentIndex].first

        // ★ [핵심] 기본 속도를 1.1f로 고정 (버튼 미선택 시)
        val speed = when {
            radioSpeed05.isChecked -> 0.5f
            radioSpeed08.isChecked -> 0.8f
            else -> 1.1f
        }

        tts.setSpeechRate(speed)
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun checkAnswer() {
        val userInput = editInput.text.toString().trim()
        val correctSentence = sentenceList[currentIndex].first

        val normalize = { s: String -> s.replace(Regex("[^a-zA-ZáéíóúñÁÉÍÓÚÑ]"), "").lowercase() }

        if (normalize(userInput) == normalize(correctSentence)) {
            showCorrectAnswer(false)
        } else {
            val masked = createMaskedHint(correctSentence, userInput)
            textResult.text = masked
            textResult.setTextColor(Color.parseColor("#D32F2F"))
            textResult.visibility = View.VISIBLE

            Toast.makeText(this, "틀렸습니다. 힌트를 확인하세요.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createMaskedHint(target: String, user: String): String {
        val targetWords = target.split(" ")
        val userWords = user.split(" ")
        val sb = StringBuilder()

        for (i in targetWords.indices) {
            val tWord = targetWords[i]
            if (i >= userWords.size) {
                sb.append("*** ")
            } else {
                val uWord = userWords[i]
                val cleanT = tWord.replace(Regex("[^a-zA-Záéíóúñ]"), "").lowercase()
                val cleanU = uWord.replace(Regex("[^a-zA-Záéíóúñ]"), "").lowercase()
                if (cleanT == cleanU) sb.append("$tWord ") else sb.append("*** ")
            }
        }
        return sb.toString().trim() + "?"
    }

    private fun showCorrectAnswer(gaveUp: Boolean) {
        val correctSentence = sentenceList[currentIndex].first
        val translation = sentenceList[currentIndex].second

        textResult.text = correctSentence
        textResult.setTextColor(Color.parseColor("#388E3C"))
        textResult.visibility = View.VISIBLE

        textTranslation.text = translation
        textTranslation.visibility = View.VISIBLE

        editInput.isEnabled = false
        btnGiveUp.visibility = View.GONE
        btnCheck.text = "다음 문제"

        if (!gaveUp) {
            Toast.makeText(this, "정답입니다! 👏", Toast.LENGTH_SHORT).show()
        }
    }

    private fun nextQuestion() {
        currentIndex++
        startQuestion()
    }

    private fun initTTS() {
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale("es", "ES")
                // ★ 초기화 시에도 1.1배 설정
                tts.setSpeechRate(1.1f)
            }
        }
    }

    override fun onDestroy() {
        if (::tts.isInitialized) { tts.stop(); tts.shutdown() }
        super.onDestroy()
    }
}