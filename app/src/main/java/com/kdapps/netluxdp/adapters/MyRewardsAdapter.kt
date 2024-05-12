package com.kdapps.netluxdp.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.kdapps.netluxdp.R
import com.kdapps.netluxdp.activities.MainActivity
import com.kdapps.netluxdp.activities.ScratchActivity
import com.kdapps.netluxdp.entities.RewardDetailsEntity
import com.kdapps.netluxdp.fragments.*
import kotlinx.android.synthetic.main.nxav_rewards_item_layout.view.*
import java.util.*
import kotlin.collections.ArrayList

class MyRewardsAdapter(val context: Context,val rewardList: List<RewardDataModel>): RecyclerView.Adapter<MyRewardsAdapter.viewHolder>() {


    class viewHolder(view: View): RecyclerView.ViewHolder(view){
        val tv_activation_key = view.tv_activation_key
        val scratchCardOverlay = view.scratch_card_overlay
        val tv_activation_status = view.tv_activation_status
        val tv_date = view.tv_scratched_date
        val tv_idx = view.tv_idx
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.nxav_rewards_item_layout, parent, false)
        return viewHolder(view)
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        val rewardKey = rewardList[position].rewardKey
        Log.e("rewardKey", rewardKey.toString())
        val month = rewardList[position].month
        val idX = rewardList[position].idX //IDX is saved in note field in ROOM database

        if(!rewardList[position].ifScratched!!){

            holder.tv_activation_key.setText("")
            holder.scratchCardOverlay.visibility = View.VISIBLE


            holder.itemView.setOnClickListener(){
                val intent = Intent(context, ScratchActivity::class.java)
                intent.putExtra("rewardKey", rewardKey)
                intent.putExtra("month", month)
                intent.putExtra("idX", idX)
                context.startActivity(intent)
            }

        }
        else{
            holder.tv_activation_key.setText(rewardKey)
            holder.scratchCardOverlay.visibility = View.INVISIBLE
            if(rewardList[position].res != "0"){
                holder.tv_activation_status.setText("Status: Used")
                holder.tv_activation_status.setBackgroundColor(Color.rgb(255,99,71))
            }
            else{
                holder.tv_activation_status.setText("Status: Unused")
                holder.tv_activation_status.setBackgroundColor(Color.GREEN)
            }
            //magenta or orange if key is unused.
            holder.tv_date.text = "Date:" + rewardList[position].whenScratched!!.substring(0,10)
            holder.tv_idx.text = "IDX:" + rewardList[position].idX//IDX is saved in note field in ROOM database
        }

    }

    override fun getItemCount(): Int {
        return rewardList.size
    }

}