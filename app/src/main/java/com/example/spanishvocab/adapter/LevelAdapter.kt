package com.example.spanishvocab.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.spanishvocab.R
import com.example.spanishvocab.LevelSelectActivity.LevelInfo

class LevelAdapter(
    private val levels: List<LevelInfo>,
    private val onLevelClick: (LevelInfo) -> Unit
) : RecyclerView.Adapter<LevelAdapter.LevelViewHolder>() {

    class LevelViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.cardLevel)
        val nameText: TextView = view.findViewById(R.id.textLevelName)
        val descText: TextView = view.findViewById(R.id.textLevelDesc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LevelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_level, parent, false)
        return LevelViewHolder(view)
    }

    override fun onBindViewHolder(holder: LevelViewHolder, position: Int) {
        val level = levels[position]
        holder.nameText.text = level.displayName
        holder.descText.text = level.desc
        holder.cardView.setOnClickListener { onLevelClick(level) }
    }

    override fun getItemCount() = levels.size
}
