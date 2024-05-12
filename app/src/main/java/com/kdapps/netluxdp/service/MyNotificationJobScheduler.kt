package com.kdapps.netluxdp.service

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.kdapps.netluxdp.database.ActivationDBAsyncTask
import com.kdapps.netluxdp.entities.ActivationDetailsEntity
import com.kdapps.netluxdp.utils.Encryption
import com.kdapps.netluxdp.utils.VolleySingleton
import org.json.JSONObject
import java.util.HashMap

class MyNotificationJobScheduler: JobService() {
    var Str :String = "http://byteseq.com/temp/"

    companion object {
        private const val TAG = "NotificationJobService"
        private const val TIME_SLEEP_MILLISECONDS: Long = 1000
    }

    private var jobCanceled : Boolean = false
    lateinit var sharedPreferences: SharedPreferences

    private fun doBackgroundWork(p0: JobParameters?){

        sharedPreferences = this.getSharedPreferences("dealer_details", MODE_PRIVATE)
        val tokenId = sharedPreferences.getString("FCM_tokenID", "Not Found")
        val dlrId = sharedPreferences.getString("dlrId", "")
        val latestDate = sharedPreferences.getString("latest_activation_date","")

        Thread(Runnable {
            kotlin.run {


                val queue = Volley.newRequestQueue(this)
                val url = Str + "newactivationdetails.php?dlrid=$dlrId&last=$latestDate"
                Log.e(TAG, url)
                val jsonArrayRequest = object : JsonArrayRequest(
                    Request.Method.GET,
                    url,
                    null,
                    Response.Listener{
                        Log.e(TAG, it.length().toString())

                        var newActivationCount:Int = 0
                        for(i in 0 until it.length()){
                            Log.e("Fetching","Getting detials")
                            val jsonObject = it.getJSONObject(i)
                            val dealerId = jsonObject.getString("DLRID")
                            val name = Encryption(this).decryptCBC(jsonObject.getString("CUNAME"))
                            val number = Encryption(this).decryptCBC(jsonObject.getString("CUNUMBER"))
                            val LotNumber = Encryption(this).decryptCBC(jsonObject.getString("CULOTNO"))
                            val key = Encryption(this).decryptCBC(jsonObject.getString("CUPKEY"))
                            val iKey = Encryption(this).decryptCBC(jsonObject.getString("CUIKEY"))
                            val aKey = Encryption(this).decryptCBC(jsonObject.getString("CUAKEY"))
                            val date = Encryption(this).decryptCBC(jsonObject.getString("ADATE"))
                            val expDate = Encryption(this).decryptCBC(jsonObject.getString("EXPDATE"))
                            val emailId = Encryption(this).decryptCBC(jsonObject.getString("CUEMAILID"))
                            val state = Encryption(this).decryptCBC(jsonObject.getString("CUSTATE"))
                            val city = Encryption(this).decryptCBC(jsonObject.getString("CUCITY"))
                            val pincode = Encryption(this).decryptCBC(jsonObject.getString("CUPINCODE"))
                            val engId = Encryption(this).decryptCBC(jsonObject.getString("ENGID"))
                            val actRes1 = Encryption(this).decryptCBC(jsonObject.getString("ACTRES1"))
                            val actRes2 = Encryption(this).decryptCBC(jsonObject.getString("ACTRES2"))
                            val actRes3 = Encryption(this).decryptCBC(jsonObject.getString("ACTRES3"))
                            val actRes4 = Encryption(this).decryptCBC(jsonObject.getString("ACTRES4"))
                            val actRes5 = Encryption(this).decryptCBC(jsonObject.getString("ACTRES5"))


                            val userEntity = ActivationDetailsEntity(dealerId,name,number,LotNumber,key,iKey,
                                aKey,expDate, date, emailId, state,city,pincode, engId,actRes1,actRes2,
                                actRes3,actRes4,actRes5)
                            //checking if the data is already entered in the local database
                            val check = ActivationDBAsyncTask(this, userEntity, 3).execute().get()

                            if (!check){
                                //adding the activation details to the local database
                                newActivationCount++
                                val async = ActivationDBAsyncTask(this, userEntity, 1).execute()
                                val result = async.get()
                                //Log.e("result",result.toString())

                            }


                        }
                        if (tokenId != null) {
                            sendFCMNotification(tokenId, newActivationCount)
                        }


                    },
                    Response.ErrorListener {

                        println("Error is $it")
                        //Showing error is some error ocurred while fetching data
                        }
                ){
                    override fun getHeaders(): MutableMap<String, String> {
                        return super.getHeaders()
                    }
                }
                //executing the API call
                queue.add(jsonArrayRequest)
                Log.d(TAG, "Job finish")
                jobFinished(p0, false)
            }
        }).start()
    }


    override fun onStartJob(p0: JobParameters?): Boolean {
        Log.d(TAG, "Job Started")
        doBackgroundWork(p0)
        return true
    }

    override fun onStopJob(p0: JobParameters?): Boolean {
        Log.d(TAG, "Job canceled before completion")
        jobCanceled = true
        return true
    }


    private fun sendFCMNotification(tokenId:String, activations:Int){
        val queue2 = Volley.newRequestQueue(this)
        val url2 = "https://fcm.googleapis.com/fcm/send"

        val jsonArray = JSONObject()
        jsonArray.put("title","NXAV DP")
        jsonArray.put("body","You have $activations new activation.")

        val params = JSONObject()
        params.put("data",jsonArray)
        params.put("to",tokenId)

        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.POST,
            url2,
            params,
            Response.Listener {

                Log.d(TAG, it.toString())
                val check = it.getInt("success")
                if(check == 1){
                    Log.d(TAG, "Notification send via API call")
                }
            },
            Response.ErrorListener {
                println("Error in API call is $it")
            }
        )

        {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "key=AAAA6E6vfW4:APA91bGRR6fsANkeSHiB5RPITOGN21R-TJ2G0UEu9dQsp8FD3oCETHRtdM6N8dlqBHA5Gq0lKBVEYIXoG6aGd4yhcBXmc1DSrPERiRKISQd8mcTTuu4HRJbXL7mOrN4MrYXoRfTJgdmf"
                headers["Content-Type"] = "application/json"
                return headers
            }
        }
        jsonObjectRequest.setRetryPolicy(
            DefaultRetryPolicy(0 , -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        )
        queue2.add(jsonObjectRequest)
    }


}

