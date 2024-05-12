package com.kdapps.netluxdp.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.*
import com.android.volley.toolbox.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.messaging.FirebaseMessaging
import com.kdapps.netluxdp.R
import com.kdapps.netluxdp.database.ActivationDBAsyncTask
import com.kdapps.netluxdp.database.OpenAsuncTask
import com.kdapps.netluxdp.entities.ActivationDetailsEntity
import com.kdapps.netluxdp.entities.OpenDetailsEntity
import com.kdapps.netluxdp.fragments.*
import com.kdapps.netluxdp.service.MyNotificationJobScheduler
import com.kdapps.netluxdp.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.progress_bar_layout.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var mProgressDialog: Dialog
    lateinit var sharedPreferences: SharedPreferences
    lateinit var fromDate:String
    var checkContinueClicks:Boolean = false
    lateinit var appUpdateManager: AppUpdateManager
    val MY_REQUEST_CODE = 1100
    var Str :String = "http://byteseq.com/temp/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedPreferences = this.getSharedPreferences("dealer_details", MODE_PRIVATE)

        this.supportActionBar?.title = "Netlux Delar Portal"
        setupToolbar()
        //setting version locally
        //for testing 9 is set. version 9 is uploaded on playstore
        sharedPreferences.edit().putString("CurrentVersion","12").apply()
        appUpdateManager = AppUpdateManagerFactory.create(this)
        checkForAppUpdate()
        val dlrId = sharedPreferences.getString("dlrId","Not Found")

        if (dlrId != null) {
            checkReset(dlrId)
        }
//        if (dlrId != null) {
//            resetData(dlrId)
//        }

        val checkNewFeatureStatus = sharedPreferences.getBoolean("new_feature_opened", false)
        if(checkNewFeatureStatus){
            img_new_feature_added.visibility = View.INVISIBLE
        }

        if(ConnectionManager().checkConnectivity(this)){
            checkManuallyForUpdate()
        }

        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val curDate = sdf.format(Date())
        val curYear = curDate.substring(6).toInt()
        val curMonth = curDate.substring(3,5).toInt()
        val curDayofMonth = curDate.substring(0,2).toInt()
        fromDate = ProperDate().getProperDate(curYear, (curMonth-1), 1)
        Log.e("properDate", fromDate)


        //giving click event to open the specific fragment when click on accordingly cardView
        card_my_activations.setOnClickListener(){
            //checkContinueClicks = true
            if(!checkContinueClicks){
                getFragment(MyActivationsFragment(),"My Activation", it)
            }

        }
        card_my_renewal.setOnClickListener(){
            if(!checkContinueClicks){
                getFragment(MyRenewalsFragment(),"My Renewals",it)
            }

        }
        card_current_scheme.setOnClickListener(){
            if(!checkContinueClicks){
                getFragment(CurrentSchemeFragment(),"Current Scheme", it)
            }

        }
        card_customer_search.setOnClickListener(){
            if(!checkContinueClicks){
                getFragment(CustomerSearchFragment(),"Customer Search", it)
            }

        }
        card_nxav_rewards.setOnClickListener(){
            if(!checkContinueClicks){
                if(ConnectionManager().checkConnectivity(this)){
                    getFragment(MyRewardsFragment2(),"My Rewards", it)
                }
                else{
                    val dialog = AlertDialog.Builder(this)
                        .setTitle("Dear User")
                        .setMessage("Device must be connected to Internet to open Rewards Section.")
                        .setCancelable(false)
                        .setPositiveButton("OK") { text, listener ->

                        }
                        .create()
                        .show()
                }

            }

        }
        card_nxav_topup.setOnClickListener(){
            if(!checkContinueClicks){
                getFragment(TopupNXAVFragment(),"Topup", it)
            }

        }
        card_order_nxav.setOnClickListener(){
            if(!checkContinueClicks){
                getFragment(OrderNXAVFragment(),"Order NXAV", it)
            }

        }
        card_my_profile.setOnClickListener(){
            if(!checkContinueClicks){
                getFragment(ProfileFragment(),"My Profile", it)
            }

        }
        card_marketing_helpline.setOnClickListener(){
            if(!checkContinueClicks){
                getFragment(MarketingHelplineFragment(),"NXAV Helpline", it)
            }

        }



        //getting previous data

        val previousDataCheck = sharedPreferences.getBoolean("previousData",false)
        val reqDataAfterReset = sharedPreferences.getBoolean("reqReset",false)
        Log.e("dlrIdCheck", dlrId.toString())
        val queue = Volley.newRequestQueue(this)
        //val url = "http://byteseq.com/temp/activationdetails.php?dlrid=$dlrId"
        val url = Str + "activationdetailsV2.php?dlrid=$dlrId"
        //getting dealer's previous activation details
        if(!previousDataCheck && !reqDataAfterReset){
            //showing progress bar to the user
            showProgressDialog("Alert 🚨\n" +"\n"+
                    "Syncing your data\n" +
                    "Do not close this app\n" +
                    "It may take a while\uD83D\uDE4F\uD83C\uDFFB")
            //getting previous customer activation details of dealer
            //checking internet connection of the user
            if(ConnectionManager().checkConnectivity(this)){
                //fetching data in the json from the server
                GlobalScope.launch(Dispatchers.IO) {
                    Log.e("threadInfo", "Current thread is ${Thread.currentThread().name}")
                    getPreviousData(url)
                }

            }

            else{
                val dialog = AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("connection not found")
                    .setPositiveButton("Retry") { text, listener ->
                        GlobalScope.launch(Dispatchers.IO) {
                            Log.e("threadInfo", "Retry thread is ${Thread.currentThread().name}")
                            getPreviousData(url)
                        }

                    }
                    .setNegativeButton("Open Settings") { text, listener ->
                        val settingIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                        startActivity(settingIntent)

                    }
                    .create()
                    .show()
            }
        }
