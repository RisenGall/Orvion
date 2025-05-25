package com.mirackalyoncu.orvion.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gorev_log")
data class GorevLog(
    @PrimaryKey(autoGenerate = true)
    val logId: Int = 0,
    val gorevId: Int,
    val tarih: String,
    val aciklama: String
)