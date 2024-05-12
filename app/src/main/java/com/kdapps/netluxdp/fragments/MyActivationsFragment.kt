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
import com.google.android.material.snackbar.Snackbar
import com.kdapps.netluxdp.R
import com.kdapps.netluxdp.activities.MainActivity
import com.kdapps.netluxdp.adapters.MyActivationAdapter
import com.kdapps.netluxdp.entities.ActivationDetailsEntity
import com.kdapps.netluxdp.utils.ButtonAnalysis
import com.kdapps.netluxdp.utils.ProperDate
import com.kdapps.netluxdp.utils.RoomDatabaseBuilder
import kotlinx.android.synthetic.main.fragment_my_activations.*
import kotlinx.android.synthetic.main.fragment_my_activations.view.*
import java.text.SimpleDateFormat
import java.util.*


class MyActivationsFragment : Fragment() {


    lateinit var viewTo:ConstraintLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        (activity as MainActivity).supportActionBar?.title ="My Activations"
        val view = inflater.inflate(R.layout.fragment_my_activations, container, false)

        viewTo = view.rootView.findViewById(R.id.activation_constraint)

        var timeInMill = Calendar.getInstance().timeInMillis

        val totalActivationCount = getActivationCount(activity as Context).execute().get()

        var toDate:String? = null
        var fromDate:String? = null
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val adf = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
        val curDate = sdf.format(Date())
        Log.e("curDate", curDate)
        val curYear = curDate.substring(6).toInt()
        val curMonth = curDate.substring(3,5).toInt()
        val curDayofMonth = curDate.substring(0,2).toInt()

        when(curMonth){
            0 ->{
                fromDate = ProperDate().getProperDate((curYear-1),8, curDayofMonth)
            }
            1 ->{
                fromDate = ProperDate().getProperDate((curYear-1),9, curDayofMonth)
            }
            2 ->{
                fromDate = ProperDate().getProperDate((curYear-1),10, curDayofMonth)
            }
            3 ->{
                fromDate = ProperDate().getProperDate((curYear-1),11, curDayofMonth)
            }

            else ->{
                fromDate = ProperDate().getProperDate(curYear,(curMonth-4), curDayofMonth)
            }
        }

        view.tv_from_date.setText(fromDate)
        toDate = ProperDate().getProperDate(curYear,(curMonth-1), curDayofMonth)
        view.tv_to_date.setText(toDate)
        val activationList = getActivations(activity as Context,fromDate, toDate).execute().get()
        Log.e("activeList", activationList.toString())
        val currentListCount = activationList.size
        view.tv_total_activation_count.setText(currentListCount.toString())
        view.activation_recycler_view.apply {
            adapter = MyActivationAdapter(activity as Context, activationList)
            layoutManager = LinearLayoutManager(activity)
        }


        //Creating DatePickerDialog to get FROM Date from user
        view.tv_from_date.setOnClickListener(){
            val cal = Calendar.getInstance()
            val y = cal.get(Calendar.YEAR)
            val m = cal.get(Calendar.MONTH)
            val d = cal.get(Calendar.DAY_OF_MONTH)
            val datepickerdialog = DatePickerDialog(activity as Context, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                val calendar = Calendar.getInstance()
                calendar.set(year,monthOfYear, dayOfMonth,9,30,0)
                timeInMill = calendar.timeInMillis
                //Log.e("minselected", timeInMill.toString())
                // Display Selected date in textbox
                //fromDate = "" + dayOfMonth + "/" + monthOfYear + "/" + year
                fromDate = ProperDate().getProperDate(year, monthOfYear, dayOfMonth)


                //fromDate ="" + year + "-" + month + "-" + day
                tv_from_date.setText(fromDate)
                ButtonAnalysis(activity as Context).addButtonDetails(it.tag.toString(), fromDate.toString(), "res2" )
            }, y, m, d)

            datepickerdialog.datePicker.maxDate = cal.timeInMillis
            datepickerdialog.show()

        }

        //Creating DatePickerDialog to get TO Date from user
        view.tv_to_date.setOnClickListener(){
            val cal = Calendar.getInstance()
            val y = cal.get(Calendar.YEAR)
            val m = cal.get(Calendar.MONTH)
            val d = cal.get(Calendar.DAY_OF_MONTH)
            val datepickerdialog = DatePickerDialog(activity as Context, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                // Display Selected date in textbox
                toDate = ProperDate().getProperDate(year, monthOfYear, dayOfMonth)

                tv_to_date.setText(toDate)
                ButtonAnalysis(activity as Context).addButtonDetails(it.tag.toString(), toDate.toString(), "res2" )
            }, y, m, d)
            datepickerdialog.datePicker.minDate = timeInMill
            datepickerdialog.datePicker.maxDate = cal.timeInMillis
            Log.e("mindate", cal.time.toString())

            datepickerdialog.show()
        }

        view.btn_show_activations.setOnClickListener(){
            ButtonAnalysis(activity as Context).addButtonDetails(it.tag.toString(), "res1", "res2" )

            if(toDate == null || fromDate == null){
                showSnackBar()
            }
            else{

                val activationList = getActivations(activity as Context, fromDate!!, toDate!!).execute().get()
                Log.e("list",activationList.toString())
                if(activationList.isEmpty()){
                    view.tv_no_activation.visibility = View.VISIBLE
                }
                else{
                    view.tv_no_activation.visibility = View.INVISIBLE
                }
                val currentListCount = activationList.size
                view.tv_total_activation_count.setText(currentListCount.toString())
                view.activation_recycler_view.apply {
                    adapter = MyActivationAdapter(activity as Context, activationList)
                    layoutManager = LinearLayoutManager(activity)
                }

            }


        }

        return view
    }


    //Creating class to get Customer Activation Details from Local Room Database
    class getActivations(val context: Context,val fromDate: String, val toDate: String)
        :AsyncTask<Void, Void, List<ActivationDetailsEntity>>(){
        override fun doInBackground(vararg p0: Void?): List<ActivationDetailsEntity> {
            val db = RoomDatabaseBuilder(context).getRoomDatabase()
            val result = db.activationDetailsDao().getSelectedActivationDetails(fromDate, toDate)
            db.close()
            return result
        }

    }

    class getActivationCount(val context: Context)
        :AsyncTask<Void, Void, Int>() {
        override fun doInBackground(vararg p0: Void?): Int {
            val db = RoomDatabaseBuilder(context).getRoomDatabase()
            val result = db.activationDetailsDao().getTotalCount()
            db.close()
            return result
        }
    }

    class getAllActivations(val context: Context)
        :AsyncTask<Void, Void, List<ActivationDetailsEntity>>(){
        override fun doInBackground(vararg p0: Void?): List<ActivationDetailsEntity> {
            val db = RoomDatabaseBuilder(context).getRoomDatabase()
            val result = db.activationDetailsDao().getAllActivationDetails()
            db.close()
            return result
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