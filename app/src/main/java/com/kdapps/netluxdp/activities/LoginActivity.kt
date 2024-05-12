package com.kdapps.netluxdp.activities

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ScrollView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.common.api.internal.LifecycleCallback.getFragment
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.kdapps.netluxdp.R
import com.kdapps.netluxdp.entities.DealerDetailsEntity
import com.kdapps.netluxdp.fragments.OfflineRegistration
import com.kdapps.netluxdp.fragments.ProfileFragment
import com.kdapps.netluxdp.utils.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.btn_register
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_show_activation_details_activtiy.*
import kotlinx.android.synthetic.main.progress_bar_layout.*
import kotlinx.coroutines.*

class LoginActivity : AppCompatActivity() {

    private lateinit var mProgressDialog: Dialog
    lateinit var sharedPreferences: SharedPreferences
    lateinit var viewTo:ScrollView
    lateinit var realOTP:String
    var registerCheck:Boolean = false
    var checkContinueClicks:Boolean = false
    var Str :String = "http://byteseq.com/temp/"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        viewTo = findViewById(R.id.login_scroll_view)


        mProgressDialog = Dialog(this)
        sharedPreferences = this.getSharedPreferences("dealer_details", MODE_PRIVATE)


        fun getFragment(fragment: Fragment, fragmentName:String, view: View){
            checkContinueClicks = true
            mProgressDialog = Dialog(this)


            Handler().postDelayed({
                showProgressDialog().hideProgressDialog(mProgressDialog)
                checkContinueClicks = false
                supportFragmentManager.beginTransaction()
                    .replace(R.id.login_scroll_view, fragment)
                    .addToBackStack("$fragment")
                    .setPrimaryNavigationFragment(fragment)
                    .commit()
            },1000)

        }
        btn_register.setOnClickListener(){

           setContentView(R.layout.fragment_marketing_helpline);
            //val intent = Intent(this, RegisterActivity::class.java)
            //startActivity(intent)



        }