//        else if(!reqDataAfterReset){
//
//        }


        btn_refresh.setOnClickListener(){
            val finalDate = sharedPreferences.getString("latest_activation_date", "")
            val url = Str + "newactivationdetailsV2.php?dlrid=$dlrId&last=$finalDate"
            //val queue = Volley.newRequestQueue(this)
            this.supportFragmentManager.popBackStack()

            showProgressDialog("Getting Details...")
            GlobalScope.launch(Dispatchers.IO) {
                Log.e("threadInfo", "Refresh thread is ${Thread.currentThread().name}")
                getPreviousData(url)
            }

        }

        val sdf2 = SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS")
        val timeStamp = sdf2.format(Date())
        val openDetails = dlrId?.let { OpenDetailsEntity(it, timeStamp) }
        if(openDetails != null){
            val result = OpenAsuncTask(this, openDetails,1).execute().get()
            Log.e("OpenDetails",result.toString())
        }




        //get the latest Firebase tokenId to send notification
        getFCMToken()
        //scheduling the background job to check the new activations
        //scheduleJob()

    }

    fun setupToolbar(){
        setSupportActionBar(main_toolbar)
        supportActionBar?.setTitle("Netlux Delar Portal")
//        supportActionBar?.setHomeButtonEnabled(true)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun checkManuallyForUpdate(){
        val url = Str +"checkversion.php"
        val curVersion = sharedPreferences.getString("CurrentVersion","")
        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            Response.Listener {
                val version = it.getString("RESULT")
                if(curVersion != "" && curVersion != null){
                    if (version.toInt() > curVersion.toInt()) {
                        val dialog = AlertDialog.Builder(this)
                            .setTitle("Dear User")
                            .setMessage("New update is available for download. Please update the app to continue using Netlux DP.")
                            .setCancelable(false)
                            .setPositiveButton("Download") { text, listener ->
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse(
                                        "https://play.google.com/store/apps/details?id=com.kdapps.netluxdp")
                                    setPackage("com.android.vending")
                                }
                                startActivity(intent)
                                this.finishAffinity()

                            }
                            .setNegativeButton("Cancel"){text, lisetener ->
                                this.finishAffinity()

                            }
                            .create()
                            .show()

                    }
                }

            },
            Response.ErrorListener {
                println("Error is $it")
            }
        ){}
        VolleySingleton(this).addToRequestQueue(jsonObjectRequest)
    }

    //checking for new version uplaoded on playstore
    private fun checkForAppUpdate(){
        // Returns an intent object that you use to check for an update.
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            Log.e("updateInfo", appUpdateInfo.updateAvailability().toString())
            Log.e("updateInfo", UpdateAvailability.UPDATE_AVAILABLE.toString())
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                // This example applies an immediate update. To apply a flexible update
                // instead, pass in AppUpdateType.FLEXIBLE
                //&& (appUpdateInfo.clientVersionStalenessDays() ?: -1) >= 4
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                // Request the update.
                appUpdateManager.startUpdateFlowForResult(
                    // Pass the intent that is returned by 'getAppUpdateInfo()'.
                    appUpdateInfo,
                    // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                    AppUpdateType.IMMEDIATE,
                    // The current activity making the update request.
                    this,
                    // Include a request code to later monitor this update request.
                    MY_REQUEST_CODE)
            }
        }
    }

    //managing downloading of update
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MY_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    Log.d(TAG, "" + "Result Ok")
                    //  handle user's approval }
                }
                Activity.RESULT_CANCELED -> {

                        val dialog = AlertDialog.Builder(this)
                            .setTitle("Dear User")
                            .setMessage("You must install the update to use our app")
                            .setCancelable(false)
                            .setPositiveButton("OK") { text, listener ->
                                this.finishAffinity()

                            }
                            .create()
                            .show()
                        //if you want to request the update again just call checkUpdate()


                    Log.d(TAG, "" + "Result Cancelled")
                    //  handle user's rejection  }
                }
                ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> {
                    //if you want to request the update again just call checkUpdate()
                    Log.d(TAG, "" + "Update Failure")
                    //  handle update failure
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability()
                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                ) {
                    // If an in-app update is already running, resume the update.
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        IMMEDIATE,
                        this,
                        MY_REQUEST_CODE
                    );
                }
            }
    }

    //to open different fragment section
    private fun getFragment(fragment: Fragment,fragmentName:String, view: View){
        checkContinueClicks = true
        mProgressDialog = Dialog(this)
        showProgressDialog().showProgressDialog(mProgressDialog, "Loading $fragmentName")
        ButtonAnalysis(this).addButtonDetails(view.tag.toString(), "res1", "res2")

        Handler().postDelayed({
            showProgressDialog().hideProgressDialog(mProgressDialog)
            checkContinueClicks = false
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_frame_layout, fragment)
                .addToBackStack("$fragment")
                .setPrimaryNavigationFragment(fragment)
                .commit()
        },1000)

    }

    fun showProgressDialog(text: String){
        mProgressDialog = Dialog(this)

        mProgressDialog.setContentView(R.layout.progress_bar_layout)

        mProgressDialog.progress_text.text = text

        mProgressDialog.setCancelable(false)
        mProgressDialog.setCanceledOnTouchOutside(false)

        //start progress dialog
        mProgressDialog.show()
    }

    fun hideProgressDialog(){
        mProgressDialog.dismiss()
    }

    class getCurrentMonthActivationCount(val context: Context, val fromDate:String)
    :AsyncTask<Void, Void, Int>(){
        override fun doInBackground(vararg p0: Void?): Int {
            val db = RoomDatabaseBuilder(context).getRoomDatabase()
            //this is month wise count
            //val result = db.activationDetailsDao().getCurrentMonthActivationCount(fromDate)

            //this is total activations from 1 may 2022
            val result1 = db.activationDetailsDao().getActivationCountFromFeb("2022-05-01")
            db.close()
            return result1
        }

    }


    //get latest activation date. To fetch the further new activation
    class getLatestDate(val context: Context):AsyncTask<Void, Void, String>(){
        override fun doInBackground(vararg p0: Void?): String {
            val db = RoomDatabaseBuilder(context).getRoomDatabase()
            val result = db.activationDetailsDao().getLatestActivationDate()
            db.close()
            return result
        }

    }

    private fun RefreshActivationData(){

    }

    private fun resetData(dlrId: String){
        //--------------updating data with the product type--------------\\
        val reqReset = sharedPreferences.getBoolean("reqReset", false)
        Log.e("Reset","Value of reqRequest is $reqReset")
        val curr_version = sharedPreferences.getString("CurrentVersion","1")
        val dataUpdatedWithProdType = sharedPreferences.getBoolean("dataUpdatedWithProdType",false)
        if((curr_version == "9" && !dataUpdatedWithProdType) || reqReset ){

            val acts = getCurrentMonthActivationCount(this,"").execute().get()
            Log.e("reset","Activations before reset in DB id $acts")
            val deleteActi = ProfileFragment.deleteALlActivations(this).execute().get()
            val deleteBtn = ProfileFragment.deleteALlButtons(this).execute().get()
            val deleteReward = ProfileFragment.deleteALlRewards(this).execute().get()

            sharedPreferences.edit().putBoolean("previousData", false).apply()
            sharedPreferences.edit().putBoolean("dataUpdatedWithProdType", true).apply()

            val activations = getCurrentMonthActivationCount(this,"").execute().get()

            Log.e("reset","data reset successful")
            Log.e("reset","Activations after reset in DB id $activations")
            if(reqReset) getDataAfterReset(dlrId)
            setResetDone(dlrId)


        }
    }

    private  fun getPreviousData(url:String){
        //get the previous activations details of the dealer
        //fetching data in the json from the server
        val activations = getCurrentMonthActivationCount(this, "").execute().get()
        Log.e("reset","Activations in DB id $activations")
        Log.e("reset","PreviousData is called with url: $url")
        val jsonArrayRequest = object : JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            Response.Listener{
                try{
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
                        val pincode =Encryption(this).decryptCBC(jsonObject.getString("CUPINCODE"))
                        val engId = Encryption(this).decryptCBC(jsonObject.getString("ENGID"))
                        val actRes1 = Encryption(this).decryptCBC(jsonObject.getString("ACTRES1"))
                        val actRes2 = Encryption(this).decryptCBC(jsonObject.getString("PROTYPE"))
//                        val actRes3 = Encryption(this).decryptCBC(jsonObject.getString("ACTRES3"))
//                        val actRes4 = Encryption(this).decryptCBC(jsonObject.getString("ACTRES4"))
//                        val actRes5 = Encryption(this).decryptCBC(jsonObject.getString("ACTRES5"))

                        val actRes3 = "RES3"
                        val actRes4 = "RES4"
                        val actRes5 = "RES5"


                        val userEntity = ActivationDetailsEntity(dealerId,name,number,LotNumber,key,iKey,
                            aKey,expDate, date, emailId, state,city,pincode, engId,actRes1,actRes2,
                            actRes3,actRes4,actRes5)
                        //checking if the data is already entered in the local database
                        val check = ActivationDBAsyncTask(this, userEntity, 3).execute().get()
                        Log.e("reset", url + userEntity.aDate.toString())
                        if (!check){
                            //adding the activation details to the local database
                            val async = ActivationDBAsyncTask(this, userEntity, 1).execute()
                            val result = async.get()
                            //Log.e("result",result.toString())
                            if(result){
                                //Log.e("Fetched","Details fetched successfully.")
                            }
                        }
                        sharedPreferences.edit().putBoolean("previousData", true).apply()
                    }
                    Toast.makeText(this,"Details fetched Successfully",Toast.LENGTH_SHORT).show()
                    hideProgressDialog()
                    val count = getCurrentMonthActivationCount(this@MainActivity, fromDate).execute().get()
                    Log.e("finalDate_count",count.toString())
                    val points = count*25
                    Log.e("reset","Activations after getting data in DB is $count")
                    tv_current_month_activation_count.setText(count.toString())
                    val dlrId = sharedPreferences.getString("dlrId","")
                    if(url == Str + "activationdetailsV2.php?dlrid=$dlrId"){
                        //val finalDate = sharedPreferences.getString("latest_activation_date", "")
                        var finalDate = getLatestDate(this).execute().get()
                        if(finalDate == null){
                            finalDate = "2022-05-01"
                        }
                        //val url2 = "https://byteseq.com/temp/newactivationdetails.php?dlrid=$dlrId&last=$finalDate"
                        val url2 = Str + "newactivationdetailsV2.php?dlrid=$dlrId&last=$finalDate"
                        showProgressDialog("Getting Details...")
                        getPreviousData(url2)
                    }
                }
                catch (e: Exception){
                    Log.e("ServerError", e.toString())
                    val dialog = AlertDialog.Builder(this)
                        .setTitle("Server Error")
                        .setMessage("Details will be fetched after some time ")
                        .setPositiveButton("OK") { text, listener ->

                        }
                        .create()
                        .show()
                }


            },
            Response.ErrorListener {

                if(url != Str +"newdlrreg.php" ){
                    //put here the alternate site link to fetch data
                    //getPreviousData("http://byteseq.com/temp/newdlrreg.php")
                }
                println("Error is $it")
                hideProgressDialog()
                //Showing error is some error ocurred while fetching data
                Toast.makeText(this@MainActivity, " Something went wrong", Toast.LENGTH_LONG).show()
            }
        ){
            override fun getHeaders(): MutableMap<String, String> {
                return super.getHeaders()
            }
        }
        //executing the API call
        jsonArrayRequest.setRetryPolicy( DefaultRetryPolicy(10000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT))
        VolleySingleton(this).addToRequestQueue(jsonArrayRequest)
        }

    private fun checkReset(dlrId: String){
        val url1 = Str + "getresetstatus.php?dlrid=$dlrId";
        val jsonArrayRequest = object: JsonArrayRequest(
            Request.Method.GET,
            url1,
            null,
            Response.Listener {
                try {
                    val obj = it.getJSONObject(0);
                    val resetCheck = obj.getString("DLRMOB3")
                    if(resetCheck == "RESET"){
                        Log.e("reset","need to reset the data")
                        sharedPreferences.edit().putBoolean("reqReset",true).apply()

                        VolleySingleton(this).requestQueue.cancelAll(this)
                        resetData(dlrId)
                    }
                    Log.e("reset","No need to reset the data")
                }
                catch (e: Exception) {
                    val dialog = AlertDialog.Builder(this)
                        .setTitle("Server Error")
                        .setMessage("Details will be fetched after some time ")
                        .setPositiveButton("OK") { text, listener ->

                        }
                }
                val reqReset = sharedPreferences.getBoolean("reqReset",false)
                val previousData = sharedPreferences.getBoolean("previousData",false)
                if(!reqReset && previousData) getNewActivation(dlrId)
            },
            Response.ErrorListener {
                Log.e("reset", "Error in get reset link is $it")
            }
        ){}
        Volley.newRequestQueue(this).add(jsonArrayRequest)
    }

    private fun getNewActivation(dlrId: String){
        val count = getCurrentMonthActivationCount(this@MainActivity, fromDate).execute().get()
        val points = count*25
        tv_current_month_activation_count.setText(count.toString())
        //store the latest activation date in shared preference
        val latestDate = getLatestDate(this).execute().get()
        val date = latestDate.substring(0,9)
        val time = latestDate.substring(11)
        val finalDate = date + "%20" + time
        sharedPreferences.edit().putString("latest_activation_date", finalDate).apply()
        val url = Str +"newactivationdetailsV2.php?dlrid=$dlrId&last=$finalDate"
        showProgressDialog("Getting Details...")

        GlobalScope.launch(Dispatchers.IO) {
            Log.e("reset","previousData func is called from main with url:$url")
            Log.e("threadInfo", "Refresh thread is ${Thread.currentThread().name}")
            getPreviousData(url)
        }

    }

    private fun setResetDone(dlrId: String){
        sharedPreferences.edit().putString("latest_activation_date","2022-05-01").apply()
        val url2 =Str +"updateresetstatus.php?dlrid=$dlrId"
        Log.e("reset","Called set reset done")
        //make a string request
        val stringRequest = object :StringRequest(
            Request.Method.GET,
            url2,
            Response.Listener {
                Log.e("Reset","updating reset value")
                sharedPreferences.edit().putBoolean("reqReset", false).apply()
            },
            Response.ErrorListener {
                Log.e("reset","Reset update error is $it")
            }
        ){}

        VolleySingleton(this).addToRequestQueue(stringRequest)
    }

    private fun getDataAfterReset(dlrId: String){
        val previousDataCheck = sharedPreferences.getBoolean("previousData",false)
        Log.e("dlrIdCheck", dlrId.toString())
        Log.e("reset","Previous data check boolean is $previousDataCheck")
        val queue = Volley.newRequestQueue(this)
        //val url = "http://byteseq.com/temp/activationdetails.php?dlrid=$dlrId"
        val url = Str +"activationdetailsV2.php?dlrid=$dlrId"
        //getting dealer's previous activation details
        if(!previousDataCheck){
            Log.e("reset","Fetching previous data")
            //showing progress bar to the user
            showProgressDialog("Alert 🚨\n" + "\n"+
                    "Syncing your data\n" +
                    "Do not close this app\n" +
                    "It may take a while\uD83D\uDE4F\uD83C\uDFFB")
            //getting previous customer activation details of dealer
            //checking internet connection of the user
            val deleteActi = ProfileFragment.deleteALlActivations(this).execute().get()
            if(ConnectionManager().checkConnectivity(this)){
                //fetching data in the json from the server
                GlobalScope.launch(Dispatchers.IO) {
                    Log.e("threadInfo", "Current thread is ${Thread.currentThread().name}")
                    getPreviousData(url)
                }

            }

            else{

                val dialog = AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("connection not found")
                    .setPositiveButton("Retry") { text, listener ->
                        GlobalScope.launch(Dispatchers.IO) {
                            Log.e("threadInfo", "Retry thread is ${Thread.currentThread().name}")
                            getPreviousData(url)
                        }

                    }
                    .setNegativeButton("Open Settings") { text, listener ->
                        val settingIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                        startActivity(settingIntent)

                    }
                    .create()
                    .show()
            }
        }
    }

    private fun scheduleJob() {
        val componentName = ComponentName(this, MyNotificationJobScheduler::class.java)
        val info = JobInfo.Builder(987, componentName)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setRequiresCharging(false)
            .setPersisted(true)
            .setPeriodic(15*60*1000)
            .build()

        val jobScheduler: JobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val resultCode = jobScheduler.schedule(info)

        val isJobScheduledSuccess = resultCode == JobScheduler.RESULT_SUCCESS
        Log.d("Job Scheduler", "Job Scheduled ${if (isJobScheduledSuccess) "SUCCESS_KEY" else "FAILED_KEY"}")
    }

    private fun logoutCheck(){
        val tokenId = sharedPreferences.getString("FCM_tokenID", "Not Found")
        Log.e("Logout",tokenId.toString())
        val url1 = Str +"DoINeedToLogout.php?TOKEN=$tokenId"
        val url2 = Str +"UpdateLogoutFlag.php?TOKEN=$tokenId"
        Log.e("Logout",url1)
        val jsonRequest1 = object: JsonArrayRequest(
            Request.Method.GET,
            url1,
            null,
            Response.Listener {
                Log.e("Logout", it.toString())
                if(it.length() != 0){
                    val obj = it.getJSONObject(0)
                    val resVal = obj.getString("RES")
                    if(resVal == "9"){
                        if (tokenId != null) {
                            updateLogout(url2, tokenId)
                        }
                    }
                }

            },
            Response.ErrorListener {
                Log.e("LogoutCheck","Logout link 1 error is $it")
            }
        ){}
        VolleySingleton(this).addToRequestQueue(jsonRequest1)
    }

    private fun updateLogout(url: String, tokenId: String){
        val stringRequest = object :StringRequest(
            Request.Method.GET,
            url,
            Response.Listener {
                logoutUser(tokenId)
            },
            Response.ErrorListener {
                Log.e("LogoutCheck","Logout update error is $it")
            }
        ){}

        VolleySingleton(this).addToRequestQueue(stringRequest)
    }

    private fun logoutUser(tokenId: String){
        val deleteActi = ProfileFragment.deleteALlActivations(this).execute().get()
        val deleteBtn = ProfileFragment.deleteALlButtons(this).execute().get()
        val deleteReward = ProfileFragment.deleteALlRewards(this).execute().get()
        if(deleteActi && deleteBtn &&deleteReward){
            sharedPreferences.edit().clear().apply()

            val url =Str +"deletetoken.php?TOKEN=$tokenId"
            val jsonArrayRequest = object : JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                Response.Listener {

                },
                Response.ErrorListener {
                    println("Error is $it")
                }
            ){}
            VolleySingleton(this).addToRequestQueue(jsonArrayRequest)
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
            (this).finish()
        }
    }

    fun getFCMToken(){
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            Log.e("Logout","Token generated is $token")
            val previousToken = sharedPreferences.getString("FCM_tokenID", "Not Found")
            val dlrID = sharedPreferences.getString("dlrId", "Not Found")
            //saving the new token in shared preferences
            sharedPreferences = this.getSharedPreferences("dealer_details", MODE_PRIVATE)
            sharedPreferences.edit().putString("FCM_tokenID", token.toString()).apply()
            //sending the new token to server side
            var updateToken:Boolean = true
            GlobalScope.launch(Dispatchers.IO) {
                val url1 = Str +"checktoken.php?TOKEN=$previousToken"
                val jsonArrayRequest1 = object : JsonArrayRequest(
                    Request.Method.GET,
                    url1,
                    null,
                    Response.Listener {
                        Log.e("checkToken",it.toString())
                        updateToken = it.length() == 0
                        if(!updateToken) logoutCheck()
                    },
                    Response.ErrorListener {  }
                ){}
                VolleySingleton(this@MainActivity).addToRequestQueue(jsonArrayRequest1)

                delay(2500)
                if(updateToken){
                    val url2 =Str +"newtoken.php?DLRID=$dlrID&TOKEN=$token"
                    Log.e("checkToken","updating Token")
                    val tokenJsonArrayRequest = object :JsonArrayRequest(
                        Request.Method.POST,
                        url2,
                        null,
                        Response.Listener {
                            //get response of DONE instead of token ID, which confirms that each token is saved
                            if(it.length()> 0){
                                Log.e("checkToken","Token updated")
                                Log.e("token_update", "tokenId saved in database")
                            }
                            logoutCheck()
                        },
                        Response.ErrorListener {
                            println("Error is $it")
                        }

                    ){}
                    VolleySingleton(this@MainActivity).addToRequestQueue(tokenJsonArrayRequest)
                }
            }

            // Log and toast

            Log.d("tokenID", token.toString())

            })
    }

}
