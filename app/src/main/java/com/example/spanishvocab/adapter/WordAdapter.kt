package com.example.spanishvocab.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.spanishvocab.R
import com.example.spanishvocab.data.Word

class WordAdapter(
    var words: List<Word>, // var로 변경 (갱신 용이)
    private val onPronounceClick: (Word) -> Unit,
    private val onFavoriteClick: (Word) -> Unit,
    private val onItemClick: (Word) -> Unit
) : RecyclerView.Adapter<WordAdapter.WordViewHolder>() {

    val currentItems: List<Word>
        get() = words

    class WordViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textSpanish: TextView = view.findViewById(R.id.textSpanish)
        val textMeaning: TextView = view.findViewById(R.id.textMeaning)
        val btnPronounce: ImageButton = view.findViewById(R.id.buttonPronounce)
        val btnFavorite: ImageButton = view.findViewById(R.id.buttonFavorite)

        // textPronunciation은 이제 사용하지 않으므로 연결하지 않아도 됩니다.
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_word, parent, false)
        return WordViewHolder(view)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val word = words[position]

        holder.textSpanish.text = word.spanish
        holder.textMeaning.text = word.meanings

        // ★ [수정됨] word.pronunciation 참조 코드 삭제함

        // 즐겨찾기 아이콘 상태 설정
        val iconRes = if (word.isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_outline
        holder.btnFavorite.setImageResource(iconRes)

        // 클릭 리스너 연결
        holder.btnPronounce.setOnClickListener { onPronounceClick(word) }
        holder.btnFavorite.setOnClickListener { onFavoriteClick(word) }
        holder.itemView.setOnClickListener { onItemClick(word) }
    }

    override fun getItemCount() = words.size

    fun updateWords(newWords: List<Word>) {
        this.words = newWords
        notifyDataSetChanged()
    }
}