package com.example.spanishvocab

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.spanishvocab.data.VocabData
import com.example.spanishvocab.data.Word
import java.util.Locale

class WordDetailActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var toolbarTitle: TextView

    private lateinit var wordCard: CardView
    private lateinit var spanishText: TextView
    private lateinit var pronunciationText: TextView
    private lateinit var meaningText: TextView
    private lateinit var exampleText: TextView
    private lateinit var exampleTranslationText: TextView

    private lateinit var favoriteButton: ImageButton
    private lateinit var pronounceButton: ImageButton
    private lateinit var pronExampleButton: ImageButton
    private lateinit var previousButton: ImageButton
    private lateinit var nextButton: ImageButton

    private var tts: TextToSpeech? = null
    private lateinit var currentWord: Word
    private lateinit var wordsList: ArrayList<Word>
    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_detail)

        // 입력 데이터
        currentWord = intent.getParcelableExtra("word") ?: return finish()
        wordsList = intent.getParcelableArrayListExtra("words") ?: arrayListOf()
        currentIndex = wordsList.indexOf(currentWord).coerceAtLeast(0)

        // 뷰 바인딩
        toolbar = findViewById(R.id.toolbar)
        toolbarTitle = findViewById(R.id.toolbarTitle)

        wordCard = findViewById(R.id.cardWord)
        spanishText = findViewById(R.id.textSpanish)
        pronunciationText = findViewById(R.id.textPronunciation)
        meaningText = findViewById(R.id.textMeaning)
        exampleText = findViewById(R.id.textExample)
        exampleTranslationText = findViewById(R.id.textExampleTranslation)

        favoriteButton = findViewById(R.id.buttonFavorite)
        pronounceButton = findViewById(R.id.buttonPronounce)
        pronExampleButton = findViewById(R.id.buttonPronounceExample)
        previousButton = findViewById(R.id.buttonPrevious)
        nextButton = findViewById(R.id.buttonNext)

        // 툴바(중앙 타이틀 TextView 사용)
        setSupportActionBar(toolbar)
        supportActionBar?.title = null
        toolbarTitle.text = "단어 상세"

        // TTS
        tts = TextToSpeech(this, this)

        // 클릭/눌림효과
        setupClickersWithPressEffect()

        // 최초 표시
        displayWord(wordsList[currentIndex])
    }

    // 눌림(축소-복원) 효과 공통
    private fun View.applyPressEffect() {
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

    private fun setupClickersWithPressEffect() {
        // 눌림 효과
        pronounceButton.applyPressEffect()
        pronExampleButton.applyPressEffect()
        favoriteButton.applyPressEffect()
        previousButton.applyPressEffect()
        nextButton.applyPressEffect()
        wordCard.applyPressEffect()

        // 클릭 리스너
        pronounceButton.setOnClickListener {
            speak(currentWord.spanish)
        }
        pronExampleButton.setOnClickListener {
            speak(currentWord.example)
        }
        favoriteButton.setOnClickListener {
            toggleFavorite()
        }
        previousButton.setOnClickListener {
            showPreviousWord()
        }
        nextButton.setOnClickListener {
            showNextWord()
        }
    }

    private fun speak(text: String?) {
        val t = text?.trim()
        if (!t.isNullOrEmpty()) {
            tts?.speak(t, TextToSpeech.QUEUE_FLUSH, null, null)
        }
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
        wordsList[currentIndex].isFavorite = currentWord.isFavorite
        // 전역 데이터와 동기화
        VocabData.getChapters().forEach { chapter ->
            chapter.words.find { it.id == currentWord.id }?.isFavorite = currentWord.isFavorite
        }
        updateFavoriteButton()
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

    override fun onBackPressed() {
        val resultIntent = Intent()
        resultIntent.putParcelableArrayListExtra("words", wordsList)
        setResult(Activity.RESULT_OK, resultIntent)
        super.onBackPressed()
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}