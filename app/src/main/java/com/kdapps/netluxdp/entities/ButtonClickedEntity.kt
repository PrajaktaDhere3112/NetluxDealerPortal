package com.kdapps.netluxdp.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "tblButtonClickedDetails")
data class ButtonClickedEntity(
    @ColumnInfo(name = "dealerId") val dealerId: String,
    @ColumnInfo(name = "buttonId") val buttonId: String,
    @PrimaryKey val timeStamp: String,
    @ColumnInfo(name = "res1") val res1:String,
    @ColumnInfo(name = "res2") val res2:String
)