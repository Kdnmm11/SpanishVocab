package com.example.spanishvocab.data.local

import androidx.room.ColumnInfo

// 데이터베이스에서 '챕터 아이디'와 '챕터 이름'만 쏙 뽑아올 때 담는 그릇입니다.
data class ChapterTuple(
    @ColumnInfo(name = "chapterId") val chapterId: Int,
    @ColumnInfo(name = "chapterName") val chapterName: String
)