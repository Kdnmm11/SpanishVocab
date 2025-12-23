package com.example.spanishvocab.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Word(
    val id: Int,
    val spanish: String,
    val meanings: String,
    val idiom: String = "",
    val example: String = "",
    val conjugation: String = "",
    var isFavorite: Boolean = false,
    val chapterId: Int = 0,
    val level: String = "A1" // ★ [추가됨] 레벨 정보 (기본값 A1)
) : Parcelable