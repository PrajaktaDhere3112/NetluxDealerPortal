package com.kdapps.netluxdp.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "tblOpenDetails")
data class OpenDetailsEntity(
    @ColumnInfo(name = "dlrId") val dlrId: String,
    @PrimaryKey val time: String
)
