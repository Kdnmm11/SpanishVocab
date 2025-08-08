package com.example.spanishvocab.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.spanishvocab.R
import com.example.spanishvocab.data.Level

class LevelAdapter(
    private val items: List<Level>,
    private val onClick: (Level) -> Unit
) : RecyclerView.Adapter<LevelAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.textLevelName) // "DELE A1"
        val desc: TextView = view.findViewById(R.id.textLevelDesc) // "초급"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_level, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val level = items[position]
        holder.name.text = level.displayName
        holder.desc.text = when (level) {
            Level.DELE_A1 -> "초급"
            Level.DELE_A2 -> "초중급"
            Level.DELE_B1 -> "중급"
            Level.DELE_B2 -> "고급"
        }

        // 카드에 ripple(클릭 피드백) 주려면 item_level.xml의 CardView에 foreground 이미 설정됨
        holder.itemView.setOnClickListener { onClick(level) }
        holder.itemView.contentDescription = "${holder.name.text}, ${holder.desc.text}"
    }

    override fun getItemCount(): Int = items.size
}