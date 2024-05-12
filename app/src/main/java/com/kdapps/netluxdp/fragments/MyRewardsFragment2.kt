package com.kdapps.netluxdp.fragments


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.kdapps.netluxdp.R
import com.kdapps.netluxdp.activities.MainActivity
import com.kdapps.netluxdp.database.RewardDBAsyncTask
import com.kdapps.netluxdp.entities.RewardDetailsEntity
import com.kdapps.netluxdp.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_my_rewards.*
import kotlinx.android.synthetic.main.fragment_my_rewards.view.*
import kotlinx.coroutines.*
import java.lang.Boolean.TRUE
import java.text.SimpleDateFormat
import java.util.*
import java.util.stream.DoubleStream.concat


class MyRewardsFragment2 : Fragment() {



    lateinit var fromDate: String
    lateinit var sharedPreferences: SharedPreferences
    lateinit var mProgressDialog: Dialog
    lateinit var selectedMonth: String
    lateinit var currentMonth: String
    lateinit var startMonth: String
    lateinit var selectedView: View
    var Str :String = "http://byteseq.com/temp/"
    var count: Int = 0
    var flag: Boolean = false
    private val itemSizeArray = ArrayList<RewardDataModel>()
    private val itemSizeArrayForTextRefresh = ArrayList<RewardDataModel>()


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        // Inflate the layout for this fragment
        sharedPreferences =
            (activity as Context).getSharedPreferences("dealer_details", MODE_PRIVATE)
        (activity as MainActivity).supportActionBar?.title = "My Rewards"
        val view = inflater.inflate(R.layout.fragment_my_rewards, container, false)

        sharedPreferences.edit().putBoolean("new_feature_opened", true).apply()
        selectedView = view


        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val curDate = sdf.format(Date())
        val curYear = curDate.substring(6).toInt()
        val curMonth = curDate.substring(3, 5).toInt()
        fromDate = ProperDate().getProperDate(curYear, (curMonth - 1), 1)
        startMonth = ProperDate().getProperDate((curYear - 1), 5, 1)
        currentMonth = fromDate.substring(0, 7)
        selectedMonth = fromDate.substring(0, 7)



        mProgressDialog = Dialog(activity as Context)
        val checkPreviousData =
            sharedPreferences.getBoolean("PreviousRewardDetailsFetched", false)
/*        if(!checkPreviousData){
            GlobalScope.launch(Dispatchers.Main) {
                showProgressDialog().showProgressDialog(mProgressDialog, "Getting previous rewards details...")
                getPreviousRewardsOnline()
            }
        }
        else{
            getThisMonthDetails(selectedMonth, view)
        }*/

        GlobalScope.launch(Dispatchers.Main) {
            showProgressDialog().showProgressDialog(
                mProgressDialog,
                "Getting previous rewards details..."
            )
            getPreviousRewardsOnline()


        }

        view.btn_collected_rewards.setOnClickListener() {
            openFragment(EarnedRewardsFragment(), "collectedRewards", "Collected")
        }

        view.btn_used_rewards.setOnClickListener() {
            openFragment(EarnedRewardsFragment(), "usedRewards", "Used")
        }

