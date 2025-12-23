package com.example.spanishvocab

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.spanishvocab.data.Word
import com.example.spanishvocab.repository.WordRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class WordDetailActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var textSpanish: TextView
    private lateinit var textMeaning: TextView
    private lateinit var textIdiom: TextView
    private lateinit var textConjugation: TextView
    private lateinit var textConjugationArrow: TextView

    private lateinit var layoutIdiom: LinearLayout
    private lateinit var layoutConjugationContainer: LinearLayout
    private lateinit var layoutConjugationHeader: LinearLayout
    private lateinit var layoutExampleContainer: LinearLayout

    private lateinit var buttonFavorite: ImageButton
    private lateinit var buttonPronounce: ImageButton
    private lateinit var buttonPrevious: Button
    private lateinit var buttonNext: Button

    private var tts: TextToSpeech? = null
    private lateinit var repo: WordRepository
    private lateinit var currentWord: Word
    private lateinit var wordsList: ArrayList<Word>
    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_detail)

        repo = WordRepository(this)

        currentWord = intent.getParcelableExtra("word") ?: return finish()
        wordsList = intent.getParcelableArrayListExtra("words") ?: arrayListOf()
        currentIndex = wordsList.indexOfFirst { it.id == currentWord.id }.coerceAtLeast(0)

        setupViews()
        setupTTS()

        displayWord(currentWord)
    }

    private fun setupViews() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = null

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            onBackPressed()
        }

        textSpanish = findViewById(R.id.textSpanish)
        textMeaning = findViewById(R.id.textMeaning)
        textIdiom = findViewById(R.id.textIdiom)
        textConjugation = findViewById(R.id.textConjugation)
        textConjugationArrow = findViewById(R.id.textConjugationArrow)

        layoutIdiom = findViewById(R.id.layoutIdiom)
        layoutConjugationContainer = findViewById(R.id.layoutConjugationContainer)
        layoutConjugationHeader = findViewById(R.id.layoutConjugationHeader)
        layoutExampleContainer = findViewById(R.id.layoutExampleContainer)

        buttonFavorite = findViewById(R.id.buttonFavorite)
        buttonPronounce = findViewById(R.id.buttonPronounce)
        buttonPrevious = findViewById(R.id.buttonPrevious)
        buttonNext = findViewById(R.id.buttonNext)

        buttonPronounce.setOnClickListener { speak(currentWord.spanish) }
        buttonFavorite.setOnClickListener { toggleFavorite() }
        buttonPrevious.setOnClickListener { showPreviousWord() }
        buttonNext.setOnClickListener { showNextWord() }

        layoutConjugationHeader.setOnClickListener {
            if (textConjugation.visibility == View.VISIBLE) {
                textConjugation.visibility = View.GONE
                textConjugationArrow.text = "▼"
            } else {
                textConjugation.visibility = View.VISIBLE
                textConjugationArrow.text = "▲"
            }
        }
    }

    private fun displayWord(word: Word) {
        currentWord = word

        textSpanish.text = word.spanish
        textMeaning.text = word.meanings

        if (word.idiom.isNotBlank()) {
            layoutIdiom.visibility = View.VISIBLE
            textIdiom.text = word.idiom
        } else {
            layoutIdiom.visibility = View.GONE
        }

        displaySmartExamples(word.example)

        if (word.conjugation.isNotBlank()) {
            layoutConjugationContainer.visibility = View.VISIBLE
            textConjugation.text = word.conjugation
            textConjugation.visibility = View.GONE
            textConjugationArrow.text = "▼"
        } else {
            layoutConjugationContainer.visibility = View.GONE
        }

        updateFavoriteButton()
        updateNavigationButtons()
    }

    /**
     * 예문 출력 함수: 번호와 텍스트의 상단 라인을 맞추고, 들여쓰기를 적용
     */
    private fun displaySmartExamples(fullExampleText: String) {
        layoutExampleContainer.removeAllViews()
        if (fullExampleText.isBlank()) return

        val lines = fullExampleText.split("\n")
        var lastContentContainer: LinearLayout? = null

        for (i in lines.indices) {
            val line = lines[i].trim()
            if (line.isBlank()) continue

            val isKorean = line.any { it.code in 0xAC00..0xD7A3 }

            if (!isKorean) {
                // ==========================
                // 1. 스페인어 행 (번호 + 내용)
                // ==========================

                val mainRow = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    // Gravity.TOP이 기본값이므로 별도 설정 불필요 (위쪽 정렬됨)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = if (layoutExampleContainer.childCount == 0) 0 else (16 * resources.displayMetrics.density).toInt()
                    }
                }

                val match = Regex("^(\\d+\\.)\\s*(.*)").find(line)
                var spanishText = line
                var numberPart = ""

                if (match != null) {
                    val (num, text) = match.destructured
                    numberPart = num
                    spanishText = text
                }

                // [좌측] 번호
                if (numberPart.isNotEmpty()) {
                    val tvNum = TextView(this).apply {
                        text = numberPart
                        textSize = 16f
                        setTextColor(Color.parseColor("#333333"))
                        includeFontPadding = false // ★ 폰트 여백 제거 (라인 맞춤 핵심)
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            rightMargin = (8 * resources.displayMetrics.density).toInt()
                        }
                    }
                    mainRow.addView(tvNum)
                }

                // [우측] 내용 컨테이너 (스페인어 + 한글)
                lastContentContainer = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }

                // 스페인어 텍스트 + 스피커 라인
                val spanishRow = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    // ★ 중요: 여기서 gravity = center_vertical을 빼야 텍스트가 위로 붙음
                }

                val tvSpanish = TextView(this).apply {
                    text = spanishText
                    textSize = 16f
                    setTextColor(Color.parseColor("#333333"))
                    includeFontPadding = false // ★ 폰트 여백 제거
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }

                val btnSpeak = ImageButton(this).apply {
                    setImageResource(R.drawable.ic_volume_up)
                    setColorFilter(ContextCompat.getColor(context, R.color.primary_blue))
                    setBackgroundResource(android.R.color.transparent)

                    val size = (32 * resources.displayMetrics.density).toInt()
                    val params = LinearLayout.LayoutParams(size, size)
                    // ★ 버튼만 수직 중앙 정렬 (텍스트 높이와 무관하게)
                    params.gravity = Gravity.CENTER_VERTICAL
                    layoutParams = params

                    setPadding(10, 10, 10, 10)
                    val textToSpeak = spanishText
                    setOnClickListener {
                        val cleanText = textToSpeak.replace(Regex("^\\d+\\."), "").trim()
                        speak(cleanText)
                    }
                }

                spanishRow.addView(tvSpanish)
                spanishRow.addView(btnSpeak)

                lastContentContainer!!.addView(spanishRow)
                mainRow.addView(lastContentContainer)
                layoutExampleContainer.addView(mainRow)

            } else {
                // ==========================
                // 2. 한글 행 (들여쓰기 적용됨)
                // ==========================
                if (lastContentContainer != null) {
                    val tvKorean = TextView(this).apply {
                        text = line
                        textSize = 14f
                        setTextColor(Color.parseColor("#888888"))
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            topMargin = (4 * resources.displayMetrics.density).toInt()
                        }
                    }
                    lastContentContainer!!.addView(tvKorean)
                } else {
                    val tvKorean = TextView(this).apply {
                        text = line
                        textSize = 14f
                        setTextColor(Color.parseColor("#888888"))
                    }
                    layoutExampleContainer.addView(tvKorean)
                }
            }
        }
    }

    private fun toggleFavorite() {
        currentWord.isFavorite = !currentWord.isFavorite
        wordsList[currentIndex].isFavorite = currentWord.isFavorite
        CoroutineScope(Dispatchers.IO).launch { repo.updateFavorite(currentWord) }
        updateFavoriteButton()
    }

    private fun updateFavoriteButton() {
        val icon = if (currentWord.isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_outline
        buttonFavorite.setImageResource(icon)
    }

    private fun updateNavigationButtons() {
        buttonPrevious.isEnabled = currentIndex > 0
        buttonNext.isEnabled = currentIndex < wordsList.size - 1
        buttonPrevious.alpha = if (buttonPrevious.isEnabled) 1.0f else 0.3f
        buttonNext.alpha = if (buttonNext.isEnabled) 1.0f else 0.3f
    }

    private fun showPreviousWord() {
        if (currentIndex > 0) {
            currentIndex--
            displayWord(wordsList[currentIndex])
        }
    }

    private fun showNextWord() {
        if (currentIndex < wordsList.size - 1) {
            currentIndex++
            displayWord(wordsList[currentIndex])
        }
    }

    private fun setupTTS() { tts = TextToSpeech(this, this) }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale("es", "ES")
            tts?.setSpeechRate(1.1f) // 기본 속도 1.1배
        }
    }

    private fun speak(text: String) { tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null) }

    override fun onBackPressed() {
        val resultIntent = Intent()
        resultIntent.putParcelableArrayListExtra("words", wordsList)
        setResult(Activity.RESULT_OK, resultIntent)
        super.onBackPressed()
    }

    override fun onDestroy() { tts?.stop(); tts?.shutdown(); super.onDestroy() }
}