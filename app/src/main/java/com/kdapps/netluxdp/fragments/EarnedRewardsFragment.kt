package com.kdapps.netluxdp.fragments
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.kdapps.netluxdp.R
import com.kdapps.netluxdp.activities.MainActivity
import com.kdapps.netluxdp.adapters.MyRewardsAdapter
import com.kdapps.netluxdp.utils.Encryption
import com.kdapps.netluxdp.utils.ProperDate
import com.kdapps.netluxdp.utils.VolleySingleton
import com.kdapps.netluxdp.utils.showProgressDialog
import kotlinx.android.synthetic.main.fragment_collected_rewards.view.*
import kotlinx.android.synthetic.main.fragment_earned_rewards.view.*
import kotlinx.android.synthetic.main.fragment_earned_rewards.view.loading_recycler_view
import kotlinx.android.synthetic.main.fragment_earned_rewards.view.tv_no_rewards_notify
import kotlinx.android.synthetic.main.nxav_rewards_item_layout.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Boolean.TRUE
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class EarnedRewardsFragment : Fragment() {

    lateinit var selectedView: View
    lateinit var selectedMonth: String
    lateinit var selectedListType: String
    lateinit var currentMonth: String
    lateinit var sharedPreferences: SharedPreferences
    lateinit var fromDate: String
    lateinit var startMonth: String
    var count: Int = 0
    var Str :String = "http://byteseq.com/temp/"
    lateinit var mProgressDialog: Dialog
    val itemSizeArray= ArrayList<RewardDataModel>()
    var updatedRewardList = ArrayList<RewardDataModel>()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_collected_rewards, container, false)
        selectedView = view
        val month = arguments?.getString("month")
        val listType = arguments?.getString("listType")
        selectedMonth = month.toString()
        selectedListType = listType.toString()

        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val curDate = sdf.format(Date())
        val curYear = curDate.substring(6).toInt()
        val curMonth = curDate.substring(3, 5).toInt()
        fromDate = ProperDate().getProperDate(curYear, (curMonth - 1), 1)
        startMonth = ProperDate().getProperDate((curYear - 1), 5, 1)
        currentMonth = fromDate.substring(0, 7)
        Log.e("transferedMonth", month.toString())


        sharedPreferences = (activity as Context).getSharedPreferences("dealer_details", Context.MODE_PRIVATE)
        (activity as MainActivity).supportActionBar?.title = "My Rewards"
        sharedPreferences.edit().putBoolean("new_feature_opened", true).apply()
        mProgressDialog = Dialog(activity as Context)

        GlobalScope.launch(Dispatchers.Main) {
            getPreviousRewardsOnline()
        }

        if (month != null && listType != null) {
            updateRewardsRecyclerView(month, selectedView, listType)
        }
        return view
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
                        val rewardKey =
                            Encryption(activity as Context).decryptCBCRewardKey(
                                jsonObject.getString(
                                    "REWARDKEY"
                                )
                            )
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
                    showProgressDialog().hideProgressDialog(mProgressDialog)
                    val dialog = AlertDialog.Builder(activity)
                        .setTitle("Dear user")
                        .setMessage("Please try after some time.")
                        .setPositiveButton("OK") { text, listener ->
                            activity?.finish()
                        }
                        .create()
                        .show()
                }
            ) {}
        VolleySingleton(activity as Context).addToRequestQueue(jsonArrayRequest)
    }

    fun updateRewardsRecyclerView(month: String, view: View = selectedView, type:String){

        viewLifecycleOwner.lifecycleScope.launch {
            view.loading_recycler_view.visibility = View.VISIBLE
            view.rv_collected_rewards.visibility = View.INVISIBLE
            delay(1750)
            view.loading_recycler_view.visibility = View.INVISIBLE
            view.rv_collected_rewards.visibility = View.VISIBLE



            //val sortedList = updatedRewardList.sortedWith(compareBy({it.ifScratched},{it.whenScratched}))
            Log.e("ItemSizeArray", itemSizeArray.toString())
            val collectedList=itemSizeArray.filter { it.res!!.startsWith("0") || it.ifScratched == false} as ArrayList<RewardDataModel>
            Log.e("collectedArray", collectedList.toString())
            val usedList= itemSizeArray.filter{ it.ifScratched == TRUE && it.res!="0"} as ArrayList<RewardDataModel>
            Log.e("UsedArray", usedList.toString())


            when (type) {

                "Collected" -> {
                    view.rv_collected_rewards.apply {
                        adapter = MyRewardsAdapter(activity as Context, collectedList)
                        layoutManager = LinearLayoutManager(activity)
                    }
                }

                "Used" -> {
                    view.rv_collected_rewards.apply {
                        adapter = MyRewardsAdapter(activity as Context, usedList)
                        layoutManager = LinearLayoutManager(activity)
                    }
                }

            }
//            view.rv_earned_rewards.apply {
//                adapter = MyRewardsAdapter(activity as Context, earnedList)
//                layoutManager = LinearLayoutManager(activity)
//            }
            if (updatedRewardList.size == 0) {
                view.tv_no_rewards_notify.setText("No Reward keys collected.")
            } else {
                view.tv_no_rewards_notify.setText("")
            }
        }

    }

    override fun onResume() {
        super.onResume()
        updateRewardsRecyclerView(selectedMonth, selectedView, selectedListType)
    }
}


