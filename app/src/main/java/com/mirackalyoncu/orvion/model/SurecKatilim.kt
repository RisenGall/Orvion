package com.mirackalyoncu.orvion.model

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "surec_katilim",
    primaryKeys = ["surecId", "kullaniciId"],
    foreignKeys = [
        ForeignKey(entity = Surec::class, parentColumns = ["surecId"], childColumns = ["surecId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Kullanici::class, parentColumns = ["kullaniciId"], childColumns = ["kullaniciId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class SurecKatilim(
    val surecId: Int,
    val kullaniciId: Int
)
