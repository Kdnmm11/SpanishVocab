package com.example.spanishvocab

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spanishvocab.data.Word
import java.util.Locale

class ReviewActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    companion object {
        fun start(context: Context, title: String, words: ArrayList<Word>) {
            val i = android.content.Intent(context, ReviewActivity::class.java)
            i.putExtra("title", title)
            i.putParcelableArrayListExtra("words", words)
            context.startActivity(i)
        }

        fun start(
            context: Context,
            title: String,
            words: ArrayList<Word>,
            chosenMap: HashMap<Int, String>,
            incorrectIds: HashSet<Int>
        ) {
            val i = android.content.Intent(context, ReviewActivity::class.java)
            i.putExtra("title", title)
            i.putParcelableArrayListExtra("words", words)
            i.putExtra("chosen", chosenMap)
            i.putExtra("incorrect", incorrectIds)
            context.startActivity(i)
        }
    }

    private var tts: TextToSpeech? = null
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: ReviewAdapter
    private var items: List<Word> = emptyList()

    private var chosenMap: HashMap<Int, String> = hashMapOf()
    private var incorrectSet: HashSet<Int> = hashSetOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        findViewById<TextView>(R.id.toolbarTitle).text =
            intent.getStringExtra("title") ?: "복습"

        items = intent.getParcelableArrayListExtra<Word>("words") ?: arrayListOf()
        @Suppress("UNCHECKED_CAST")
        chosenMap = intent.getSerializableExtra("chosen") as? HashMap<Int, String> ?: hashMapOf()
        @Suppress("UNCHECKED_CAST")
        incorrectSet = intent.getSerializableExtra("incorrect") as? HashSet<Int> ?: hashSetOf()

        recycler = findViewById(R.id.recyclerViewReview)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = ReviewAdapter(
            items,
            onPronounceWord = { word ->
                tts?.speak(word.spanish, TextToSpeech.QUEUE_FLUSH, null, null)
            },
            onPronounceExample = { word ->
                if (word.example.isNotBlank()) {
                    tts?.speak(word.example, TextToSpeech.QUEUE_FLUSH, null, null)
                }
            },
            onToggleFavorite = { word ->
                word.isFavorite = !word.isFavorite
                adapter.notifyItemChanged(items.indexOf(word))
            },
            chosenMap = chosenMap,
            incorrectSet = incorrectSet
        )
        recycler.adapter = adapter

        tts = TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale("es", "ES")
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

/* ---------- Adapter / ViewHolder ---------- */

private class ReviewAdapter(
    private val data: List<Word>,
    private val onPronounceWord: (Word) -> Unit,
    private val onPronounceExample: (Word) -> Unit,
    private val onToggleFavorite: (Word) -> Unit,
    private val chosenMap: Map<Int, String>,
    private val incorrectSet: Set<Int>
) : RecyclerView.Adapter<ReviewVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewVH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review_word, parent, false)
        return ReviewVH(v, onPronounceWord, onPronounceExample, onToggleFavorite)
    }

    override fun onBindViewHolder(holder: ReviewVH, position: Int) {
        val w = data[position]
        val chosen = chosenMap[w.id]
        val isIncorrect = incorrectSet.contains(w.id)
        holder.bind(w, chosen, isIncorrect)
    }

    override fun getItemCount(): Int = data.size
}

private class ReviewVH(
    itemView: View,
    private val onPronounceWord: (Word) -> Unit,
    private val onPronounceExample: (Word) -> Unit,
    private val onToggleFavorite: (Word) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    private val textSpanish = itemView.findViewById<TextView>(R.id.textSpanish)
    private val textPronunciation = itemView.findViewById<TextView>(R.id.textPronunciation)
    private val textMeaning = itemView.findViewById<TextView>(R.id.textMeaning)
    private val textExampleLabel = itemView.findViewById<TextView>(R.id.textExampleLabel)
    private val textExample = itemView.findViewById<TextView>(R.id.textExample)
    private val textExampleTranslation = itemView.findViewById<TextView>(R.id.textExampleTranslation)
    private val textChosen = itemView.findViewById<TextView>(R.id.textChosen)

    private val btnSpeakWord = itemView.findViewById<ImageButton>(R.id.buttonPronounce)
    private val btnSpeakExample = itemView.findViewById<ImageButton>(R.id.buttonSpeakExample)
    private val btnFavorite = itemView.findViewById<ImageButton>(R.id.buttonFavorite)

    fun bind(word: Word, chosenText: String?, isIncorrect: Boolean) {
        textSpanish.text = word.spanish

        // ★ [수정] 발음 필드 삭제로 인한 에러 해결 -> 뷰 숨김 처리
        // textPronunciation.text = "[${word.pronunciation}]"
        textPronunciation.visibility = View.GONE

        textMeaning.text = word.meanings

        textExampleLabel.text = "예문"
        textExample.text = word.example

        // ★ [수정] 예문 해석 필드 삭제로 인한 에러 해결 -> 뷰 숨김 처리
        // textExampleTranslation.text = word.exampleTranslation
        textExampleTranslation.visibility = View.GONE

        if (isIncorrect && !chosenText.isNullOrBlank()) {
            textChosen.visibility = View.VISIBLE
            textChosen.text = "내가 고른 답: $chosenText"
        } else {
            textChosen.visibility = View.GONE
        }

        btnFavorite.setImageResource(
            if (word.isFavorite) R.drawable.ic_favorite_filled
            else R.drawable.ic_favorite_outline
        )

        btnSpeakWord.setOnClickListener { onPronounceWord(word) }
        btnSpeakExample.setOnClickListener { onPronounceExample(word) }
        btnFavorite.setOnClickListener { onToggleFavorite(word) }
    }
}