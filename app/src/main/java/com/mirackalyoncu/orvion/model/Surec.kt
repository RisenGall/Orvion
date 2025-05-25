package com.mirackalyoncu.orvion.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "surec",
    foreignKeys = [
        ForeignKey(
            entity = Kullanici::class,
            parentColumns = ["kullaniciId"],
            childColumns = ["olusturanKullaniciId"],
            onDelete = ForeignKey.SET_NULL
        ),

    ]
)
data class Surec(
    @PrimaryKey(autoGenerate = true)
    val surecId: Int = 0,
    val surecAdi: String,
    val bitisTarihi: String,
    val olusturanKullaniciId: Int?,
    val olusturmaTarihi: String,
    val girisKodu: Int

)