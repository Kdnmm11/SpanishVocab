package com.example.spanishvocab.data

import com.example.spanishvocab.data.vocabdata.a1.*
import com.example.spanishvocab.data.vocabdata.a2.*

object VocabData {
    fun getChapters(): List<Chapter> {
        return listOf(
            Chapter(1, Level.DELE_A1, "인사", "A1: 기본적인 인사 표현", greetingWordsA1),
            Chapter(2, Level.DELE_A1, "가족", "A1: 가족 관계 단어", familyWordsA1),
            Chapter(3, Level.DELE_A1, "음식", "A1: 일상 음식 단어", foodWordsA1),
            Chapter(4, Level.DELE_A1, "시간", "A1: 시간/날짜 관련", timeWordsA1),
            Chapter(5, Level.DELE_A1, "장소", "A1: 주요 장소 단어", placeWordsA1),
            Chapter(6, Level.DELE_A2, "여행", "A2: 여행/교통 단어", travelWordsA2),
            Chapter(7, Level.DELE_A2, "쇼핑", "A2: 쇼핑/구매 단어", shoppingWordsA2),
            Chapter(8, Level.DELE_A2, "건강", "A2: 건강/병원 단어", healthWordsA2),
            Chapter(9, Level.DELE_A2, "학교", "A2: 학교/수업 단어", schoolWordsA2),
            Chapter(10, Level.DELE_A2, "날씨", "A2: 날씨/기후 단어", weatherWordsA2)
        )
    }

    fun getFavoriteWords(): List<Word> {
        return getChapters().flatMap { it.words }.filter { it.isFavorite }
    }
}
