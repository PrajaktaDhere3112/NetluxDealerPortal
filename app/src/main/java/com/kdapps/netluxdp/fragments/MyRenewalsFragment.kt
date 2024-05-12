package com.kdapps.netluxdp.fragments

import android.app.DatePickerDialog
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.google.android.material.snackbar.Snackbar
import com.kdapps.netluxdp.R
import com.kdapps.netluxdp.activities.MainActivity
import com.kdapps.netluxdp.adapters.MyActivationAdapter
import com.kdapps.netluxdp.adapters.MyRenewalsAdapter
import com.kdapps.netluxdp.database.NXAVRoomDatabase
import com.kdapps.netluxdp.entities.ActivationDetailsEntity
import com.kdapps.netluxdp.utils.ButtonAnalysis
import com.kdapps.netluxdp.utils.RoomDatabaseBuilder
import kotlinx.android.synthetic.main.fragment_my_activations.*
import kotlinx.android.synthetic.main.fragment_my_activations.view.*
import kotlinx.android.synthetic.main.fragment_my_renewals.*
import kotlinx.android.synthetic.main.fragment_my_renewals.view.*
import java.text.SimpleDateFormat
import java.util.*


class MyRenewalsFragment : Fragment() {

    lateinit var viewTo:ConstraintLayout
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        (activity as MainActivity).supportActionBar?.title ="My Renewals"
        val view =  inflater.inflate(R.layout.fragment_my_renewals, container, false)

        viewTo = view.rootView.findViewById(R.id.renewal_constraint_layout)

        var timeInMill = Calendar.getInstance().timeInMillis


        var toDate:String? = null
        var fromDate:String? = null
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val curDate = sdf.format(Date())
        val curYear = curDate.substring(6).toInt()
        val curMonth = curDate.substring(3,5).toInt()
        val curDayofMonth = curDate.substring(0,2).toInt()

        when(curMonth){
            0 ->{
                fromDate = getProperDate((curYear-1),8, curDayofMonth)
            }
            1 ->{
                fromDate = getProperDate((curYear-1),9, curDayofMonth)
            }
            2 ->{
                fromDate = getProperDate((curYear-1),10, curDayofMonth)
            }
            else ->{
                fromDate = getProperDate(curYear,(curMonth-4), curDayofMonth)
            }
        }

        view.tv_from_date_renewal.setText(fromDate)
        toDate = getProperDate(curYear,(curMonth-1), curDayofMonth)
        view.tv_to_date_renewal.setText(toDate)
        val expiredList = getExpiredActivation(activity as Context, fromDate, toDate).execute().get()
        if(!expiredList.isEmpty()){
            view.tv_no_expired_activation.visibility = View.INVISIBLE
        }
        view.renewal_recycler_view.apply {
            adapter = MyRenewalsAdapter(activity as Context, expiredList)
            layoutManager = LinearLayoutManager(activity)
        }


        //Creating DatePickerDialog to get FROM Date from user
        view.tv_from_date_renewal.setOnClickListener(){
            val cal = Calendar.getInstance()
            val y = cal.get(Calendar.YEAR)
            val m = cal.get(Calendar.MONTH)
            val d = cal.get(Calendar.DAY_OF_MONTH)
            val datepickerdialog = DatePickerDialog(activity as Context, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                val calendar = Calendar.getInstance()
                calendar.set(year,monthOfYear, dayOfMonth,9,30,0)
                timeInMill = calendar.timeInMillis
                Log.e("minselected", timeInMill.toString())
                // Display Selected date in textbox
                //fromDate = "" + dayOfMonth + "/" + monthOfYear + "/" + year
                fromDate = getProperDate(year, monthOfYear, dayOfMonth)
                ButtonAnalysis(activity as Context).addButtonDetails(it.tag.toString(), fromDate.toString(), "res2" )

                //fromDate ="" + year + "-" + month + "-" + day
                tv_from_date_renewal.setText(fromDate)
            }, y, m, d)

            datepickerdialog.datePicker.maxDate = cal.timeInMillis
            datepickerdialog.show()

        }

        //Creating DatePickerDialog to get TO Date from user
        view.tv_to_date_renewal.setOnClickListener(){
            val cal = Calendar.getInstance()
            val y = cal.get(Calendar.YEAR)
            val m = cal.get(Calendar.MONTH)
            val d = cal.get(Calendar.DAY_OF_MONTH)
            val datepickerdialog = DatePickerDialog(activity as Context, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                // Display Selected date in textbox
                toDate = getProperDate(year, monthOfYear, dayOfMonth)

                tv_to_date_renewal.setText(toDate)
                ButtonAnalysis(activity as Context).addButtonDetails(it.tag.toString(), toDate.toString(), "res2" )
            }, y, m, d)
            datepickerdialog.datePicker.minDate = timeInMill
//            datepickerdialog.datePicker.maxDate = cal.timeInMillis
            Log.e("mindate", cal.time.toString())

            datepickerdialog.show()
        }

        view.btn_show_renewal.setOnClickListener(){
            ButtonAnalysis(activity as Context).addButtonDetails(it.tag.toString(), "res1", "res2" )
            if(toDate == null || fromDate == null){
                showSnackBar()
            }
            else{

                val activationList = getExpiredActivation(
                    activity as Context,
                    fromDate!!,
                    toDate!!
                ).execute().get()
                Log.e("list",activationList.toString())
                if(activationList.isEmpty()){
                    view.tv_no_expired_activation.visibility = View.VISIBLE
                }
                else{
                    view.tv_no_expired_activation.visibility = View.INVISIBLE
                }
                view.renewal_recycler_view.apply {
                    adapter = MyRenewalsAdapter(activity as Context, activationList)
                    layoutManager = LinearLayoutManager(activity)
                }

            }


        }


        return view
    }

    class getExpiredActivation(val context: Context, val fromDate:String, val toDate:String)
        :AsyncTask<Void, Void, List<ActivationDetailsEntity>>(){
        override fun doInBackground(vararg p0: Void?): List<ActivationDetailsEntity> {
            val db = RoomDatabaseBuilder(context).getRoomDatabase()
            val result =db.activationDetailsDao().getExpiredActivationDetails(fromDate, toDate)
            db.close()
            return result
        }

    }
    private fun getProperDate(year:Int, monthOfYear: Int, dayOfMonth:Int):String{
        var month:String
        var day:String = dayOfMonth.toString()
        var date:String
        if(monthOfYear < 9){
            month = ("0" +(monthOfYear + 1))
            Log.e("monthLAtes", month)
            if(dayOfMonth < 10){
                day = ("0" +dayOfMonth)
                Log.e("dateLaates", day)
                date ="" + year + "-" + month + "-" + day
            }
            else{
                date ="" + year + "-" + month + "-" + dayOfMonth
            }
            return date
        }
        else{
            month =( monthOfYear + 1).toString()
            if(dayOfMonth < 10){
                day = ("0" +dayOfMonth)
                Log.e("dateLaates", day)
                date ="" + year + "-" + month + "-" + day
            }
            else{
                date ="" + year + "-" + month + "-" + dayOfMonth
            }
            return date
        }
    }

    private fun showSnackBar(){
        Snackbar.make(
            viewTo,
            "Please select both the dates",
            Snackbar.LENGTH_SHORT
        ).show()

    }
    override fun onDestroyView() {
        (activity as MainActivity).supportActionBar?.title =getString(R.string.app_name)
        super.onDestroyView()
    }
}