        return view
    }


    private fun openFragment(fragment: Fragment, name: String, type: String) {
        val bundle = Bundle()
        bundle.putString("month", selectedMonth)
        bundle.putString("listType", type)
        fragment.arguments = bundle
        childFragmentManager.beginTransaction()
            .replace(R.id.rewards_frame_layout, fragment)
            .addToBackStack(name)
            .commit()

    }


    @SuppressLint("UseRequireInsteadOfGet")
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
                        val rewardKey = Encryption(activity as Context).decryptCBCRewardKey(jsonObject.getString("REWARDKEY"))
                        //Log.e("newRewardKey", rewardKey)
                        val ifScratchedString = jsonObject.getString("IFSCRATCHED")
                        var ifScratched = false
                        if (ifScratchedString == "TRUE") ifScratched = true
                        val whenScratched = jsonObject.getString("WHENSCRATCHED")
                        //key is used or not
                        val res = jsonObject.getString("RES")
                        count++
                        itemSizeArray.add(RewardDataModel(month, idX, rewardKey, ifScratched, whenScratched, res))
                        val collectedListCount=itemSizeArray.filter { it.res!!.startsWith("0") || it.ifScratched == false}.size
                        Log.e("collectedArray", collectedListCount.toString())
                        val usedListCount= itemSizeArray.filter{ it.ifScratched == TRUE && it.res!="0"}.size
                        Log.e("UsedArray", usedListCount.toString())
                        // If card is scratched we are filter it and get reward into used list
                        // if card is not scratched and res is start with zero then we get rewards into colllection list
                        //tv_collected_rewards_count.setText("No. of cards: ${collectedListCount.toString()}")
                        //tv_used_rewards_count.setText("No. of cards: ${usedListCount.toString()}")
                    }

                    sharedPreferences.edit().putBoolean("PreviousRewardDetailsFetched", true)
                        .apply()

                    showProgressDialog().hideProgressDialog(mProgressDialog)
                    getThisMonthDetails(selectedMonth, selectedView)
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



    @SuppressLint("UseRequireInsteadOfGet")
    private fun refreshText() {
        Log.e("processUpdate", "getting previous rewards online")
        val dlrId = sharedPreferences.getString("dlrId", "")
        val url =  Str + "getprevrewkeys.php?DLRID=$dlrId"
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
                        val rewardKey = Encryption(activity as Context).decryptCBCRewardKey(jsonObject.getString("REWARDKEY"))
                        //Log.e("newRewardKey", rewardKey)
                        val ifScratchedString = jsonObject.getString("IFSCRATCHED")
                        var ifScratched = false
                        if (ifScratchedString == "TRUE") ifScratched = true
                        val whenScratched = jsonObject.getString("WHENSCRATCHED")
                        //key is used or not
                        val res = jsonObject.getString("RES")
                        count++
                        itemSizeArrayForTextRefresh.add(RewardDataModel(month, idX, rewardKey, ifScratched, whenScratched, res))
                        val collectedListCount=itemSizeArrayForTextRefresh.filter { it.res!!.startsWith("0") || it.ifScratched == false}.size
                        Log.e("collectedArray", collectedListCount.toString())
                        val usedListCount= itemSizeArrayForTextRefresh.filter{ it.ifScratched == TRUE && it.res!="0"}.size
                        Log.e("UsedArray", usedListCount.toString())
                        // If card is scratched we are filter it and get reward into used list
                        // if card is not scratched and res is start with zero then we get rewards into colllection list
                        tv_collected_rewards_count.setText("No. of cards: ${collectedListCount.toString()}")
                        tv_used_rewards_count.setText("No. of cards: ${usedListCount.toString()}")
                    }

                    sharedPreferences.edit().putBoolean("PreviousRewardDetailsFetched", true)
                        .apply()

                    showProgressDialog().hideProgressDialog(mProgressDialog)
//                    getThisMonthDetails(selectedMonth, selectedView)
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



    /* fun getList(): ArrayList<RewardDataModel> {
         return itemSizeArray
     }*/



    private fun getThisMonthDetails(month: String, view: View) {
        //Log.e("processUpdate", "getThisMonthDetails")
        //-----view.loading_recycler_view.visibility = View.VISIBLE
        //-----view.previous_scratch_card_rv.visibility = View.INVISIBLE
        Log.e("processStatus", "getThisMonth func started")
        val count1 = getMonthActivationCount(activity as Context, month).execute().get()
        Log.e("TotalActivationFromMay", count1.toString())


        val reqForNewCard = count1.toInt() % 5

        when (reqForNewCard) {
            0 -> {
                view.tv_cur_month_count.setText(reqForNewCard.toString() + "\uD83E\uDD15")
                view.img_scratch_overlay.setImageResource(R.drawable.reward_0_star)
            }

            1 -> {
                view.tv_cur_month_count.setText(reqForNewCard.toString() + "\uD83D\uDE2C")
                view.img_scratch_overlay.setImageResource(R.drawable.reward_1_star)
            }
            2 -> {
                view.tv_cur_month_count.setText(reqForNewCard.toString() + "\uD83D\uDE03 ")
                view.img_scratch_overlay.setImageResource(R.drawable.reward_2_star)
            }
            3 -> {
                view.tv_cur_month_count.setText(reqForNewCard.toString() + "\uD83E\uDD29")
                view.img_scratch_overlay.setImageResource(R.drawable.reward_3_star)
            }
            4 -> {
                view.tv_cur_month_count.setText(reqForNewCard.toString() + "\uD83D\uDE0D")
                view.img_scratch_overlay.setImageResource(R.drawable.reward_4_star)
            }
        }
        //Log.e("cardDetails","reqForNewcard $reqForNewCard")
        val scratchCardWon = count1.toInt() / 5
        Log.e("scratchCardWon_listSize", "scratch card won:$scratchCardWon and previous Reward keys:${itemSizeArray.size}")

        if (itemSizeArray.size < scratchCardWon) {
            val scratchCardToAdd = scratchCardWon - itemSizeArray.size
            for (i in 1..scratchCardToAdd) {
                //Log.e("cardadded", i.toString())
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    //fetch activation key from the server
                    getRewardKey(month, view)
                    flag = true
                }
            }
        }
        Handler().postDelayed({refreshText()},2500)

