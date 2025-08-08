package com.example.spanishvocab

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.spanishvocab.data.Word
import java.util.*

class StudyActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var toolbar: Toolbar
    private lateinit var toolbarTitle: TextView
    private lateinit var progressText: TextView
    private lateinit var wordCard: CardView
    private lateinit var spanishText: TextView
    private lateinit var pronunciationText: TextView
    private lateinit var meaningLayout: View
    private lateinit var meaningText: TextView
    private lateinit var exampleLayout: View
    private lateinit var exampleText: TextView
    private lateinit var exampleTranslationText: TextView
    private lateinit var pronounceButton: ImageButton
    private lateinit var pronounceExampleButton: ImageButton
    private lateinit var favoriteButton: ImageButton
    private lateinit var showMeaningButton: Button
    private lateinit var showExampleButton: Button
    private lateinit var nextButton: Button
    private lateinit var previousButton: Button

    private var tts: TextToSpeech? = null
    private lateinit var wordsList: List<Word>
    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        // 상태바 색상(메인과 일관)
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
        wordCard = findViewById(R.id.cardWord)
        spanishText = findViewById(R.id.textSpanish)
        pronunciationText = findViewById(R.id.textPronunciation)
        meaningLayout = findViewById(R.id.layoutMeaning)
        meaningText = findViewById(R.id.textMeaning)
        exampleLayout = findViewById(R.id.layoutExample)
        exampleText = findViewById(R.id.textExample)
        exampleTranslationText = findViewById(R.id.textExampleTranslation)
        pronounceButton = findViewById(R.id.buttonPronounce)
        pronounceExampleButton = findViewById(R.id.buttonPronounceExample)
        favoriteButton = findViewById(R.id.buttonFavorite)
        showMeaningButton = findViewById(R.id.buttonShowMeaning)
        showExampleButton = findViewById(R.id.buttonShowExample)
        nextButton = findViewById(R.id.buttonNext)
        previousButton = findViewById(R.id.buttonPrevious)

        toolbarTitle.text = "Study Mode"

        pronounceButton.setOnClickListener { pronounceWord() }
        pronounceExampleButton.setOnClickListener { pronounceExample() }
        favoriteButton.setOnClickListener { toggleFavorite() }
        showMeaningButton.setOnClickListener { showMeaning() }
        showExampleButton.setOnClickListener { showExample() }
        nextButton.setOnClickListener { nextWord() }
        previousButton.setOnClickListener { previousWord() }
    }

    private fun setupTTS() {
        tts = TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale("es", "ES")
        }
    }

    private fun displayWord() {
        val word = wordsList[currentIndex]

        progressText.text = "${currentIndex + 1} / ${wordsList.size}"
        spanishText.text = word.spanish
        pronunciationText.text = "[${word.pronunciation}]"
        meaningText.text = word.meanings.joinToString("\n• ", "• ")
        exampleText.text = word.example
        exampleTranslationText.text = word.exampleTranslation

        // 초기 상태
        meaningLayout.visibility = View.GONE
        exampleLayout.visibility = View.GONE
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

    private fun pronounceExample() {
        val word = wordsList[currentIndex]
        if (word.example.isNotBlank()) {
            tts?.speak(word.example, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    private fun showMeaning() {
        meaningLayout.visibility = View.VISIBLE
        showMeaningButton.visibility = View.GONE
    }

    private fun showExample() {
        exampleLayout.visibility = View.VISIBLE
        showExampleButton.visibility = View.GONE
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

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}