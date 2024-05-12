package com.kdapps.netluxdp.activities

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.ScrollView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import com.kdapps.netluxdp.R
import com.kdapps.netluxdp.utils.VolleySingleton
import com.kdapps.netluxdp.utils.showProgressDialog
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.fragment_my_activations.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class RegisterActivity : AppCompatActivity() {


    lateinit var viewTo:ScrollView
    lateinit var sharedPreferences: SharedPreferences
    lateinit var dialog: Dialog
    var Str :String = "http://byteseq.com/temp/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.offlineregistration)

        sharedPreferences = this.getSharedPreferences("dealer_details", MODE_PRIVATE)
        viewTo = register_scroll_view
        dialog = Dialog(this)

        img_calendar.setOnClickListener(){
            openCalender()
        }
        et_dlr_DOB.setOnClickListener(){
            openCalender()
        }

        btn_register.setOnClickListener(){
            val checkDetails = validateDetails()
            if(checkDetails){
                val dlrName = et_dlr_name.text.toString()
                val shopName = et_shop_name.text.toString()
                val DOB = et_dlr_DOB.text.toString()
                val pincode = et_shop_pincode.text.toString()
                val mobNo = et_dlr_mob_no.text.toString()
                showProgressDialog().showProgressDialog(dialog,"Please wait for while...")
                addDealerDetails(dlrName, shopName, DOB, pincode, mobNo)
            }

        }
    }

    private fun addDealerDetails(dlrName:String, shopName:String, DOB:String, pincode:String, mobNo:String){
        val url = Str + "newdlrreg.php?OWNNAME=$dlrName&DLRMOB1=$mobNo&SHOPNAME=$shopName&PINCODE=$pincode&DOB=$DOB%2000:00:00"
        val stringRequest = object : StringRequest(Request.Method.POST,url, Response.Listener{
            try {
                val name = it
                Log.e("Register_check", name)
                sharedPreferences.edit().putBoolean("registrationCompletedCheck", false)
                showProgressDialog().hideProgressDialog(dialog)
                val dialog = AlertDialog.Builder(this)
                    .setTitle("New User")
                    .setMessage("Your registration will be completed within 2 hours.")
                    .setPositiveButton("OK") { text, listener ->
                        this.finishAffinity()
                    }
                    .create()
                    .show()
            }catch (e: JSONException){
                e.printStackTrace()
                Toast.makeText(this@RegisterActivity, "something went wrong", Toast.LENGTH_SHORT).show()
                showProgressDialog().hideProgressDialog(dialog)
            }

        },Response.ErrorListener{
            showProgressDialog().hideProgressDialog(dialog)
            showSnackBar("Please try again")
            //Toast.makeText(this@RegisterActivity, it.toString() , Toast.LENGTH_LONG).show()

        }){
            override fun getHeaders(): MutableMap<String, String> {
                return super.getHeaders()
            }

        }
        VolleySingleton(this).addToRequestQueue(stringRequest)
    }

    fun openCalender(){
        val cal = Calendar.getInstance()
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH)
        val d = cal.get(Calendar.DAY_OF_MONTH)
        var toDate:String
        val datepickerdialog = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

            // Display Selected date in textbox
            toDate = getProperDate(year, monthOfYear, dayOfMonth)

            et_dlr_DOB.setText(toDate)
        }, y, m, d)
        datepickerdialog.datePicker.maxDate = cal.timeInMillis
        Log.e("mindate", cal.time.toString())

        datepickerdialog.show()
    }

    private fun validateDetails():Boolean{
        return when {
            TextUtils.isEmpty(et_dlr_name.text.toString().trim() { it <= ' ' }) -> {
                showSnackBar("Please Enter Name")
                false
            }

            TextUtils.isEmpty(et_dlr_mob_no.text.toString().trim() { it <= ' ' }) -> {
                showSnackBar("Please Enter Mobile number")
                false
            }

            TextUtils.isEmpty(et_shop_name.text.toString().trim() { it <= ' ' }) -> {
                showSnackBar("Please Enter Shop Name")
                false
            }

            TextUtils.isEmpty(et_dlr_DOB.text.toString().trim() { it <= ' ' }) -> {
                showSnackBar("Please Enter DOB")
                false
            }
            TextUtils.isEmpty(et_shop_pincode.text.toString().trim() { it <= ' ' }) -> {
                showSnackBar("Please Enter Shop Pincode")
                false
            }

            else -> {
                //showErrorSnackBar("You are registered user!!!",false)
                true
            }
        }
    }
    private fun getProperDate(year:Int, monthOfYear: Int, dayOfMonth:Int):String{
        var month:String
        var day:String = dayOfMonth.toString()
        var date:String
        if(monthOfYear < 9){
            month = ("0" +(monthOfYear + 1))
            Log.e("monthLAtes", month)
            if(dayOfMonth < 10){
                day = ("0" +dayOfMonth)
                Log.e("dateLaates", day)
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
                Log.e("dateLaates", day)
                date ="" + year + "-" + month + "-" + day
            }
            else{
                date ="" + year + "-" + month + "-" + dayOfMonth
            }
            return date
        }
    }

    private fun showSnackBar(msg: String){
        Snackbar.make(
            viewTo,
            msg,
            Snackbar.LENGTH_SHORT
        )
            .setBackgroundTint(getResources().getColor(R.color.warning_color))
            .show()

    }


}