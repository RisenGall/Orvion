package com.mirackalyoncu.orvion.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "gorev",
    foreignKeys = [
        ForeignKey(entity = Surec::class, parentColumns = ["surecId"], childColumns = ["surecId"], onDelete = ForeignKey.CASCADE),

    ]
)
data class Gorev(
    @PrimaryKey(autoGenerate = true)
    val gorevId: Int = 0,
    val surecId: Int,

    val baslik: String,
    val aciklama: String,
    val durum: String,
    val sonTarih: String
)
