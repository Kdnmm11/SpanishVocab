package com.example.spanishvocab.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.spanishvocab.R
import com.example.spanishvocab.data.Chapter

class ChapterAdapter(
    private var chapters: List<Chapter>,
    private val onChapterClick: (Chapter) -> Unit
) : RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder>() {

    class ChapterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.cardChapter)
        val titleText: TextView = view.findViewById(R.id.textChapterTitle)
        val descriptionText: TextView = view.findViewById(R.id.textChapterDescription)
        val wordCountText: TextView = view.findViewById(R.id.textWordCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chapter, parent, false)
        return ChapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChapterViewHolder, position: Int) {
        val chapter = chapters[position]
        holder.titleText.text = chapter.title
        holder.descriptionText.text = chapter.description
        holder.wordCountText.text = "${chapter.words.size} 단어"
        holder.cardView.setOnClickListener {
            onChapterClick(chapter)
        }
    }

    override fun getItemCount() = chapters.size

    // ★★ 반드시 아래 함수 추가! ★★
    fun updateChapters(newChapters: List<Chapter>) {
        chapters = newChapters
        notifyDataSetChanged()
    }
}
