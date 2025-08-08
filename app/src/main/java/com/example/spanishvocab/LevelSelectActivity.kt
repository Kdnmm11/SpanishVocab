package com.example.spanishvocab

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spanishvocab.adapter.LevelAdapter
import com.example.spanishvocab.data.Level
import com.example.spanishvocab.databinding.ActivityLevelSelectBinding

class LevelSelectActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLevelSelectBinding
    private lateinit var levelAdapter: LevelAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLevelSelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "" // 타이틀 자체는 숨기고 직접 배치

        // 레벨 리스트(카드)
        val levels = listOf(
            LevelInfo(Level.DELE_A1, "초급"),
            LevelInfo(Level.DELE_A2, "초중급"),
            LevelInfo(Level.DELE_B1, "중급"),
            LevelInfo(Level.DELE_B2, "고급")
        )
        levelAdapter = LevelAdapter(levels) { levelInfo ->
            saveLevel(levelInfo.level)
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
        binding.recyclerViewLevels.apply {
            layoutManager = LinearLayoutManager(this@LevelSelectActivity)
            adapter = levelAdapter
        }

        // 타이틀/서브 직접 세팅
        binding.textTitle.text = "DELE 레벨 선택"
        binding.textSubtitle.text = "나에게 맞는 스페인어 DELE 레벨을 골라주세요"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveLevel(level: Level) {
        getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .edit().putString("selected_level", level.name).apply()
    }

    data class LevelInfo(val level: Level, val desc: String) {
        val displayName: String get() = level.displayName
    }
}
