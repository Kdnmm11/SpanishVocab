package com.example.spanishvocab.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WordDao {
    // [수정] chapterId -> chapter_id AS chapterId (매핑을 위해 별칭 사용)
    @Query("SELECT DISTINCT chapter_id AS chapterId, chapter_name AS chapterName FROM words")
    suspend fun getUniqueChapters(): List<ChapterTuple>

    // [수정] WHERE chapterId -> WHERE chapter_id
    @Query("SELECT * FROM words WHERE chapter_id = :chapterId")
    suspend fun getWordsByChapter(chapterId: Int): List<WordEntity>

    // [수정] WHERE chapterId -> WHERE chapter_id
    @Query("SELECT COUNT(*) FROM words WHERE chapter_id = :chapterId")
    suspend fun getWordCount(chapterId: Int): Int

    @Query("SELECT * FROM words")
    suspend fun getAllWords(): List<WordEntity>

    @Query("DELETE FROM words")
    suspend fun clearAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(words: List<WordEntity>)

    // [수정] SET isFavorite -> SET is_favorite
    @Query("UPDATE words SET is_favorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Int, isFavorite: Boolean)
}