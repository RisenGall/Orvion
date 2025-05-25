package com.mirackalyoncu.orvion.roomdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mirackalyoncu.orvion.model.Gorev
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable

@Dao
interface GorevDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGorev(gorev: Gorev): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGorevList(gorevList: List<Gorev>): Completable


    @Update
    fun updateGorev(gorev: Gorev): Completable

    @Query("SELECT * FROM gorev WHERE surecId = :surecId")
    fun getGorevlerBySurec(surecId: Int): Flowable<List<Gorev>>


    @Query("""
        SELECT g.* FROM gorev g
        INNER JOIN surec_katilim sk ON g.surecId = sk.surecId
        WHERE sk.kullaniciId = :kullaniciId
    """)
    fun getGorevlerByKullanici(kullaniciId: Int): Flowable<List<Gorev>>

    @Query("""
    SELECT g.* FROM gorev g
    WHERE g.surecId IN (
        SELECT s.surecId FROM surec s
        LEFT JOIN surec_katilim sk ON s.surecId = sk.surecId
        WHERE s.olusturanKullaniciId = :kullaniciId OR sk.kullaniciId = :kullaniciId
    )
""")
    fun getGorevlerBySureceDahilKullanici(kullaniciId: Int): Flowable<List<Gorev>>

}
