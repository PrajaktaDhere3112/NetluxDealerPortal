package com.kdapps.netluxdp.database

import androidx.room.Dao
import androidx.room.Insert
import com.kdapps.netluxdp.entities.OpenDetailsEntity


@Dao
interface OpenDetailsDao {

    @Insert
    fun insertDetails(openDetailsEntity: OpenDetailsEntity)
}