package com.kdapps.netluxdp.fragments

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.kdapps.netluxdp.R
import com.kdapps.netluxdp.activities.MainActivity
import com.kdapps.netluxdp.adapters.MyActivationAdapter
import com.kdapps.netluxdp.entities.ActivationDetailsEntity
import com.kdapps.netluxdp.utils.RoomDatabaseBuilder
import kotlinx.android.synthetic.main.fragment_customer_search.*
import kotlinx.android.synthetic.main.fragment_customer_search.view.*
import kotlinx.android.synthetic.main.fragment_my_activations.view.*
import kotlinx.android.synthetic.main.fragment_topup_n_x_a_v.*

class CustomerSearchFragment : Fragment() {

    lateinit var viewTo:ConstraintLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        (activity as MainActivity).supportActionBar?.title ="Customer Search"
        val view = inflater.inflate(R.layout.fragment_customer_search, container, false)
        viewTo = view.rootView.findViewById(R.id.search_constraint_layout)

        val namesList = getAllCustomerNames(activity as Context).execute().get()
        val namesAdapter = ArrayAdapter<String>(activity as Context,R.layout.search_suggestion_item_layout,R.id.tv_search_suggestion, namesList)

        view.customer_name.threshold = 1
        view.customer_name.setAdapter(namesAdapter)


        view.btn_search_customer.setOnClickListener(){
            val checkDetails = validateDetails()
            if(checkDetails){
                view.tv_search_results.visibility = View.INVISIBLE
                view.tv_no_customer.visibility = View.INVISIBLE
                val customerList: List<ActivationDetailsEntity>
                if(view.customer_name.text.toString() != ""){
                    val name = view.customer_name.text.toString()
                    customerList = getCustomerDetailsName(activity as Context, name).execute().get()
                    view.tv_search_results.setText("Search results for: $name")
                    if(customerList.isEmpty()){
                        view.tv_no_customer.visibility = View.VISIBLE
                    }
                    view.customer_name.setText("")

                }
                else{
                    val number = view.customer_mobile_number.text.toString()
                    customerList = getCustomerDetailsFromNumber(activity as Context, number).execute().get()
                    view.tv_search_results.setText("Search results for: $number")

                    if(customerList.isEmpty()){
                        view.tv_no_customer.visibility = View.VISIBLE
                    }
                    view.customer_mobile_number.setText("")

                }
                view.tv_search_results.visibility = View.VISIBLE
                view.customer_search_recycler_view.apply {
                    adapter = MyActivationAdapter(activity as Context, customerList)
                    layoutManager = LinearLayoutManager(activity)
                }
            }

        }

        return view
    }

    private fun validateDetails():Boolean{
        return when {
            TextUtils.isEmpty(customer_name.text.toString().trim() { it <= ' ' }) &&
                    TextUtils.isEmpty(customer_mobile_number.text.toString().trim() { it <= ' ' }) -> {
                showSnackBar("Please Enter Customer Name OR Mobile number")
                false
            }

            else -> {
                //showErrorSnackBar("You are registered user!!!",false)
                true
            }
        }
    }

    class getAllCustomerNames(val context: Context)
        : AsyncTask<Void, Void, List<String>>() {
        override fun doInBackground(vararg p0: Void?): List<String> {
            val db = RoomDatabaseBuilder(context).getRoomDatabase()
            val result = db.activationDetailsDao().getAllCustomerNames()
            db.close()
            return result
        }
    }

    class getCustomerDetailsName(val context: Context,val name:String)
        :AsyncTask<Void, Void,List<ActivationDetailsEntity>>(){
        override fun doInBackground(vararg p0: Void?): List<ActivationDetailsEntity> {
            val db = RoomDatabaseBuilder(context).getRoomDatabase()
            val wildcardName = "%$name%"
            val result = db.activationDetailsDao().getCustomerDetailsFromName(wildcardName)
            db.close()
            return result
        }

    }
    class getCustomerDetailsFromNumber(val context: Context,val number:String)
        :AsyncTask<Void, Void,List<ActivationDetailsEntity>>(){
        override fun doInBackground(vararg p0: Void?): List<ActivationDetailsEntity> {
            val db = RoomDatabaseBuilder(context).getRoomDatabase()
            val result = db.activationDetailsDao().getCustomerDetailsFromNumber(number)
            db.close()
            return result
        }

    }

    private fun showSnackBar(msg: String){
        Snackbar.make(
            viewTo,
            msg,
            Snackbar.LENGTH_SHORT
        )
            .setBackgroundTint(getResources().getColor(R.color.warning_color))
            .show()

    }


    override fun onDestroyView() {
        (activity as MainActivity).supportActionBar?.title =getString(R.string.app_name)
        super.onDestroyView()
    }

}