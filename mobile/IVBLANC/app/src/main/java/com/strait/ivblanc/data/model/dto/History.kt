package com.strait.ivblanc.data.model.dto

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class History(
    val createDate: String,
    val date: String,
    val location: Double,
    val field: Double,
    val historyId: Int,
    val photos: List<HistoryPhoto>,
    val styleUrl: String,
    val subject: String,
    val temperature_high: Int,
    val temperature_low: Int,
    val text: String,
    val updateDate: String,
    val userId: Int,
    val weather: String
): Parcelable