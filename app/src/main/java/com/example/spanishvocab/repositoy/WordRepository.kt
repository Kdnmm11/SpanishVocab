package com.example.spanishvocab.repository

import android.content.Context
import com.example.spanishvocab.data.Chapter
import com.example.spanishvocab.data.Level
import com.example.spanishvocab.data.Word
import com.example.spanishvocab.data.local.AppDatabase
import com.example.spanishvocab.data.local.WordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.ArrayList

class WordRepository(context: Context) {
    private val wordDao = AppDatabase.getDatabase(context).wordDao()
    private val client = OkHttpClient()

    private val GOOGLE_SHEET_URL = "https://docs.google.com/spreadsheets/d/e/2PACX-1vTGjpflVMYHtcsOJSIZqbGmIx4ApLSBsUQXkOgfwAWsOmMKkZx8w_EZlfbrMPcPW8F0qEMo0KcvyTm6/pub?gid=330646318&single=true&output=csv"

    // 구글 시트 동기화
    suspend fun syncWithGoogleSheet(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(GOOGLE_SHEET_URL).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) return@withContext Result.failure(Exception("다운로드 실패"))

            val csvData = response.body?.string() ?: return@withContext Result.failure(Exception("데이터 없음"))
            val newEntities = parseCsvRobust(csvData)

            if (newEntities.isNotEmpty()) {
                wordDao.clearAll()
                wordDao.insertAll(newEntities)
                Result.success("동기화 완료! (${newEntities.size}개 단어)")
            } else {
                Result.failure(Exception("데이터 없음"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun getChaptersFromDB(): List<Chapter> = withContext(Dispatchers.IO) {
        val tuples = wordDao.getUniqueChapters()
        tuples.map { tuple ->
            val count = wordDao.getWordCount(tuple.chapterId)
            val dummyWords = List(count) { Word(id=0, spanish="", meanings="") }
            Chapter(tuple.chapterId, Level.DELE_A1, tuple.chapterName, "학습하기", dummyWords)
        }
    }

    suspend fun getWords(chapterId: Int): List<Word> = withContext(Dispatchers.IO) {
        wordDao.getWordsByChapter(chapterId).map { it.toDomainModel() }
    }

    suspend fun getAllWords(): List<Word> = withContext(Dispatchers.IO) {
        wordDao.getAllWords().map { it.toDomainModel() }
    }

    // ★ [추가] 전체 단어 랜덤 가져오기 (퀴즈용)
    suspend fun getAllWordsRandom(): List<Word> = withContext(Dispatchers.IO) {
        wordDao.getAllWords().map { it.toDomainModel() }.shuffled()
    }

    // ★ [추가] 즐겨찾기 단어 가져오기 (퀴즈용)
    suspend fun getFavoriteWords(): List<Word> = withContext(Dispatchers.IO) {
        wordDao.getAllWords().map { it.toDomainModel() }.filter { it.isFavorite }
    }

    // ★ [추가] 레벨별 단어 가져오기 (딕테이션용)
    suspend fun getWordsByLevel(level: String): List<Word> = withContext(Dispatchers.IO) {
        // level 컬럼과 일치하는 단어만 필터링 (csv 파싱 시 A1, A2 등이 들어간다고 가정)
        wordDao.getAllWords().map { it.toDomainModel() }.filter { it.level.equals(level, ignoreCase = true) }
    }

    // ★ [추가] 즐겨찾기 업데이트
    suspend fun updateFavorite(word: Word) = withContext(Dispatchers.IO) {
        wordDao.updateFavorite(word.id, word.isFavorite)
    }

    // CSV 파싱 로직
    private fun parseCsvRobust(csv: String): List<WordEntity> {
        val entities = ArrayList<WordEntity>()
        val rows = ArrayList<List<String>>()
        var currentRow = ArrayList<String>()
        var currentCell = StringBuilder()
        var inQuotes = false

        for (i in 0 until csv.length) {
            val char = csv[i]
            if (char == '\"') {
                if (i + 1 < csv.length && csv[i + 1] == '\"') {
                    currentCell.append('\"')
                } else {
                    inQuotes = !inQuotes
                }
            } else if (char == ',' && !inQuotes) {
                currentRow.add(currentCell.toString().trim())
                currentCell = StringBuilder()
            } else if ((char == '\n' || char == '\r') && !inQuotes) {
                if (currentCell.isNotEmpty() || currentRow.isNotEmpty()) {
                    currentRow.add(currentCell.toString().trim())
                    rows.add(ArrayList(currentRow))
                    currentRow = ArrayList()
                    currentCell = StringBuilder()
                }
            } else {
                currentCell.append(char)
            }
        }
        if (currentCell.isNotEmpty() || currentRow.isNotEmpty()) {
            currentRow.add(currentCell.toString().trim())
            rows.add(currentRow)
        }

        val chapterIdMap = mutableMapOf<String, Int>()
        var nextChapterId = 1

        for ((index, row) in rows.withIndex()) {
            if (index == 0) continue
            if (row.size < 2) continue

            val chName = row[0].replace("\"", "")
            val spanish = row[1].replace("\"", "")
            if (chName.isBlank() || spanish.isBlank()) continue

            if (!chapterIdMap.containsKey(chName)) chapterIdMap[chName] = nextChapterId++
            val chId = chapterIdMap[chName]!!

            entities.add(WordEntity(
                chapterName = chName,
                chapterId = chId,
                spanish = spanish,
                meanings = row.getOrElse(2) { "" }.replace("\"", ""),
                idiom = row.getOrElse(3) { "" }.replace("\"", ""),
                example = row.getOrElse(4) { "" }.replace("\"", ""),
                conjugation = row.getOrElse(5) { "" }.replace("\"", ""),
                level = "A1", // ★ CSV에 레벨 컬럼이 없다면 기본값은 A1입니다.
                isFavorite = false
            ))
        }
        return entities
    }
}