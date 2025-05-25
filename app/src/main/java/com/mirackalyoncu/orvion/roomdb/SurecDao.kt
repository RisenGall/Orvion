package com.mirackalyoncu.orvion.roomdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.mirackalyoncu.orvion.model.Surec
import com.mirackalyoncu.orvion.model.SurecKatilim
import com.mirackalyoncu.orvion.model.SurecWithProgress
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single

@Dao
interface SurecDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSurec(surec: Surec): Single<Long>

    @Update
    fun updateSurec(surec: Surec): Completable

    @Query("SELECT * FROM surec")
    fun getAllSurecler(): Flowable<List<Surec>>

    @Query("SELECT * FROM surec WHERE surecId = :id")
    fun getSurecById(id: Int): Single<Surec>





    @Transaction
    @Query("""
        SELECT s.surecId, s.surecAdi, 
               COUNT(g.gorevId) AS toplamGorev, 
               SUM(CASE WHEN g.durum = 'Tamamlandı' THEN 1 ELSE 0 END) AS tamamlananGorev
        FROM surec s
        LEFT JOIN gorev g ON s.surecId = g.surecId
        WHERE s.olusturanKullaniciId = :kullaniciId
        GROUP BY s.surecId
    """)
    fun getSurecWithProgressByAdminId(kullaniciId: Int): Flowable<List<SurecWithProgress>>


    @Transaction
    @Query("""
        SELECT s.surecId, s.surecAdi, 
               COUNT(g.gorevId) AS toplamGorev,
               SUM(CASE WHEN g.durum = 'Tamamlandı' THEN 1 ELSE 0 END) AS tamamlananGorev
        FROM surec s
        LEFT JOIN gorev g ON s.surecId = g.surecId
        WHERE s.surecId IN (
            SELECT surecId FROM surec_katilim WHERE kullaniciId = :kullaniciId
        )
        GROUP BY s.surecId
    """)
    fun getSurecWithProgressByUserId(kullaniciId: Int): Flowable<List<SurecWithProgress>>


    @Query("SELECT * FROM surec WHERE girisKodu = :girisKodu LIMIT 1")
    fun getSurecByGirisKodu(girisKodu: Int): Single<Surec>

    @Query("SELECT COUNT(*) FROM surec WHERE girisKodu = :kod")
    fun girisKoduVarMi(kod: Int): Single<Int>

    @Insert
    fun insertSurecKatilim(surecKatilim: List<SurecKatilim>): Completable
}
