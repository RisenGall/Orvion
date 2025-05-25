package com.mirackalyoncu.orvion.roomdb
import androidx.room.*
import com.mirackalyoncu.orvion.model.Kullanici

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single

@Dao
interface KullaniciDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertKullanici(kullanici: Kullanici): Completable

    @Update
    fun updateKullanici(kullanici: Kullanici): Completable

    @Delete
    fun deleteKullanici(kullanici: Kullanici): Completable

    @Query("SELECT * FROM kullanici")
    fun getAllKullanicilar(): Flowable<List<Kullanici>>

    @Query("SELECT * FROM kullanici WHERE kullaniciId = :id")
    fun getKullaniciById(id: Int): Single<Kullanici>

    @Query("SELECT * FROM kullanici WHERE email = :email LIMIT 1")
    fun kullaniciVarMi(email: String): Single<Kullanici>

    @Query("SELECT * FROM kullanici WHERE sifre = :sifre LIMIT 1")
    fun getKullaniciBySifre(sifre: String): Single<Kullanici>

    @Query("SELECT * FROM kullanici WHERE rol != 'admin'")
    fun getKullanicilarHaricAdmin(): Flowable<List<Kullanici>>

    @Query("SELECT * FROM kullanici WHERE email = :email LIMIT 1")
    fun getKullaniciByEmail(email: String): Single<Kullanici>

    @Query("SELECT * FROM kullanici WHERE email = :email AND sifre = :sifre LIMIT 1")
    fun getKullaniciByEmailAndSifre(email: String, sifre: String): Single<Kullanici>


}