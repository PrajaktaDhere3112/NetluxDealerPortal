package com.kdapps.netluxdp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.kdapps.netluxdp.entities.RewardDetailsEntity

@Dao
interface RewardDetailsDao {
    @Insert
    fun insertRewardDetails(rewardDetailsEntity: RewardDetailsEntity)

    @Query("SELECT * FROM tblRewardsDetails1")
    fun getAllRewardDetails(): List<RewardDetailsEntity>

    @Query("SELECT * FROM tblRewardsDetails1 WHERE month =:month")
    fun getMonthRewardDetails(month: String): List<RewardDetailsEntity>


    @Query("SELECT * FROM tblRewardsDetails1 WHERE rewardKey =:rewardKey")
    fun checkRewardKeyPresent(rewardKey: String):List<RewardDetailsEntity>

    @Query("UPDATE tblRewardsDetails1 SET ifScratched = 1 WHERE rewardKey = :rewardKey")
    fun updateCardScratched(rewardKey: String)

    @Query("UPDATE tblRewardsDetails1 SET whenScratched = :time WHERE rewardKey = :rewardKey")
    fun updateTimeCardScratched(rewardKey: String,time:String)

    @Query("DELETE FROM tblRewardsDetails1")
    fun DeleteAllRewardDetails()
}