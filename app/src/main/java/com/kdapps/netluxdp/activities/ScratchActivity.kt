package com.kdapps.netluxdp.activities

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.anupkumarpanwar.scratchview.ScratchView
import com.kdapps.netluxdp.R
import com.kdapps.netluxdp.database.RewardDBAsyncTask
import com.kdapps.netluxdp.entities.RewardDetailsEntity
import com.kdapps.netluxdp.fragments.RewardDataModel
import com.kdapps.netluxdp.utils.Encryption
import com.kdapps.netluxdp.utils.VolleySingleton
import com.kdapps.netluxdp.utils.showProgressDialog
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_scratch.*
import kotlinx.android.synthetic.main.fragment_my_rewards.view.*
import kotlinx.android.synthetic.main.fragment_scratch_card.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ScratchActivity : AppCompatActivity() {

    lateinit var sharedPreferences: SharedPreferences
    var count: Int = 0
    var Str :String = "http://byteseq.com/temp/"
    private val itemSizeArray = ArrayList<RewardDataModel>()
    lateinit var mProgressDialog: Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scratch)

        sharedPreferences = this.getSharedPreferences("dealer_details",
            Context.MODE_PRIVATE
        )
        val rewardKey = this.intent.getStringExtra("rewardKey")
        val month = this.intent.getStringExtra("month")
        val idX = this.intent.getStringExtra("idX")
        tv_show_activation_key.setText(rewardKey)
        img_cancel.setOnClickListener(){
            onBackPressed()
        }

        scratch_view.setRevealListener(object : ScratchView.IRevealListener {
            override fun onRevealed(scratchView: ScratchView) {
                val party = Party(
                    speed = 0f,
                    maxSpeed = 40f,
                    damping = 0.9f,
                    spread = 360,
                    colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                    emitter = Emitter(duration = 3, TimeUnit.SECONDS).perSecond(300),
                    position = Position.Relative(0.5, 0.3)
                )
                konfettiView.start(party)

                val mMediaPlayer = MediaPlayer.create(this@ScratchActivity, R.raw.cheer)
                mMediaPlayer.start()
                scratchView.reveal()
                val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
                val curDateTime = sdf.format(Date())

                val dlrId = sharedPreferences.getString("dlrId", "")
                if(rewardKey != null && month != null){
                    val rewardDetailsEntity = dlrId?.let { RewardDetailsEntity(it, month ,rewardKey, false, curDateTime) }
                    if(rewardDetailsEntity != null){
                        val update1 = RewardDBAsyncTask(this@ScratchActivity, rewardDetailsEntity, 2).execute().get()
                        val update2 = RewardDBAsyncTask(this@ScratchActivity, rewardDetailsEntity, 3).execute().get()
                        GlobalScope.launch(Dispatchers.IO) {
                            postrRewardDetails(curDateTime,idX.toString())
                        }

                    }
                }

                GlobalScope.launch(Dispatchers.Main) {
                    delay(15000)
                    this@ScratchActivity.finish()
                }

                GlobalScope.launch(Dispatchers.Main) {
                    delay(3000)
                    showProgressDialog().showProgressDialog(
                        mProgressDialog,
                        "Refreshing"
                    )
                    getPreviousRewardsOnline()

                }

            }

            override fun onRevealPercentChangedListener(scratchView: ScratchView, percent: Float) {
                if (percent >= 0.3) {
                    scratchView.reveal()
                    //Log.d("Reveal Percentage", "onRevealPercentChangedListener: $percent")
                }
            }
        })

    }
    private fun getPreviousRewardsOnline() {
        Log.e("processUpdate", "getting previous rewards online")
        val dlrId = sharedPreferences.getString("dlrId", "")
        val url = Str + "getprevrewkeys.php?DLRID=$dlrId"
        val jsonArrayRequest =
            object : JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                Response.Listener {
                    for (i in 0 until it.length()) {
                        Log.e("processUpdate", "getting each previous rewards online")
                        val jsonObject = it.getJSONObject(i)
                        val month = jsonObject.getString("MONTH")
                        val monthInt = month.replace("-", "")
                        val idX = jsonObject.getString("IDX")
                        val rewardKey = Encryption(this as Context).decryptCBCRewardKey(jsonObject.getString("REWARDKEY"))
                        //Log.e("newRewardKey", rewardKey)
                        val ifScratchedString = jsonObject.getString("IFSCRATCHED")
                        var ifScratched = false
                        if (ifScratchedString == "TRUE") ifScratched = true
                        val whenScratched = jsonObject.getString("WHENSCRATCHED")
                        //key is used or not
                        val res = jsonObject.getString("RES")
                        count++
                        itemSizeArray.add(RewardDataModel(month, idX, rewardKey, ifScratched, whenScratched, res))

                    }

                    sharedPreferences.edit().putBoolean("PreviousRewardDetailsFetched", true)
                        .apply()


                },
                Response.ErrorListener {
                    println("Error in getting previous reward data is $it")
                    val dialog = AlertDialog.Builder(this)
                        .setTitle("Dear user")
                        .setMessage("Please try after some time.")
                        .setPositiveButton("OK") { text, listener ->
                            this?.finish()
                        }
                        .create()
                        .show()
                }
            ) {}
        VolleySingleton(this as Context).addToRequestQueue(jsonArrayRequest)
    }

    fun postrRewardDetails(time: String, idX:String){
        val url = Str + "updateoffer51.php?IDX=$idX&WHENSCRATCHED=$time"
        val jsonArrayRequest = object : JsonArrayRequest(
            Request.Method.POST,
            url,
            null,
            Response.Listener {
                              //Log.e("Show Submitted","true")
            },
            Response.ErrorListener {
                println("Error while submitting reward details is $it")
            }
        ){

        }
        VolleySingleton(this).addToRequestQueue(jsonArrayRequest)
    }
}