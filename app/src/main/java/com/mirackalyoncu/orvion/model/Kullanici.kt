package com.mirackalyoncu.orvion.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kullanici")
data class Kullanici(
    @PrimaryKey(autoGenerate = true)
    val kullaniciId: Int = 0,

    val ad: String,

    val soyad: String,

    val email: String,

    val sifre: String,

    val rol: String = "kullanici"

)