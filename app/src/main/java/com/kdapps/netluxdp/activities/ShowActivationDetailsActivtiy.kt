package com.kdapps.netluxdp.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.room.Room
import com.kdapps.netluxdp.R
import com.kdapps.netluxdp.database.NXAVRoomDatabase
import com.kdapps.netluxdp.entities.ActivationDetailsEntity
import com.kdapps.netluxdp.utils.ButtonAnalysis
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_show_activation_details_activtiy.*

class ShowActivationDetailsActivtiy : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_activation_details_activtiy)

        //setting up the toolbar
        setupToolbar()
        //getting the selected customer's productKey from the intent
        val cupKey = intent.getStringExtra("cupKey")
        //retriving activation details from the given product key
        if(cupKey != null){
            val activationDetailsEntity = getActivationDetails(this, cupKey).execute().get()
            et_cu_name.setText(activationDetailsEntity.cuName)
            et_cu_mob.setText(activationDetailsEntity.cuNumber)
            et_cu_product_key.setText(activationDetailsEntity.cupkey)
            when(activationDetailsEntity.actRes2){
                "NXAV" ->{
                    et_cu_product_type.setText("Total Security")
                }

                "NXTS" ->{
                    et_cu_product_type.setText("Total Protection")
                }
                else ->{
                    et_cu_product_type.setText(activationDetailsEntity.actRes2)
                }
            }

            et_cu_lot_no.setText(activationDetailsEntity.cuLotNo)
            et_cu_a_date.setText(activationDetailsEntity.aDate.substring(0,10))
            et_cu_exp_date.setText(activationDetailsEntity.expDate.substring(0,10))
            et_cu_exp_email_id.setText(activationDetailsEntity.cuEmailId)
            et_cu_state.setText(activationDetailsEntity.cuState)
            et_cu_city.setText(activationDetailsEntity.cuCity)
            et_cu_pincode.setText(activationDetailsEntity.cuPincode)
            et_dealer_code.setText("")
            et_cu_eng_id.setText(activationDetailsEntity.engId)
        }

        call_customer.setOnClickListener(){
            ButtonAnalysis(this).addButtonDetails(it.tag.toString(), "res1", "res2")
            val mob_number = et_cu_mob.text
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mob_number))
            startActivity(intent)
        }

    }

    fun setupToolbar(){
        setSupportActionBar(activation_toolbar)
        supportActionBar?.setTitle("Netlux Delar Portal")
    }

    //method to retrive activation details from the local database
    class getActivationDetails(val context: Context, val cupKey:String)
        :AsyncTask<Void, Void, ActivationDetailsEntity>()
    {
        override fun doInBackground(vararg p0: Void?): ActivationDetailsEntity {
            val db = Room.databaseBuilder(context, NXAVRoomDatabase::class.java, "NXAV-db" ).build()
            return db.activationDetailsDao().checkActivationEntry(cupKey)
        }

    }
}