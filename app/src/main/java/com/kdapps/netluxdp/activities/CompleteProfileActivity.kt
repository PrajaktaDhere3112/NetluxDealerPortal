package com.kdapps.netluxdp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.kdapps.netluxdp.R
import kotlinx.android.synthetic.main.activity_complete_profile.*

class CompleteProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complete_profile)

        btn_submit_profile.setOnClickListener(){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            this.finishAffinity()
        }

    }

}