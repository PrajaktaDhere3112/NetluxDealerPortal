package com.kdapps.netluxdp.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.kdapps.netluxdp.BuildConfig
import com.kdapps.netluxdp.R
import com.kdapps.netluxdp.activities.LoginActivity
import com.kdapps.netluxdp.activities.MainActivity
import com.kdapps.netluxdp.activities.RegisterActivity
import com.kdapps.netluxdp.utils.RoomDatabaseBuilder
import com.kdapps.netluxdp.utils.VolleySingleton
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    lateinit var sharedPreferences: SharedPreferences
    var Str :String = "http://byteseq.com/temp/"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        (activity as MainActivity).supportActionBar?.title ="My Profile"
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        sharedPreferences = (activity as Context).getSharedPreferences("dealer_details", MODE_PRIVATE)

        val version = BuildConfig.VERSION_CODE

        view.tv_version_name.setText("11.10.10.$version")
        Log.e("version", version.toString())
        val tokenId = sharedPreferences.getString("FCM_tokenID","")


        view.btn_logout.setOnClickListener(){
            val dialog = AlertDialog.Builder(activity as Context)
                .setCancelable(false)
                .setTitle("Confirmation")
                .setMessage("Are you sure you want to Logout?")
                .setPositiveButton("NO") { text, listener ->

                }
                .setNegativeButton("YES") { text, listener ->

                    val deleteActi = deleteALlActivations(activity as Context).execute().get()
                    val deleteBtn = deleteALlButtons(activity as Context).execute().get()
                    val deleteReward = deleteALlRewards(activity as Context).execute().get()
                    if(deleteActi && deleteBtn &&deleteReward){
                        sharedPreferences.edit().clear().apply()

                        GlobalScope.launch(Dispatchers.IO) {
                            val url =Str + "deletetoken.php?TOKEN=$tokenId"
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
                            VolleySingleton(activity as Context).addToRequestQueue(jsonArrayRequest)
                        }
                        val intent = Intent(activity, LoginActivity::class.java)
                        startActivity(intent)
                        (activity as MainActivity).finish()
                    }

                }
                .create()
                .show()
        }

        return view
    }

    class deleteALlActivations(val context: Context):AsyncTask<Void, Void,Boolean>(){
        override fun doInBackground(vararg p0: Void?): Boolean {
            val db = RoomDatabaseBuilder(context).getRoomDatabase()
            db.activationDetailsDao().DeleteAllActivationDetails()
            return true
        }
    }
    class deleteALlButtons(val context: Context):AsyncTask<Void, Void,Boolean>(){
        override fun doInBackground(vararg p0: Void?): Boolean {
            val db = RoomDatabaseBuilder(context).getRoomDatabase()
            db.buttonClickedDetailsDao().clearAllButtonDetails()
            return true
        }
    }

    class deleteALlRewards(val context: Context):AsyncTask<Void, Void,Boolean>(){
        override fun doInBackground(vararg p0: Void?): Boolean {
            val db = RoomDatabaseBuilder(context).getRoomDatabase()
            db.rewardDetailsDao().DeleteAllRewardDetails()
            return true
        }
    }
    override fun onDestroyView() {
        (activity as MainActivity).supportActionBar?.title =getString(R.string.app_name)
        super.onDestroyView()
    }


}