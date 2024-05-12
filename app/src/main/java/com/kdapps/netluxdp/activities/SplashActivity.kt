package com.kdapps.netluxdp.activities

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.kdapps.netluxdp.R
import com.kdapps.netluxdp.utils.ConnectionManager
import com.kdapps.netluxdp.utils.VolleySingleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    lateinit var sharedPreferences: SharedPreferences
    var Str :String = "http://byteseq.com/temp/"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        sharedPreferences = this.getSharedPreferences("dealer_details", MODE_PRIVATE)
        val loginCheck = sharedPreferences.getBoolean("loginCheck", false)

        Handler().postDelayed({
            if(loginCheck){
                val dlrID = sharedPreferences.getString("dlrId","Not Found")
                val profile_check = sharedPreferences.getBoolean("profile_check",false)
                if(ConnectionManager().checkConnectivity(this) && !profile_check){
                    val url = Str + "checkdlrdetails.php?DLRID=$dlrID"
                    GlobalScope.launch(Dispatchers.IO) {
                        val jsonArrayObject = object : JsonArrayRequest(
                            Request.Method.GET,
                            url,
                            null,
                            Response.Listener {

                                val size  = it.length()
                                if(size == 0){
                                    /////Currently checkDlrDetails link is not active thats why directly opening mainActivity otherwise open CompleteProfileActivity
                                    val intent = Intent(this@SplashActivity, MainActivity::class.java)
                                    startActivity(intent)
                                    this@SplashActivity.finish()
                                }else{
                                    sharedPreferences.edit().putBoolean("profile_check", true).commit()
                                    val intent = Intent(this@SplashActivity, MainActivity::class.java)
                                    startActivity(intent)
                                    this@SplashActivity.finish()
                                }
                            },
                            Response.ErrorListener {

                            }
                        ){}
                        VolleySingleton(this@SplashActivity).addToRequestQueue(jsonArrayObject)
                    }
                }
                else{
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    this.finish()
                }

            }
            else{
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                this.finish()
            }

        },1000)
    }
}