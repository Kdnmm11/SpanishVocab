package com.example.spanishvocab.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.spanishvocab.R
import com.example.spanishvocab.data.Word

class WordAdapter(
    private var words: List<Word>,
    private val onWordClick: (Word) -> Unit,
    private val onFavoriteClick: (Word) -> Unit
) : RecyclerView.Adapter<WordAdapter.WordViewHolder>() {

    class WordViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.cardWord)
        val spanishText: TextView = view.findViewById(R.id.textSpanish)
        val pronunciationText: TextView = view.findViewById(R.id.textPronunciation)
        val meaningText: TextView = view.findViewById(R.id.textMeaning)
        val favoriteButton: ImageButton = view.findViewById(R.id.buttonFavorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_word, parent, false)
        return WordViewHolder(view)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val word = words[position]
        holder.spanishText.text = word.spanish
        holder.pronunciationText.text = "[${word.pronunciation}]"
        holder.meaningText.text = word.meanings.joinToString(", ")

        // 즐겨찾기 버튼 상태 설정
        val favoriteIcon = if (word.isFavorite) {
            R.drawable.ic_favorite_filled
        } else {
            R.drawable.ic_favorite_outline
        }
        holder.favoriteButton.setImageResource(favoriteIcon)

        holder.cardView.setOnClickListener {
            onWordClick(word)
        }

        holder.favoriteButton.setOnClickListener {
            onFavoriteClick(word)
        }
    }

    override fun getItemCount() = words.size

    fun updateWords(newWords: List<Word>) {
        words = newWords
        notifyDataSetChanged()
    }
}
