package com.mirackalyoncu.orvion.roomdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mirackalyoncu.orvion.model.Surec
import com.mirackalyoncu.orvion.model.SurecKatilim
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable

@Dao
interface SurecKatilimDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun kullaniciyiSureceEkle(katilim: SurecKatilim): Completable

    @Query("SELECT * FROM surec WHERE surecId IN (SELECT surecId FROM surec_katilim WHERE kullaniciId = :kullaniciId)")
    fun getKullaniciSurecleri(kullaniciId: Int): Flowable<List<Surec>>
}
