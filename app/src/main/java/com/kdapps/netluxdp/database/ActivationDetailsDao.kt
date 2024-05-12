package com.kdapps.netluxdp.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.kdapps.netluxdp.entities.ActivationDetailsEntity


@Dao
interface ActivationDetailsDao {

    @Insert
    fun insertActivationDetails(activationDetailsEntity: ActivationDetailsEntity)

    @Delete
    fun deleteActivationDetails(activationDetailsEntity: ActivationDetailsEntity)

    @Query("SELECT * FROM tblActivaitonDetails")
    fun getAllActivationDetails(): List<ActivationDetailsEntity>

    @Query("SELECT * FROM tblActivaitonDetails WHERE date(aDate) >= :fromDate AND  date(aDate) <=:toDate ORDER BY aDate DESC")
    fun getSelectedActivationDetails(fromDate: String, toDate: String): List<ActivationDetailsEntity>

    @Query("SELECT * FROM tblActivaitonDetails WHERE date(expDate) >= :fromDate AND  date(expDate) <=:toDate ORDER BY expDate DESC")
    fun getExpiredActivationDetails(fromDate: String, toDate: String): List<ActivationDetailsEntity>

    @Query("SELECT * FROM tblActivaitonDetails WHERE cupkey = :key")
    fun checkActivationEntry( key: String): ActivationDetailsEntity

    @Query("SELECT COUNT(*) FROM tblActivaitonDetails WHERE date(aDate)>= :fromDate AND CAST (cuLotNo AS INTEGER)<1400000")
    fun getCurrentMonthActivationCount(fromDate:String):Int

    @Query("SELECT COUNT(*) FROM tblActivaitonDetails WHERE strftime('%Y-%m', aDate) = :month AND CAST (cuLotNo AS INTEGER)<1400000 ")
    fun getMonthActivationCount(month:String):Int

    ///get activation count from feb 2022
    @Query("SELECT COUNT(*) FROM tblActivaitonDetails WHERE date(aDate) >=:date AND CAST (cuLotNo AS INTEGER)<1400000 ")
    fun getActivationCountFromFeb(date:String):Int

    @Query("SELECT COUNT(*) FROM tblActivaitonDetails")
    fun getTotalCount(): Int

    @Query("SELECT aDate from tblActivaitonDetails ORDER BY aDate DESC LIMIT 1")
    fun getLatestActivationDate():String

    @Query("SELECT cuName from tblActivaitonDetails")
    fun getAllCustomerNames():List<String>

    @Query("SELECT * FROM tblActivaitonDetails WHERE cuName LIKE :customerName")
    fun getCustomerDetailsFromName(customerName:String): List<ActivationDetailsEntity>

    @Query("SELECT *FROM tblActivaitonDetails WHERE cuNumber = :customerNumber")
    fun getCustomerDetailsFromNumber(customerNumber:String):List<ActivationDetailsEntity>

    @Query("DELETE FROM tblActivaitonDetails")
    fun DeleteAllActivationDetails()
    //9575765006
    //1123771
}