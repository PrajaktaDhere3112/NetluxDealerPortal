package com.kdapps.netluxdp.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kdapps.netluxdp.entities.ActivationDetailsEntity
import com.kdapps.netluxdp.entities.ButtonClickedEntity
import com.kdapps.netluxdp.entities.OpenDetailsEntity
import com.kdapps.netluxdp.entities.RewardDetailsEntity

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE `tblButtonClickedDetails` (`dealerId` TEXT NOT NULL, `buttonId` TEXT NOT NULL,`timeStamp` TEXT NOT NULL, `res1` TEXT NOT NULL, `res2` TEXT NOT NULL, " +
                "PRIMARY KEY(`timeStamp`))")
    }
}

val MIGRATION_3_4 = object: Migration(3,4){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE `tblRewardsDetails`(`dlrId` TEXT NOT NULL, `month` TEXT NOT NULL, `rewardKey` TEXT NOT NULL, " + "PRIMARY KEY(`rewardKey`))")
    }
}

val MIGRATION_4_5 = object: Migration(4,5){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE `tblRewardsDetails1`(`dlrId` TEXT NOT NULL, `month` TEXT NOT NULL, `rewardKey` TEXT NOT NULL,`ifScratched` INTEGER NOT NULL,`whenScratched` TEXT NOT NULL,`note` TEXT NOT NULL,`res` TEXT NOT NULL, " + "PRIMARY KEY(`rewardKey`))")
    }
}

val MIGRATION_5_6 = object: Migration(5,6){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE `tblOpenDetails`(`dlrId` TEXT NOT NULL, `time` TEXT NOT NULL, " + "PRIMARY KEY(`time`))")
    }
}

@Database(entities = arrayOf(ActivationDetailsEntity::class, ButtonClickedEntity::class, RewardDetailsEntity::class, OpenDetailsEntity::class), version = 6)//5
abstract class NXAVRoomDatabase: RoomDatabase() {
    abstract fun activationDetailsDao(): ActivationDetailsDao
    abstract fun buttonClickedDetailsDao(): ButtonClickedDetailsDao
    abstract fun rewardDetailsDao(): RewardDetailsDao
    abstract fun openDetailsDao(): OpenDetailsDao
}