package com.example.spanishvocab.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.spanishvocab.R
import com.example.spanishvocab.data.Chapter

class ChapterAdapter(
    private var chapters: List<Chapter>, // var로 변경하여 수정 가능하게 함
    private val onItemClick: (Chapter) -> Unit
) : RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder>() {

    class ChapterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.textChapterTitle)
        val count: TextView = view.findViewById(R.id.textWordCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chapter, parent, false)
        return ChapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChapterViewHolder, position: Int) {
        val chapter = chapters[position]

        holder.title.text = chapter.title
        holder.count.text = "${chapter.words.size} 단어"

        holder.itemView.setOnClickListener {
            onItemClick(chapter)
        }
    }

    override fun getItemCount() = chapters.size

    // ★ [추가됨] 리스트 갱신 함수
    fun updateChapters(newChapters: List<Chapter>) {
        this.chapters = newChapters
        notifyDataSetChanged()
    }
}