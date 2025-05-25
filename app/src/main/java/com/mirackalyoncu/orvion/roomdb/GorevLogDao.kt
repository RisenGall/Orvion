package com.mirackalyoncu.orvion.roomdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.mirackalyoncu.orvion.model.GorevLog
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable

@Dao
interface GorevLogDao {

    @Insert
    fun insertLog(log: GorevLog): Completable

    @Query("SELECT * FROM gorev_log WHERE gorevId = :gorevId")
    fun getLogsByGorev(gorevId: Int): Flowable<List<GorevLog>>
}