        btn_send_otp.setOnClickListener(){
            val checkNumber = onlyValidateNumber()
            val checkInternet = ConnectionManager().checkConnectivity(this)
            val btnText = btn_send_otp.text.toString()
            if(checkNumber && btnText == "GET OTP" && checkInternet){
                it.isEnabled = false
                //timer
                val waitCountDown = object : CountDownTimer(30000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        btn_send_otp.setText("OTP SENT (" + millisUntilFinished / 1000 +"s)")
                    }

                    override fun onFinish() {
                        it.isEnabled = true
                        btn_send_otp.setText("CALL FOR OTP")
                        btn_re_enter_no.visibility = View.VISIBLE
                    }
                }.start()



                val mobNo = et_mobile_number.text.toString()
                GlobalScope.launch(Dispatchers.IO) {
                    val url = Str + "nxavdpotp.php?MOB=$mobNo"
                    val jsonObjectRequest = object: JsonObjectRequest(
                        Request.Method.GET,
                        url,
                        null,
                        Response.Listener {
                                          val result = it.getString("RESULT")
                            if(result == "SMSSENT"){
                                showGreenSnackBar("OTP sent successfully!!!")
                            }
                            else if(result == "ACCOUNTNOTREGISTERED"){
                                val dialog = AlertDialog.Builder(this@LoginActivity)
                                    .setTitle("New User")
                                    .setMessage("Your mobile number is not Registered")
                                    .setPositiveButton("Register") { text, listener ->
                                        val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                                        startActivity(intent)

                                    }
                                    .setNegativeButton("Contact NXAV") { text, listener ->
                                        val number =getString( R.string.activation_helpline_1).trim()
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number))
                                        startActivity(intent)
                                    }
                                    .create()
                                    .show()
                                waitCountDown.cancel()
                                btn_send_otp.isEnabled = true
                                btn_send_otp.setText("GET OTP")

                            }
                            else{
                                val dialog = AlertDialog.Builder(this@LoginActivity)
                                    .setTitle("Dear User")
                                    .setMessage("PLease try after some time")
                                    .setPositiveButton("OK") { text, listener ->

                                    }

                                    .create()
                                    .show()
                            }
                        },
                        Response.ErrorListener {

                            println("Error is $it")
                            val dialog = AlertDialog.Builder(this@LoginActivity)
                                .setTitle("Dear User")
                                .setMessage("PLease try after some time")
                                .setPositiveButton("OK") { text, listener ->

                                }

                                .create()
                                .show()
                        }
                    ){}
                    VolleySingleton(this@LoginActivity).addToRequestQueue(jsonObjectRequest)
                }

            }
            else if( !checkInternet){
                showSnackBar("Check your internet connectivity.")
            }
            else if(btnText == "CALL FOR OTP"){
                it.isEnabled = true
                val otp_number = getString( R.string.technical_helpline_1).trim()
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + otp_number))
                startActivity(intent)
            }



        }

        btn_re_enter_no.setOnClickListener(){
            et_mobile_number.setText("")
            btn_send_otp.isEnabled = true
            btn_send_otp.setText("GET OTP")
            btn_re_enter_no.visibility = View.GONE
        }

        btn_login.setOnClickListener(){

            val mobNo = et_mobile_number.text.toString()
            validateNumber()
            val check = validateNumber()
            checkRegistrationCompletion(mobNo)
            if(check){
                if(ConnectionManager().checkConnectivity(this)) {
                    showProgressDialog().showProgressDialog(mProgressDialog,"Checking Mobile Number")
                    val url = Str + "getdlrid.php?dlrmob=$mobNo"

                    val jsonArrayRequest = object : JsonArrayRequest(
                        Request.Method.POST,
                        url,
                        null,
                        Response.Listener {
                            if(it.length() == 0){
                                if(registerCheck){
                                    val dialog = AlertDialog.Builder(this)
                                        .setTitle("New User")
                                        .setMessage("Your registration is in process, will get completed soon.")
                                        .setPositiveButton("OK") { text, listener ->
                                            this.finishAffinity()

                                        }
                                        .create()
                                        .show()
                                }
                                else{
                                    val dialog = AlertDialog.Builder(this)
                                        .setTitle("New User")
                                        .setMessage("Your mobile number is not Registered")
                                        .setPositiveButton("Register") { text, listener ->
                                            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                                            startActivity(intent)

                                        }
                                        .setNegativeButton("Contact NXAV") { text, listener ->
                                            val number =getString( R.string.activation_helpline_1).trim()
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number))
                                            startActivity(intent)
                                        }
                                        .create()
                                        .show()
                                }
                                showProgressDialog().hideProgressDialog(mProgressDialog)


                            }else{

                                val jsonObject = it.getJSONObject(0)
                                val dlrId = jsonObject.getString("DLRID")
                                Log.e("dlrId", dlrId)
                                sharedPreferences.edit().putString("dlrId", dlrId).apply()
                                showProgressDialog().hideProgressDialog(mProgressDialog)
                                checkCredentials(dlrId)


                            }
                        },
                        Response.ErrorListener {
                            println("Error is $it")
                            showProgressDialog().hideProgressDialog(mProgressDialog)
                            //Showing error is some error ocurred while fetching data
                            Toast.makeText(this@LoginActivity, " Something went wrong", Toast.LENGTH_LONG).show()
                        }
                    ){
                        override fun getHeaders(): MutableMap<String, String> {
                            return super.getHeaders()
                        }
                    }
                    VolleySingleton(this).addToRequestQueue(jsonArrayRequest)

                }
                else{
                    showSnackBar("Check your internet connection")
                }
            }



        }
    }

    private fun checkRegistrationCompletion(mobNo:String){
        val url = Str + "checknewdlrreg.php?DLRMOB1=$mobNo"
        val queue = Volley.newRequestQueue(this)
        val jsonArrayRequest = object :JsonArrayRequest(
            Request.Method.POST,
            url,
            null,
            Response.Listener {
                              for(i in 0 until it.length()){
                                  val jsonObject = it.getJSONObject(i)
                                  val check = jsonObject.getString("PROCESS")
                                  registerCheck = check != "PENDING"
                              }
            },
            Response.ErrorListener {
                println("Error is $it")
            }
        ){
            override fun getHeaders(): MutableMap<String, String> {
                return super.getHeaders()
            }
        }
        queue.add(jsonArrayRequest)
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

    private fun showGreenSnackBar(msg: String){
        Snackbar.make(
            viewTo,
            msg,
            Snackbar.LENGTH_SHORT
        )
            .setBackgroundTint(getResources().getColor(R.color.success_color))
            .show()

    }


    private fun checkCredentials(dlrId: String){
        showProgressDialog().showProgressDialog(mProgressDialog,"Logging in...")
        val url = Str +"getotp.php?dlrid=$dlrId"
        val otpJsonArray = object :JsonArrayRequest(
            Request.Method.POST,
            url,
            null,
            Response.Listener {
                if(it.length()== 0){
                    val dialog = AlertDialog.Builder(this)
                        .setTitle("Sign In Failed")
                        .setMessage("Dear user, Please contact 7843000430 regarding registration.")
                        .setPositiveButton("OK") { text, listener ->
                            this.finishAffinity()

                        }
                        .create()
                        .show()
                }else{
                    val jsonObject = it.getJSONObject(0)
                    realOTP = jsonObject.getString("OTP")
                    val OTP = et_OTP.text.toString()
                    if(realOTP == OTP){
                        //getDealerDetails(dlrId)
                        sharedPreferences.edit().putBoolean("loginCheck", true).apply()
                        showProgressDialog().hideProgressDialog(mProgressDialog)
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        this.finish()
                    }
                    else{
                        showProgressDialog().hideProgressDialog(mProgressDialog)
                        showSnackBar("Incorrect OTP. Please enter correct OTP.")
                    }
                }

            },
            Response.ErrorListener {
                println("Error is $it")
            }
        ){

        }
        VolleySingleton(this).addToRequestQueue(otpJsonArray)

    }

    private fun getDealerDetails(dlrId: String){
        //get dealer details from server and save it in sharedPreferences
        showProgressDialog().showProgressDialog(mProgressDialog,"Getting Dealer Details")
        var detailsCheck: Boolean = false
        val url =Str +"dlrdetails.php?dlrid=$dlrId"
        //val queue = Volley.newRequestQueue(this)
        val jsonArrayRequest = object :JsonArrayRequest(
            Request.Method.POST,
            url,
            null,
            Response.Listener {
                if(it.length() == 0){
                    showSnackBar("No dealer details Found")
                    showProgressDialog().hideProgressDialog(mProgressDialog)
                }else{
                    Log.e("data", "Fectching...")

                    Log.e("Fetching", "Getting detials")
                    val jsonObject = it.getJSONObject(0)
                    val dealerId =jsonObject.getString("DLRID")
                    val dlrName = Encryption(this).decryptCBC(jsonObject.getString("DLRNAME"))
                    val dlrShopName = Encryption(this).decryptCBC(jsonObject.getString("DLRSHOPNAME"))
                    val mobNo1 = Encryption(this).decryptCBC(jsonObject.getString("DLRMOB1"))
                    val mobNo2 = Encryption(this).decryptCBC(jsonObject.getString("DLRMOB2"))
                    val mobNo3 = Encryption(this).decryptCBC(jsonObject.getString("DLRMOB3"))
                    val dlrCode = Encryption(this).decryptCBC(jsonObject.getString("DLRCODE"))
                    val dlrDOB = Encryption(this).decryptCBC(jsonObject.getString("DLRDOB"))
                    val dlrShopAdd1 = Encryption(this).decryptCBC(jsonObject.getString("DLRSHOPADDRESS1"))
                    val dlrShopAdd2 = Encryption(this).decryptCBC(jsonObject.getString("DLRSHOPADDRESS2"))
                    val dlrShopAdd3 = Encryption(this).decryptCBC(jsonObject.getString("DLRSHOPADDRESS3"))
                    val dlrState = Encryption(this).decryptCBC(jsonObject.getString("DLRSTATE"))
                    val dlrShopPincode= Encryption(this).decryptCBC(jsonObject.getString("DLRSHOPPINCODE"))
                    val engId1 = Encryption(this).decryptCBC(jsonObject.getString("ENGID1"))
                    val engId2 = Encryption(this).decryptCBC(jsonObject.getString("ENGID2"))
                    val engId3 = Encryption(this).decryptCBC(jsonObject.getString("ENGID3"))
                    val engId4 = Encryption(this).decryptCBC(jsonObject.getString("ENGID4"))
                    val engName1 = Encryption(this).decryptCBC(jsonObject.getString("ENGNAME1"))
                    val engName2 = Encryption(this).decryptCBC(jsonObject.getString("ENGNAME2"))
                    val engName3 = Encryption(this).decryptCBC(jsonObject.getString("ENGNAME3"))
                    val engName4 = Encryption(this).decryptCBC(jsonObject.getString("ENGNAME4"))
                    val engNumber1 = Encryption(this).decryptCBC(jsonObject.getString("ENGNUMB1"))
                    val engNumber2 = Encryption(this).decryptCBC(jsonObject.getString("ENGNUMB2"))
                    val engNumber3 = Encryption(this).decryptCBC(jsonObject.getString("ENGNUMB3"))
                    val engNumber4 = Encryption(this).decryptCBC(jsonObject.getString("ENGNUMB4"))
                    val dlrRes1 = Encryption(this).decryptCBC(jsonObject.getString("DLRRES1"))
                    val dlrRes2= Encryption(this).decryptCBC(jsonObject.getString("DLRRES2"))
                    val dlrRes3= Encryption(this).decryptCBC(jsonObject.getString("DLRRES3"))
                    val dlrRes4= Encryption(this).decryptCBC(jsonObject.getString("DLRRES4"))
                    val dlrRes5= Encryption(this).decryptCBC(jsonObject.getString("DLRRES5"))

                    val dlrDetails = DealerDetailsEntity(
                        dealerId,dlrCode,mobNo1, mobNo2, mobNo3, dlrName, dlrShopName, dlrShopAdd1,dlrShopAdd2,dlrShopAdd3,
                        dlrShopPincode,dlrState,dlrDOB, engId1,engId2,engId3,engId4,engName1,engName2,engName3,engName4,
                        engNumber1,engNumber2,engNumber3,engNumber4, dlrRes1,dlrRes2, dlrRes3, dlrRes4, dlrRes5
                    )
                    val jsonDlrDetails = Gson().toJson(dlrDetails)
                    sharedPreferences.edit().putString("jsonDealerDetails", jsonDlrDetails).apply()
                    sharedPreferences.edit().putBoolean("loginCheck", true).apply()
                    detailsCheck = true
                    showSnackBar("DAta fetched")
                    showProgressDialog().hideProgressDialog(mProgressDialog)
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                }


            },
            Response.ErrorListener {
                println("Error is $it")
                detailsCheck = false
                showProgressDialog().hideProgressDialog(mProgressDialog)
                //Showing error is some error ocurred while fetching data
                showSnackBar("Please check your internet")

            }
        ){
            override fun getHeaders(): MutableMap<String, String> {
                return super.getHeaders()
            }
        }
        VolleySingleton(this).addToRequestQueue(jsonArrayRequest)
        Log.e("checkdata", detailsCheck.toString())
    }

    fun validateNumber():Boolean{
        return when {
            TextUtils.isEmpty(et_mobile_number.text.toString().trim() { it <= ' '}) -> {
                showSnackBar("Please Enter Mobile number")
                false
            }
            TextUtils.isEmpty(et_OTP.text.toString().trim() { it <= ' '}) -> {
                showSnackBar("Please Enter OTP")
                false
            }

            else ->{
                true
            }
        }
    }

    fun onlyValidateNumber():Boolean{
        return when {
            TextUtils.isEmpty(et_mobile_number.text.toString().trim() { it <= ' '}) -> {
                showSnackBar("Please Enter Mobile number")
                false
            }

            else ->{
                true
            }
        }
    }

}


