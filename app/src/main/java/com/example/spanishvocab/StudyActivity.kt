package com.example.spanishvocab

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import com.example.spanishvocab.data.Word
import java.util.*

class StudyActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var toolbar: Toolbar
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
    private lateinit var favoriteButton: ImageButton
    private lateinit var showMeaningButton: Button
    private lateinit var showExampleButton: Button
    private lateinit var nextButton: Button
    private lateinit var previousButton: Button

    private var tts: TextToSpeech? = null
    private lateinit var wordsList: List<Word>
    private var currentIndex = 0
    private var showingMeaning = false
    private var showingExample = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study)

        wordsList = intent.getParcelableArrayListExtra("words") ?: listOf()
        if (wordsList.isEmpty()) {
            finish()
            return
        }

        setupViews()
        setupTTS()
        displayWord()
    }

    private fun setupViews() {
        toolbar = findViewById(R.id.toolbar)
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
        favoriteButton = findViewById(R.id.buttonFavorite)
        showMeaningButton = findViewById(R.id.buttonShowMeaning)
        showExampleButton = findViewById(R.id.buttonShowExample)
        nextButton = findViewById(R.id.buttonNext)
        previousButton = findViewById(R.id.buttonPrevious)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Study Mode"

        setupClickListeners()
    }

    private fun setupClickListeners() {
        pronounceButton.setOnClickListener {
            pronounceWord()
        }

        favoriteButton.setOnClickListener {
            toggleFavorite()
        }

        showMeaningButton.setOnClickListener {
            showMeaning()
        }

        showExampleButton.setOnClickListener {
            showExample()
        }

        nextButton.setOnClickListener {
            nextWord()
        }

        previousButton.setOnClickListener {
            previousWord()
        }
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

        // 상태 초기화
        showingMeaning = false
        showingExample = false
        meaningLayout.visibility = View.GONE
        exampleLayout.visibility = View.GONE
        showMeaningButton.visibility = View.VISIBLE
        showExampleButton.visibility = View.VISIBLE

        updateFavoriteButton()
        updateNavigationButtons()
    }

    private fun updateFavoriteButton() {
        val word = wordsList[currentIndex]
        val favoriteIcon = if (word.isFavorite) {
            R.drawable.ic_favorite_filled
        } else {
            R.drawable.ic_favorite_outline
        }
        favoriteButton.setImageResource(favoriteIcon)
    }

    private fun updateNavigationButtons() {
        previousButton.isEnabled = currentIndex > 0
        nextButton.text = if (currentIndex < wordsList.size - 1) "Next" else "Finish"
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
        showingMeaning = true
        meaningLayout.visibility = View.VISIBLE
        showMeaningButton.visibility = View.GONE
    }

    private fun showExample() {
        showingExample = true
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

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