/*        if(flag==true){
            GlobalScope.launch(Dispatchers.Main) {
                showProgressDialog().showProgressDialog(
                    mProgressDialog,
                    "Getting rewards details..."
                )
                getPreviousRewardsOnline()

            }
        }
*/
        Log.e("Status", "I have check if i have to fetch new key")
        Log.e("Status", "Now I have to wait for 2 sec")
        Log.e("Status", "2 sec over")

        //view.progress_scratch_card?.setProgress(reqForNewCard*20)
        view.tv_show_req_activations.setText("Complete ${5 - reqForNewCard} more activations to unlock the scratch card.")
        //---updateRewardsRecyclerView(month, view)
        /*og.e("ItemSizeArray", itemSizeArray.toString())
         val collectedListCount=itemSizeArray.filter { it.res!!.startsWith("0") || it.ifScratched == false}.size
         Log.e("collectedArray", collectedListCount.toString())
         val usedListCount= itemSizeArray.filter{ it.ifScratched == TRUE && it.res!="0"}.size
         Log.e("UsedArray", usedListCount.toString())
         // If card is scratched we are filter it and get reward into used list
         // if card is not scratched and res is start with zero then we get rewards into colllection list
         view.tv_collected_rewards_count.setText("No. of cards: ${collectedListCount.toString()}")
         view.tv_used_rewards_count.setText("No. of cards: ${usedListCount.toString()}")
         */

    }

    private fun getRewardKey(month: String, view: View) {
        //Log.e("rewardKey", "GetRewardKey function is called")
        val dlrId = sharedPreferences.getString("dlrId", "")
        val url =  Str + "getrewkey.php?DLRID=$dlrId&MONTH=$month"
        val jsonArrayRequest = object : JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            Response.Listener {
                try {
                    for (i in 0 until it.length()) {
                        //Log.e("Getting Key", "in progress")
                        val jsonObject = it.getJSONObject(i)
                        val rewardKey = Encryption(activity as Context).decryptCBCRewardKey(
                            jsonObject.getString("REWARDKEY")
                        )
                        //Log.e("rewardKey", rewardKey)
                        val IDX = jsonObject.getString("IDX")

                        view.tv_activation_key.setText(rewardKey)

                        val checkKey = MyRewardsFragment2.checkRewardKeyInRoom(activity as Context, rewardKey).execute().get()
                        val result = checkKey.size

                        val rewardDetailsEntity = dlrId?.let { RewardDetailsEntity(it, month, rewardKey, false, "", IDX, "0")
                        }
                        if (rewardDetailsEntity != null && result == 0) {
                            val result1 = RewardDBAsyncTask(activity as Context, rewardDetailsEntity, 1).execute().get()
                        }
                    }
                } catch (e: Exception) {
                    showDialog("Server Error", "Reward key will be fetched after some time")
                }
            },
            Response.ErrorListener {
                println("Error is getting reward key is $it")
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

    private fun showDialog(title: String, msg: String) {
        val dialog = AlertDialog.Builder(activity as Context)
            .setTitle(title)
            .setMessage(msg)
            .setPositiveButton("OK") { text, listener ->
            }
            .create()
            .show()
    }

    override fun onResume() {
        super.onResume()

        val firstTime = sharedPreferences.getBoolean("opened_first_time", true)
        Log.e("ScratchCard", "Resume function called and checkdum $firstTime")
        // if (!firstTime) {
        //getThisMonthDetails(selectedMonth, selectedView)
        // }
        //updateRewardsRecyclerView(selectedMonth, selectedView)
    }

    override fun onDestroyView() {
        (activity as MainActivity).supportActionBar?.title = getString(R.string.app_name)
        sharedPreferences.edit().putBoolean("opened_first_time", false).apply()
        super.onDestroyView()
    }
    class getMonthActivationCount(val context: Context, val month: String)
        : AsyncTask<Void, Void, Int>() {
        override fun doInBackground(vararg p0: Void?): Int {
            val db = RoomDatabaseBuilder(context).getRoomDatabase()
            val result1 = db.activationDetailsDao().getActivationCountFromFeb("2022-05-01")
            //val result = db.activationDetailsDao().getMonthActivationCount(month)
            db.close()
            return result1
        }
    }

    class checkRewardKeyInRoom(val context: Context, val rewadKey: String)
        :AsyncTask<Void,Void,List<RewardDetailsEntity>>(){
        override fun doInBackground(vararg p0: Void?): List<RewardDetailsEntity> {
            val db = RoomDatabaseBuilder(context).getRoomDatabase()
            val result = db.rewardDetailsDao().checkRewardKeyPresent(rewadKey)
            db.close()
            return result
        }

    }

}

data class RewardDataModel(
    val month: String?,
    val idX: String?,
    val rewardKey: String?,
    val ifScratched: Boolean?,
    val whenScratched:String?,
    val res:String?,
)
