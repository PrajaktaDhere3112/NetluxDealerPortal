package com.kdapps.netluxdp.database

import android.content.Context
import android.os.AsyncTask
import androidx.room.Room
import com.kdapps.netluxdp.entities.RewardDetailsEntity

class RewardDBAsyncTask(val context: Context, val rewardDetailsEntity: RewardDetailsEntity, val mode:Int)
    :AsyncTask<Void, Void, Boolean>(){
    val db = Room.databaseBuilder(context, NXAVRoomDatabase::class.java, "NXAV-db").addMigrations(
        MIGRATION_1_2, MIGRATION_3_4, MIGRATION_4_5,MIGRATION_5_6).build()
    override fun doInBackground(vararg p0: Void?): Boolean {
        when(mode){
            1 -> {
                db.rewardDetailsDao().insertRewardDetails(rewardDetailsEntity)
                db.close()
                return true
            }
            2 ->{
                db.rewardDetailsDao().updateCardScratched(rewardDetailsEntity.rewardKey)
                db.close()
                return true
            }

            3 ->{
                db.rewardDetailsDao().updateTimeCardScratched(rewardDetailsEntity.rewardKey, rewardDetailsEntity.whenScratched)
            }
        }
        return false
    }
}