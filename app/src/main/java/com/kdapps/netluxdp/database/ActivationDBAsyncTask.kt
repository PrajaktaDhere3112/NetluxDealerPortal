package com.kdapps.netluxdp.database

import android.content.Context
import android.os.AsyncTask
import androidx.loader.content.AsyncTaskLoader
import androidx.room.Room
import com.kdapps.netluxdp.entities.ActivationDetailsEntity

class ActivationDBAsyncTask(val context: Context, val activationDetailsEntity: ActivationDetailsEntity, val mode:Int)
    :AsyncTask<Void, Void,Boolean>(){

    val db = Room.databaseBuilder(context, NXAVRoomDatabase::class.java, "NXAV-db").addMigrations(
            MIGRATION_1_2, MIGRATION_3_4, MIGRATION_4_5,MIGRATION_5_6).build()
    override fun doInBackground(vararg p0: Void?): Boolean {
        when(mode){
            1 -> {
                db.activationDetailsDao().insertActivationDetails(activationDetailsEntity)
                db.close()
                return true
            }

            2 ->{
                db.activationDetailsDao().deleteActivationDetails(activationDetailsEntity)
                db.close()
                return true
            }

            3 ->{
                val activations: ActivationDetailsEntity =db.activationDetailsDao().checkActivationEntry(activationDetailsEntity.cupkey)
                db.close()
                return activations != null
            }
        }
        return false
    }

}