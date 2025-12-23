package com.example.spanishvocab.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.spanishvocab.data.Word

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "chapter_name") val chapterName: String,
    @ColumnInfo(name = "chapter_id") val chapterId: Int,
    val spanish: String,
    val meanings: String,
    val idiom: String,
    val example: String,
    val conjugation: String,
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean,

    // ★ [추가됨] 레벨 컬럼
    @ColumnInfo(name = "level") val level: String = "A1"
) {
    fun toDomainModel(): Word {
        return Word(
            id = id,
            spanish = spanish,
            meanings = meanings,
            idiom = idiom,
            example = example,
            conjugation = conjugation,
            isFavorite = isFavorite,
            chapterId = chapterId,
            level = level // ★ [추가됨] 여기서 Word 객체로 넘겨줌
        )
    }
}