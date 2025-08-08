package com.example.spanishvocab.adapter

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.spanishvocab.R
import com.example.spanishvocab.data.Word

class WordAdapter(
    private var words: List<Word>,
    private val onWordClick: (Word) -> Unit,
    private val onFavoriteClick: (Word) -> Unit,
    private val onPronounceClick: (Word) -> Unit
) : RecyclerView.Adapter<WordAdapter.WordVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_word, parent, false)
        return WordVH(v, onWordClick, onFavoriteClick, onPronounceClick)
    }

    override fun onBindViewHolder(holder: WordVH, position: Int) {
        holder.bind(words[position])
    }

    override fun getItemCount(): Int = words.size

    /** 외부에서 현재 표시 중인 리스트를 필요로 할 때 사용 (SearchActivity 등) */
    fun currentItems(): List<Word> = words

    fun updateWords(newWords: List<Word>) {
        words = newWords
        notifyDataSetChanged()
    }

    class WordVH(
        itemView: View,
        private val onWordClick: (Word) -> Unit,
        private val onFavoriteClick: (Word) -> Unit,
        private val onPronounceClick: (Word) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val card: CardView? = itemView.findViewById(R.id.cardWord)
        private val textSpanish: TextView? = itemView.findViewById(R.id.textSpanish)
        private val textPron: TextView? = itemView.findViewById(R.id.textPronunciation)
        private val textMeaningOneLine: TextView? = itemView.findViewById(R.id.textMeaning)
        private val btnFavorite: ImageButton? = itemView.findViewById(R.id.buttonFavorite)
        private val btnPronounce: ImageButton? = itemView.findViewById(R.id.buttonPronounce)

        fun bind(word: Word) {
            textSpanish?.text = word.spanish
            textPron?.text = "[${word.pronunciation}]"
            textMeaningOneLine?.text = word.meanings.firstOrNull()?.let { "• $it" } ?: ""

            (card ?: itemView).setOnClickListener { onWordClick(word) }
            applyPressEffect(card ?: itemView)

            btnFavorite?.apply {
                isFocusable = false
                isClickable = true
                setImageResource(
                    if (word.isFavorite) R.drawable.ic_favorite_filled
                    else R.drawable.ic_favorite_outline
                )
                setOnClickListener {
                    onFavoriteClick(word)
                    setImageResource(
                        if (word.isFavorite) R.drawable.ic_favorite_filled
                        else R.drawable.ic_favorite_outline
                    )
                }
                applyPressEffect(this)
            }

            btnPronounce?.apply {
                isFocusable = false
                isClickable = true
                setOnClickListener { onPronounceClick(word) }
                applyPressEffect(this)
            }
        }

        private fun applyPressEffect(v: View) {
            v.setOnTouchListener { view, e ->
                when (e.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        view.animate().scaleX(0.98f).scaleY(0.98f).setDuration(70).start()
                        view.alpha = 0.96f
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        view.animate().scaleX(1f).scaleY(1f).setDuration(90).start()
                        view.alpha = 1f
                    }
                }
                false
            }
        }
    }
}