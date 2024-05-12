package com.kdapps.netluxdp.database

import android.content.Context
import android.os.AsyncTask
import androidx.room.Room
import com.kdapps.netluxdp.entities.OpenDetailsEntity

class OpenAsuncTask(val context: Context, val openDetailsEntity: OpenDetailsEntity, val mode:Int)
    : AsyncTask<Void, Void, Boolean>() {
    val db = Room.databaseBuilder(context, NXAVRoomDatabase::class.java, "NXAV-db").addMigrations(
        MIGRATION_1_2, MIGRATION_3_4, MIGRATION_4_5,MIGRATION_5_6).build()

    override fun doInBackground(vararg p0: Void?): Boolean {
        when(mode){
            1 ->{
                db.openDetailsDao().insertDetails(openDetailsEntity)
                db.close()
                return true
            }
        }
        return false
    }
}