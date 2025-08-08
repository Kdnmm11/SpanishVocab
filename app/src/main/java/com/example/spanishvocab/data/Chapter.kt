package com.example.spanishvocab.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Chapter(
    val id: Int,
    val level: Level,   // 이 필드 추가
    val title: String,
    val description: String,
    val words: List<Word>
) : Parcelable
