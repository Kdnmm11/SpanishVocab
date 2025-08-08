package com.example.spanishvocab

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import com.example.spanishvocab.data.Word
import java.util.*

class WordDetailActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var toolbar: Toolbar
    private lateinit var wordCard: CardView
    private lateinit var spanishText: TextView
    private lateinit var pronunciationText: TextView
    private lateinit var meaningText: TextView
    private lateinit var exampleText: TextView
    private lateinit var exampleTranslationText: TextView
    private lateinit var favoriteButton: ImageButton
    private lateinit var pronounceButton: ImageButton
    private lateinit var previousButton: ImageButton
    private lateinit var nextButton: ImageButton

    private var tts: TextToSpeech? = null
    private lateinit var currentWord: Word
    private lateinit var wordsList: ArrayList<Word>
    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_detail)

        currentWord = intent.getParcelableExtra("word") ?: return
        wordsList = intent.getParcelableArrayListExtra("words") ?: arrayListOf()
        currentIndex = wordsList.indexOf(currentWord)

        setupViews()
        setupTTS()
        displayWord(currentWord)
    }

    private fun setupViews() {
        toolbar = findViewById(R.id.toolbar)
        wordCard = findViewById(R.id.cardWord)
        spanishText = findViewById(R.id.textSpanish)
        pronunciationText = findViewById(R.id.textPronunciation)
        meaningText = findViewById(R.id.textMeaning)
        exampleText = findViewById(R.id.textExample)
        exampleTranslationText = findViewById(R.id.textExampleTranslation)
        favoriteButton = findViewById(R.id.buttonFavorite)
        pronounceButton = findViewById(R.id.buttonPronounce)
        previousButton = findViewById(R.id.buttonPrevious)
        nextButton = findViewById(R.id.buttonNext)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        favoriteButton.setOnClickListener {
            toggleFavorite()
        }

        pronounceButton.setOnClickListener {
            pronounceWord()
        }

        previousButton.setOnClickListener {
            showPreviousWord()
        }

        nextButton.setOnClickListener {
            showNextWord()
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

    private fun displayWord(word: Word) {
        currentWord = word

        spanishText.text = word.spanish
        pronunciationText.text = "[${word.pronunciation}]"
        meaningText.text = word.meanings.joinToString("\n• ", "• ")
        exampleText.text = word.example
        exampleTranslationText.text = word.exampleTranslation

        updateFavoriteButton()
        updateNavigationButtons()

        supportActionBar?.title = word.spanish
    }

    private fun updateFavoriteButton() {
        val favoriteIcon = if (currentWord.isFavorite) {
            R.drawable.ic_favorite_filled
        } else {
            R.drawable.ic_favorite_outline
        }
        favoriteButton.setImageResource(favoriteIcon)
    }

    private fun updateNavigationButtons() {
        previousButton.isEnabled = currentIndex > 0
        nextButton.isEnabled = currentIndex < wordsList.size - 1
    }

    private fun toggleFavorite() {
        currentWord.isFavorite = !currentWord.isFavorite
        updateFavoriteButton()
    }

    private fun pronounceWord() {
        tts?.speak(currentWord.spanish, TextToSpeech.QUEUE_FLUSH, null, null)
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
