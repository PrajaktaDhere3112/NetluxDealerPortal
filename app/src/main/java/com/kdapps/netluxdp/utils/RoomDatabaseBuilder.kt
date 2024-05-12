package com.kdapps.netluxdp.utils

import android.content.Context
import androidx.room.Room
import com.kdapps.netluxdp.database.*

class RoomDatabaseBuilder(val context: Context){


    fun getRoomDatabase(): NXAVRoomDatabase {
        val db = Room.databaseBuilder(context, NXAVRoomDatabase::class.java, "NXAV-db").addMigrations(
            MIGRATION_1_2, MIGRATION_3_4, MIGRATION_4_5,MIGRATION_5_6).build()
        return db
    }

}