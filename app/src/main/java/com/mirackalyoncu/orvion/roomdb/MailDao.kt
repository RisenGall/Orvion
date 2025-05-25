package com.mirackalyoncu.orvion.roomdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mirackalyoncu.orvion.model.Mail
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable

@Dao
interface MailDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun mesajGonder(mail: Mail): Completable

    @Query("SELECT * FROM mail WHERE aliciId = :kullaniciId ORDER BY tarih DESC")
    fun gelenKutusu(kullaniciId: Int): Flowable<List<Mail>>

    @Query("SELECT * FROM mail WHERE gonderenId = :kullaniciId ORDER BY tarih DESC")
    fun gidenKutusu(kullaniciId: Int): Flowable<List<Mail>>

    @Update
    fun updateMail(mail: Mail): Completable


}
