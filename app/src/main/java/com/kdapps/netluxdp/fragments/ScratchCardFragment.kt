package com.kdapps.netluxdp.fragments

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.anupkumarpanwar.scratchview.ScratchView
import com.kdapps.netluxdp.R
import com.kdapps.netluxdp.database.RewardDBAsyncTask
import com.kdapps.netluxdp.entities.RewardDetailsEntity
import com.kdapps.netluxdp.utils.VolleySingleton
import kotlinx.android.synthetic.main.fragment_scratch_card.view.*
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

class ScratchCardFragment : Fragment() {
    lateinit var sharedPreferences:SharedPreferences
    lateinit var month: String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_scratch_card, container, false)
        sharedPreferences = (activity as Context).getSharedPreferences("dealer_details",
            Context.MODE_PRIVATE
        )
        val bundle = this.arguments
        val rewardKey = bundle?.getString("rewardKey")
        month = bundle?.getString("month").toString()

        if (rewardKey != null) {
            Log.e("rewardKey",rewardKey)
        }
        view.tv_show_activation_key.setText(rewardKey)
        view.img_cancel.setOnClickListener(){
            requireActivity().onBackPressed()
        }

        view.scratch_view.setRevealListener(object : ScratchView.IRevealListener {
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
                view.konfettiView.start(party)

                val mMediaPlayer = MediaPlayer.create(activity as Context, R.raw.cheer)
                mMediaPlayer.start()
                scratchView.reveal()


                val dlrId = sharedPreferences.getString("dlrId", "")
                val month = "2022-01"
                if(rewardKey != null){
                    val rewardDetailsEntity = dlrId?.let { RewardDetailsEntity(it, month ,rewardKey, false, "now") }
                    if(rewardDetailsEntity != null){
                        val result = RewardDBAsyncTask(activity as Context, rewardDetailsEntity, 2).execute().get()
                        //val result = RewardDBAsyncTask(activity as Context, rewardDetailsEntity, 1).execute().get()
                    }
                }


            }

            override fun onRevealPercentChangedListener(scratchView: ScratchView, percent: Float) {
                if (percent >= 0.5) {

                    Log.d("Reveal Percentage", "onRevealPercentChangedListener: $percent")
                }
            }
        })
        return view
    }



    override fun onDestroyView() {
        super.onDestroyView()
    }

}