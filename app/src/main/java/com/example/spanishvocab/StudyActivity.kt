package com.example.spanishvocab

import android.graphics.Color
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.spanishvocab.data.Word
import java.util.*

class StudyActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var toolbar: Toolbar
    private lateinit var toolbarTitle: TextView
    private lateinit var progressText: TextView
    private lateinit var spanishText: TextView

    // 뜻 섹션
    private lateinit var showMeaningButton: Button
    private lateinit var layoutMeaningContainer: View
    private lateinit var meaningText: TextView
    private lateinit var layoutIdiom: View
    private lateinit var idiomText: TextView
    private lateinit var layoutConjugation: View
    private lateinit var conjugationText: TextView

    // 예문 섹션
    private lateinit var showExampleButton: Button
    private lateinit var layoutExampleContainer: View
    private lateinit var dynamicExampleContent: LinearLayout

    private lateinit var pronounceButton: ImageButton
    private lateinit var favoriteButton: ImageButton
    private lateinit var nextButton: Button
    private lateinit var previousButton: Button

    private var tts: TextToSpeech? = null
    private lateinit var wordsList: List<Word>
    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.primary_blue)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study)

        wordsList = intent.getParcelableArrayListExtra("words") ?: listOf()
        if (wordsList.isEmpty()) {
            Toast.makeText(this, "학습할 단어가 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupViews()
        setupTTS()
        displayWord()
    }

    private fun setupViews() {
        toolbar = findViewById(R.id.toolbar)
        toolbarTitle = findViewById(R.id.toolbarTitle)
        progressText = findViewById(R.id.textProgress)
        spanishText = findViewById(R.id.textSpanish)

        // 뜻 관련 뷰
        showMeaningButton = findViewById(R.id.buttonShowMeaning)
        layoutMeaningContainer = findViewById(R.id.layoutMeaningContainer)
        meaningText = findViewById(R.id.textMeaning)
        layoutIdiom = findViewById(R.id.layoutIdiom)
        idiomText = findViewById(R.id.textIdiom)
        layoutConjugation = findViewById(R.id.layoutConjugation)
        conjugationText = findViewById(R.id.textConjugation)

        // 예문 관련 뷰
        showExampleButton = findViewById(R.id.buttonShowExample)
        layoutExampleContainer = findViewById(R.id.layoutExampleContainer)
        dynamicExampleContent = findViewById(R.id.dynamicExampleContent)

        pronounceButton = findViewById(R.id.buttonPronounce)
        favoriteButton = findViewById(R.id.buttonFavorite)
        nextButton = findViewById(R.id.buttonNext)
        previousButton = findViewById(R.id.buttonPrevious)

        toolbarTitle.text = "Study Mode"

        pronounceButton.setOnClickListener { pronounceWord() }
        favoriteButton.setOnClickListener { toggleFavorite() }

        showMeaningButton.setOnClickListener { showMeaning() }
        showExampleButton.setOnClickListener { showExample() }

        nextButton.setOnClickListener { nextWord() }
        previousButton.setOnClickListener { previousWord() }
    }

    private fun displayWord() {
        val word = wordsList[currentIndex]

        progressText.text = "${currentIndex + 1} / ${wordsList.size}"
        spanishText.text = word.spanish

        meaningText.text = word.meanings

        if (word.idiom.isNotBlank()) {
            layoutIdiom.visibility = View.VISIBLE
            idiomText.text = word.idiom
        } else {
            layoutIdiom.visibility = View.GONE
        }

        if (word.conjugation.isNotBlank()) {
            layoutConjugation.visibility = View.VISIBLE
            conjugationText.text = word.conjugation
        } else {
            layoutConjugation.visibility = View.GONE
        }

        layoutMeaningContainer.visibility = View.GONE
        layoutExampleContainer.visibility = View.GONE
        showMeaningButton.visibility = View.VISIBLE
        showExampleButton.visibility = View.VISIBLE

        updateFavoriteButton()
        updateNavButtons()
    }

    private fun updateFavoriteButton() {
        val word = wordsList[currentIndex]
        val icon = if (word.isFavorite) R.drawable.ic_favorite_filled
        else R.drawable.ic_favorite_outline
        favoriteButton.setImageResource(icon)
    }

    private fun updateNavButtons() {
        previousButton.isEnabled = currentIndex > 0
        previousButton.alpha = if (previousButton.isEnabled) 1.0f else 0.3f
        nextButton.text = if (currentIndex < wordsList.size - 1) "다음" else "완료"
    }

    private fun toggleFavorite() {
        val word = wordsList[currentIndex]
        word.isFavorite = !word.isFavorite
        updateFavoriteButton()
    }

    private fun pronounceWord() {
        val word = wordsList[currentIndex]
        tts?.speak(word.spanish, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun showMeaning() {
        layoutMeaningContainer.visibility = View.VISIBLE
        showMeaningButton.visibility = View.GONE
    }

    private fun showExample() {
        layoutExampleContainer.visibility = View.VISIBLE
        showExampleButton.visibility = View.GONE

        // 예문 생성 (WordDetailActivity와 동일 로직)
        displaySmartExamples(wordsList[currentIndex].example)
    }

    private fun displaySmartExamples(fullExampleText: String) {
        dynamicExampleContent.removeAllViews()
        if (fullExampleText.isBlank()) return

        val lines = fullExampleText.split("\n")

        for (i in lines.indices) {
            val line = lines[i].trim()
            if (line.isBlank()) continue

            val isKorean = line.any { it.code in 0xAC00..0xD7A3 }
            val topMargin = if (isKorean) 0 else 16
            val finalTopMargin = if (dynamicExampleContent.childCount == 0) 0 else topMargin

            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, finalTopMargin, 0, 0)
                layoutParams = params
            }

            val textView = TextView(this).apply {
                text = line
                textSize = if (isKorean) 15f else 16f
                setTextColor(Color.parseColor(if (isKorean) "#666666" else "#333333"))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            rowLayout.addView(textView)

            if (!isKorean) {
                val speakBtn = ImageButton(this).apply {
                    setImageResource(R.drawable.ic_volume_up)
                    setColorFilter(ContextCompat.getColor(context, R.color.primary_blue))
                    setBackgroundResource(android.R.color.transparent)
                    layoutParams = LinearLayout.LayoutParams(60, 60)
                    setPadding(10, 10, 10, 10)
                    setOnClickListener {
                        val cleanText = line.replace(Regex("^\\d+\\."), "").trim()
                        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, null)
                    }
                }
                rowLayout.addView(speakBtn)
            }
            dynamicExampleContent.addView(rowLayout)
        }
    }

    private fun previousWord() {
        if (currentIndex > 0) {
            currentIndex--
            displayWord()
        }
    }

    private fun nextWord() {
        if (currentIndex < wordsList.size - 1) {
            currentIndex++
            displayWord()
        } else {
            finish()
        }
    }

    private fun setupTTS() { tts = TextToSpeech(this, this) }
    override fun onInit(status: Int) { if (status == TextToSpeech.SUCCESS) tts?.language = Locale("es", "ES") }
    override fun onDestroy() { tts?.stop(); tts?.shutdown(); super.onDestroy() }
}