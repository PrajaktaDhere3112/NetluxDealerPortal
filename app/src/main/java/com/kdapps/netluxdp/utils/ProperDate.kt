package com.kdapps.netluxdp.utils

import android.util.Log
import java.text.SimpleDateFormat

class ProperDate() {
    fun getProperDate(year:Int, monthOfYear: Int, dayOfMonth:Int):String{
        var month:String
        var day:String = dayOfMonth.toString()
        var date:String
        if(monthOfYear < 9){
            month = ("0" +(monthOfYear + 1))
            //Log.e("monthLAtes", month)
            if(dayOfMonth < 10){
                day = ("0" +dayOfMonth)
                //Log.e("dateLates", day)
                date ="" + year + "-" + month + "-" + day
            }
            else{
                date ="" + year + "-" + month + "-" + dayOfMonth
            }
            return date
        }
        else{
            month =( monthOfYear + 1).toString()
            if(dayOfMonth < 10){
                day = ("0" +dayOfMonth)
                //Log.e("dateLates", day)
                date ="" + year + "-" + month + "-" + day
            }
            else{
                date ="" + year + "-" + month + "-" + dayOfMonth
            }
            return date
        }
    }

    fun getProperTime(time:String):String{
        val format = SimpleDateFormat("HH:mm")
        val date = format.parse(time)
        val sdf = SimpleDateFormat("h:mma")
        val properTime = sdf.format(date).toUpperCase()
        return  properTime


    }

    fun getNextMonth(month : String):String{
        val reqMonth:String
        val m = month.substring(5)
        Log.e("month", "month is $m")
        val y = month.substring(0,4)
        Log.e("month", "year is $y")
        if(m == "12"){
            val newY = (y.toInt() + 1).toString()
            val newM = "01"
            reqMonth = newY +"-"+ newM
        }
        else{
            val newM = (m.toInt() + 1).toString()
            if(newM.toInt() <=9){
                reqMonth = y+"-0"+newM
            }
            else{
                reqMonth = y+"-"+newM
            }
        }
        return reqMonth
    }

    fun getPreviousMonth(month : String):String{
        val reqMonth:String
        val m = month.substring(5)
        Log.e("month", "month is $m")
        val y = month.substring(0,4)
        if(m == "01"){
            val newY = (y.toInt() - 1).toString()
            val newM = "12"
            reqMonth = newY +"-"+ newM
        }
        else{
            val newM = (m.toInt() - 1).toString()
            if(newM.toInt()<=9){
                reqMonth = y+"-0"+newM
            }
            else{
                reqMonth = y+"-"+newM
            }

        }
        return reqMonth
    }

    fun getMonthName(month: String):String{
        val m = month.substring(5)
        val y = month.substring(0,4)
        when(m){
            "01" ->{
                return "Jan $y"
            }
            "02" ->{
                return "Feb $y"
            }
            "03" ->{
                return "Mar $y"
            }
            "04" ->{
                return "Apr $y"

            }
            "05" ->{
                return "May $y"

            }
            "06" ->{
                return "Jun $y"
            }
            "07" ->{
                return "Jul $y"

            }
            "08" ->{
                return "Aug $y"

            }
            "09" ->{
                return "Sep $y"

            }
            "10" ->{
                return "Oct $y"

            }
            "11" ->{
                return "Nov $y"

            }
            "12" ->{
                return "Dec $y"

            }
            else ->{
                return y
            }
        }
    }
}