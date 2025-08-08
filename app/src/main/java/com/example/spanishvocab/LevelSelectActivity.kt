package com.example.spanishvocab

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spanishvocab.data.Level

class LevelSelectActivity : AppCompatActivity() {

    private var forTest: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_level_select)

        forTest = intent.getBooleanExtra("forTest", false)

        // 툴바 타이틀(중앙 TextView)
        findViewById<TextView>(R.id.toolbarTitle).text =
            if (forTest) "레벨 선택 (테스트)" else "레벨 선택"

        val recycler = findViewById<RecyclerView>(R.id.recyclerViewLevels)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = SimpleLevelAdapter(Level.values().toList()) { level ->
            if (forTest) {
                // 테스트 모드: 전역 상태 저장 없이 선택 결과만 반환
                setResult(RESULT_OK, Intent().putExtra("selected_level", level.name))
                finish()
            } else {
                // 일반 모드: 전역 레벨 저장 후 메인으로
                saveLevel(level)
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }

    private fun saveLevel(level: Level) {
        getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .edit().putString("selected_level", level.name).apply()
    }
}

/** item_level.xml 용 간단 어댑터 */
class SimpleLevelAdapter(
    private val items: List<Level>,
    private val onClick: (Level) -> Unit
) : RecyclerView.Adapter<SimpleLevelVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleLevelVH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_level, parent, false)
        return SimpleLevelVH(v)
    }

    override fun onBindViewHolder(holder: SimpleLevelVH, position: Int) {
        holder.bind(items[position], onClick)
    }

    override fun getItemCount(): Int = items.size
}

class SimpleLevelVH(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
    private val name = itemView.findViewById<TextView>(R.id.textLevelName)
    private val desc = itemView.findViewById<TextView>(R.id.textLevelDesc)

    fun bind(level: Level, onClick: (Level) -> Unit) {
        name.text = level.displayName // 예: "DELE A1"
        desc.text = when (level.name) {
            "DELE_A1" -> "초급 A1"
            "DELE_A2" -> "초급 A2"
            "DELE_B1" -> "중급 B1"
            "DELE_B2" -> "중급 B2"
            else -> ""
        }
        itemView.setOnClickListener { onClick(level) }
    }
}