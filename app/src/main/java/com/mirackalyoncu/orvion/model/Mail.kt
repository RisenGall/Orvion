package com.mirackalyoncu.orvion.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "mail",
    foreignKeys = [
        ForeignKey(entity = Kullanici::class, parentColumns = ["kullaniciId"], childColumns = ["gonderenId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Kullanici::class, parentColumns = ["kullaniciId"], childColumns = ["aliciId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class Mail(
    @PrimaryKey(autoGenerate = true) val mailId: Int = 0,
    val gonderenId: Int,
    val aliciId: Int,
    val konu: String,
    val icerik: String,
    val tarih: String,
    var okundu: Boolean = false
)
