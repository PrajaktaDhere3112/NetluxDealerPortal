package com.kdapps.netluxdp.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tblRewardsDetails1")
data class RewardDetailsEntity (
    @ColumnInfo(name = "dlrId") val dlrId: String,
    @ColumnInfo(name = "month") val month: String,
    @PrimaryKey val rewardKey: String,
    @ColumnInfo(name = "ifScratched") val ifScratched: Boolean,
    @ColumnInfo(name = "whenScratched") val whenScratched:String,
    @ColumnInfo(name = "note") val note: String = "No note",
    @ColumnInfo(name = "res") val res:String = "Reserved"
    )