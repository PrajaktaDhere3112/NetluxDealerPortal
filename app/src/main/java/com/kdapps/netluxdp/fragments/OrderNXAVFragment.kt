package com.kdapps.netluxdp.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.kdapps.netluxdp.R
import com.kdapps.netluxdp.activities.MainActivity
import com.kdapps.netluxdp.entities.DealerDetailsEntity
import com.kdapps.netluxdp.utils.ButtonAnalysis
import com.kdapps.netluxdp.utils.VolleySingleton
import com.kdapps.netluxdp.utils.showProgressDialog
import kotlinx.android.synthetic.main.fragment_order_n_x_a_v.*
import kotlinx.android.synthetic.main.fragment_order_n_x_a_v.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class OrderNXAVFragment : Fragment() {

    lateinit var sharedPreferences: SharedPreferences
    lateinit var viewTo: ConstraintLayout
    lateinit var mProgressDialog:Dialog
    var Str :String = "http://byteseq.com/temp/"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        (activity as MainActivity).supportActionBar?.title ="Order NXAV"
        val view = inflater.inflate(R.layout.fragment_order_n_x_a_v, container, false)
        viewTo = view.rootView.findViewById(R.id.order_nxav_constraint_layout)
        sharedPreferences = (activity as Context).getSharedPreferences("dealer_details", MODE_PRIVATE)
        val dlrId = sharedPreferences.getString("dlrId","")

        view.radioGroup.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener{radioGroup, i ->
            var selectedId = radioGroup.checkedRadioButtonId
            val radio = radioGroup.radioGroup
            if(selectedId == R.id.radio_btn_custom_qty){
                view.et_custom_quantity.visibility = View.VISIBLE
            }
        })

        view.btn_buy_now.setOnClickListener(){
            val checkedId = view.radioGroup.checkedRadioButtonId
            when(checkedId){
                R.id.radio_btn_10qty ->{
                    if (dlrId != null) {
                        orderConfirmed(it,dlrId, "10")
                    }

                }
                R.id.radio_btn_25qty ->{
                    if (dlrId != null) {
                        orderConfirmed(it,dlrId, "25")
                    }

                }
                R.id.radio_btn_50qty ->{
                    if (dlrId != null) {
                        orderConfirmed(it,dlrId, "50")
                    }

                }

                R.id.radio_btn_custom_qty ->{
                    val quantity = et_custom_quantity.text.toString()
                    if(quantity != ""){
                        if (dlrId != null) {
                            orderConfirmed(it,dlrId, quantity)
                        }
                    }
                    else{
                        showSnackBar("Please enter custom quantity to order")
                    }

                }
                else ->{
                    showSnackBar("Please select the quantity")

                }
            }
        }

        return view
    }

    private fun orderConfirmed(view:View, dlrId:String, quantity:String){
        mProgressDialog = Dialog(activity as Context)
        ButtonAnalysis(activity as Context).addButtonDetails(view.tag.toString(), "$quantity Quantity", "res2")
        showProgressDialog().showProgressDialog(mProgressDialog, "Placing Order...")
        GlobalScope.launch(Dispatchers.IO) {
            val url = Str + "neworder.php?DLRID=$dlrId&QTY=$quantity"
            val orderJsonArray = object: JsonArrayRequest(
                Request.Method.POST,
                url,
                null,
                Response.Listener {
                    if(it.length()> 0){
                        showProgressDialog().hideProgressDialog(mProgressDialog)
                        val dialog = AlertDialog.Builder(activity as Context)
                            .setTitle("Order Confirmed")
                            .setMessage("Thank you! for ordering $quantity NXAV product.Our Sales team will contact you soon.")
                            .setPositiveButton("OK") { text, listener ->

                            }
                            .create()
                            .show()
                    }
                },
                Response.ErrorListener {
                    showProgressDialog().hideProgressDialog(mProgressDialog)
                    Toast.makeText(activity as Context, "Please Retry",Toast.LENGTH_SHORT).show()
                    println("Error is $it")
                }
            ){}
            VolleySingleton(activity as Context).addToRequestQueue(orderJsonArray)
        }



    }

    private fun showSnackBar(text:String){
        Snackbar.make(
            viewTo,
            text,
            Snackbar.LENGTH_SHORT
        ).show()

    }
    override fun onDestroyView() {
        (activity as MainActivity).supportActionBar?.title =getString(R.string.app_name)
        super.onDestroyView()
    }

//    get dealer details from local database
//    sharedPreferences = (activity as Context).getSharedPreferences("dealer_details", MODE_PRIVATE)
//    val jsonDealerDetails = sharedPreferences.getString("jsonDealerDetails", "")
//    val dealerDetailsEntity = Gson().fromJson(jsonDealerDetails, DealerDetailsEntity::class.java)
//    Log.e("details", dealerDetailsEntity.toString())

}