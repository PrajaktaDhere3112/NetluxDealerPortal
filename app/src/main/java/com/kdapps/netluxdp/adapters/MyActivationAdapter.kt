package com.kdapps.netluxdp.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.Layout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.kdapps.netluxdp.R
import com.kdapps.netluxdp.activities.ShowActivationDetailsActivtiy
import com.kdapps.netluxdp.entities.ActivationDetailsEntity
import com.kdapps.netluxdp.utils.ButtonAnalysis
import com.kdapps.netluxdp.utils.ProperDate
import kotlinx.android.synthetic.main.activation_list_item_layout.view.*
import java.text.SimpleDateFormat
import java.util.*

class MyActivationAdapter(val context:Context, val ActivationList: List<ActivationDetailsEntity>)
    :RecyclerView.Adapter<MyActivationAdapter.viewHolder>(){

    class viewHolder(view: View): RecyclerView.ViewHolder(view){

        val tv_name = view.tv_activation_customer_name
        val tv_pKey = view.tv_activation_customer_a_date
        val tv_active_state = view.tv_active_state

    }
    //setting differCallBack to avoid the reloading of whole recycler view
    private val differCallBack = object: DiffUtil.ItemCallback<ActivationDetailsEntity>(){
        override fun areItemsTheSame(oldItem: ActivationDetailsEntity, newItem: ActivationDetailsEntity): Boolean {
            return oldItem.cupkey == newItem.cupkey
        }

        override fun areContentsTheSame(oldItem: ActivationDetailsEntity, newItem: ActivationDetailsEntity): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallBack)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activation_list_item_layout, parent, false)
        return viewHolder(view)
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        val name = ActivationList[position].cuName
        val mob_no = ActivationList[position].cuNumber
        val aDate = ActivationList[position].aDate.substring(0,10)
        val aTime = ActivationList[position].aDate.substring(11,16)
        val expDate = ActivationList[position].expDate.substring(0,10)

        val properTime = ProperDate().getProperTime(aTime)
        //setting all the text views and activation state
        holder.tv_name.setText("Name: $name")
        holder.tv_pKey.setText("Date: $aDate($properTime)")
        holder.tv_active_state.setText("Active")
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val curDate = simpleDateFormat.format(Date())
        if(curDate <= expDate ){
            holder.tv_active_state.setBackgroundColor(Color.GREEN)
        }
        else{
            holder.tv_active_state.setBackgroundColor(Color.RED)
            holder.tv_active_state.setText("Expired")
        }
        //setting up onClick event on each item of the recycler view
        holder.itemView.setOnClickListener(){
            it.tag = "item_in_activation_list"
            ButtonAnalysis(context).addButtonDetails(it.tag.toString(), "res1", "res2")
            val intent = Intent(context, ShowActivationDetailsActivtiy::class.java)
            //passing product key to the next activity using intent
            intent.putExtra("cupKey", ActivationList[position].cupkey)
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return ActivationList.size
    }
}