package com.example.spanishvocab.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Word(
    val id: Int,
    val spanish: String,
    val pronunciation: String,
    val meanings: List<String>,
    val example: String,
    val exampleTranslation: String,
    val chapterId: Int,
    var isFavorite: Boolean = false,
    var isMastered: Boolean = false
) : Parcelable
