package com.kdapps.netluxdp.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.kdapps.netluxdp.entities.ButtonClickedEntity


@Dao
interface ButtonClickedDetailsDao {

    @Insert
    fun insertButtonClickedDetails(buttonClickedEntity: ButtonClickedEntity)

    @Delete
    fun deleteButtonClickedDetails(buttonClickedEntity: ButtonClickedEntity)

    @Query("SELECT * FROM tblButtonClickedDetails")
    fun getAllButtonDetails(): List<ButtonClickedEntity>

    @Query("DELETE FROM tblButtonClickedDetails")
    fun clearAllButtonDetails()
}