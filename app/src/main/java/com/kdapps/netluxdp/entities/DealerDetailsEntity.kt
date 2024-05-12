package com.kdapps.netluxdp.entities

import androidx.room.Entity


data class DealerDetailsEntity(
    val dlrId: String,
    val dlrCode:String,
    val dlrMob1:String,
    val dlrMob2:String,
    val dlrMob3:String,
    val dlrName:String,
    val dlrShopName:String,
    val dlrShopAddress1:String,
    val dlrShopAddress2:String,
    val dlrShopAddress3:String,
    val dlrShopPincode:String,
    val dlrState: String,
    val dlrDOB: String,
    val engId1: String,
    val engId2: String,
    val engId3: String,
    val engId4: String,
    val engName1: String,
    val engName2: String,
    val engName3: String,
    val engName4: String,
    val engNum1: String,
    val engNum2: String,
    val engNum3: String,
    val engNum4: String,
    val dlrRes1:String,
    val dlrRes2:String,
    val dlrRes3:String,
    val dlrRes4:String,
    val dlrRes5:String
)
