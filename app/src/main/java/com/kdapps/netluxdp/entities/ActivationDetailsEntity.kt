package com.kdapps.netluxdp.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "tblActivaitonDetails")
data class ActivationDetailsEntity(
    @ColumnInfo(name = "dlrId") val dlrId: String,
    @ColumnInfo(name ="cuName") val cuName: String,
    @ColumnInfo(name = "cuNumber") val cuNumber: String,
    @ColumnInfo(name = "cuLotNo") val cuLotNo: String,
    @PrimaryKey val cupkey: String,
    @ColumnInfo(name = "cuiKey") val cuiKey:String,
    @ColumnInfo(name = "cuaKey") val cuaKey:String,
    @ColumnInfo(name = "expDate") val expDate:String,
    @ColumnInfo(name = "aDate") val aDate:String,
    @ColumnInfo(name = "cuEmailId") val cuEmailId:String,
    @ColumnInfo(name = "cuState") val cuState:String,
    @ColumnInfo(name = "cuCity") val cuCity:String,
    @ColumnInfo(name = "cuPincode") val cuPincode:String,
    @ColumnInfo(name = "engId") val engId:String,
    @ColumnInfo(name = "actRes1") val actRes1:String,
    @ColumnInfo(name = "actRes2") val actRes2:String,
    @ColumnInfo(name = "actRes3") val actRes3:String,
    @ColumnInfo(name = "actRes4") val actRes4:String,
    @ColumnInfo(name = "actRes5") val actRes5:String

)
