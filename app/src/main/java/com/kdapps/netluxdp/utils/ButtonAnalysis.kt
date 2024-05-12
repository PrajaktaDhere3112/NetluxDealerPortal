package com.kdapps.netluxdp.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.kdapps.netluxdp.database.ButtonClickedDBAsyncTask
import com.kdapps.netluxdp.entities.ButtonClickedEntity
import java.text.SimpleDateFormat
import java.util.*

open class ButtonAnalysis(val context: Context) {
    val sharedPreferences = context.getSharedPreferences("dealer_details", MODE_PRIVATE)
    val dealerId = sharedPreferences.getString("dlrId","")

    fun addButtonDetails(buttonId:String, res1:String, res2:String): Boolean{
        val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS")
        val timeStamp = sdf.format(Date())
        if(dealerId != null){
            val buttonEntity = ButtonClickedEntity(dealerId, buttonId, timeStamp, res1,res2)
            val add = ButtonClickedDBAsyncTask(context,buttonEntity, 1 ).execute()
            val check = add.get()
            return check
        }
        else{
            return false
        }
    }
}