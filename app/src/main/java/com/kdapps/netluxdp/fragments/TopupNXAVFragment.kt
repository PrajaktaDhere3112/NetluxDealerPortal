package com.kdapps.netluxdp.fragments


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.android.gms.common.api.internal.LifecycleCallback.getFragment
import com.google.android.material.snackbar.Snackbar
import com.kdapps.netluxdp.R
import com.kdapps.netluxdp.activities.MainActivity
import com.kdapps.netluxdp.utils.ButtonAnalysis
import com.kdapps.netluxdp.utils.VolleySingleton
import com.kdapps.netluxdp.utils.showProgressDialog
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_topup_n_x_a_v.*
import kotlinx.android.synthetic.main.fragment_topup_n_x_a_v.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONException



class TopupNXAVFragment : Fragment() {

    lateinit var viewTo: ConstraintLayout
    lateinit var mProgressDialog:Dialog
    var text:String?=null
    var checkContinueClicks:Boolean = false
    var Str :String = "http://www.netlux.in/"



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        (activity as MainActivity).supportActionBar?.title = "Topup"
        val view = inflater.inflate(R.layout.fragment_topup_n_x_a_v, container, false)
        viewTo = view.rootView.findViewById(R.id.topup_constraint_layout)


        val productTypes = resources.getStringArray(R.array.product_types)
        val arrayadapter = ArrayAdapter(
            activity as Context,
            R.layout.search_suggestion_item_layout, R.id.tv_search_suggestion, productTypes
        )
        view.product_type_spinner.adapter = arrayadapter

        view.btn_submit_profile.setOnClickListener {
            submit_details()

        }
            return view
    }



   /* private fun openFragment(fragment: Fragment,name:String) {
        val bundle = Bundle()
      //  bundle.putString("month", selectedMonth)
        fragment.arguments = bundle
        childFragmentManager.beginTransaction()
            .replace(R.id.topup_next, fragment)
            .addToBackStack(name)
            .commit()

    }*/
    fun submit_details(){
        val check = validateDetails()
        if (check) {
            mProgressDialog = Dialog(activity as Context)
            showProgressDialog().showProgressDialog(mProgressDialog, "Validating Data...")
            val pkey = et_product_key.text.toString()
            val ikey = et_installation_key.text.toString()
            val cuName = et_customer_name.text.toString()
            val cuMobile = et_customer_mobile_number.text.toString()
            val cuEmail = et_customer_email.text.toString()
            val dealerCode = et_dealer_code.text.toString()
            val productType = product_type_spinner.selectedItem.toString().substring(0, 4)
            val dealerMobile = et_dealer_mobile_number.text.toString()
            val enggCode = et_engineer_code.text.toString()
            val pincode = et_dealer_mobile_number.text.toString()

            GlobalScope.launch(Dispatchers.IO) {
                val url =  "http://www.netlux.in/infoup.php?pkey=$pkey&ikey=$ikey&cuname=$cuName&cuemail=$cuEmail&cuno=$cuMobile&deno=$dealerMobile&decode=$dealerCode&enggcode=$enggCode&dist=$pincode&protype=$productType"
                Log.e("url", url)
                val postTopupDetails = object : StringRequest(
                    Method.POST,
                    url,
                    Response.Listener {
                        try {
                            val name = it
                            if (name == "SUBMITTED") {
                                showProgressDialog().hideProgressDialog(mProgressDialog)
                                checkTopupCompleted(pkey, ikey)
                            } else {
                                submit_details()
                                showProgressDialog().hideProgressDialog(mProgressDialog)
                                showDialogBox(
                                    "Details Error",
                                    "Please Enter Correct details."
                                )

                            }

                        } catch (e: JSONException) {
                            e.printStackTrace()
                            showProgressDialog().hideProgressDialog(mProgressDialog)
                            Toast.makeText(
                                activity as Context,
                                "something went wrong",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    Response.ErrorListener {
                        println("Error is $it")
                        showProgressDialog().hideProgressDialog(mProgressDialog)
                        Toast.makeText(
                            activity as Context,
                            "Something went wrong",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                ) {}
                VolleySingleton(activity as Context).addToRequestQueue(postTopupDetails)
            }
        }
    }



    private fun validateDetails():Boolean{
        return when {
            TextUtils.isEmpty(et_product_key.text.toString().trim() { it <= ' ' }) -> {
                showSnackBar("Please Enter Product Key")
                false
            }

            TextUtils.isEmpty(et_installation_key.text.toString().trim() { it <= ' ' }) -> {
                showSnackBar("Please Enter Activation Key")
                false
            }

            TextUtils.isEmpty(et_customer_name.text.toString().trim() { it <= ' ' }) -> {
                showSnackBar("Please Enter Customer Name")
                false
            }

            TextUtils.isEmpty(et_customer_mobile_number.text.toString().trim() { it <= ' ' }) -> {
                showSnackBar("Please Enter Customer mobile number")
                false
            }

            else -> {
                //showErrorSnackBar("You are registered user!!!",false)
                true
            }
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

    @SuppressLint("UseRequireInsteadOfGet", "ResourceType")
    private fun checkTopupCompleted(pkey:String, ikey:String){
        mProgressDialog = Dialog(activity as Context)
        showProgressDialog().showProgressDialog(mProgressDialog,"Activation in progess...")
        GlobalScope.launch(Dispatchers.IO) {

            delay(1500)
            val url =  "http://www.netlux.in/infoout.php?&paraURL=101&pkey=$pkey&ikey=$ikey"
            val getTopupResponse = @SuppressLint("UseRequireInsteadOfGet")
            object :StringRequest(
                Method.GET,
                url,
                Response.Listener {
                    Log.e("responseTopup", it)
                    showProgressDialog().hideProgressDialog(mProgressDialog)
                    Log.e("responseTopupLength", it.length.toString())
                    if(it.length != 6){
                        val responseList = it.split("#")
                        Log.e("responseTopupResult", responseList[5])
                        if(responseList[4].length == 0) {
                            submit_details()
                        }
                     else  if(responseList[4] != "NO_AK"){
                            val newActivationKey = responseList[4]
                            Log.e("newActivationKey", newActivationKey)
                            showDialogBox("Topup","Your Topup is succusseful!!!.Your activation key is $newActivationKey")
                            val dialog = AlertDialog.Builder(activity as Context)
                                .setTitle("Topup")
                                .setMessage("Your Topup is succusseful!!!.Your activation key is $newActivationKey")
                                .setPositiveButton("OK") { text, listener ->
                                    tv_after_successful_activation.setText("Dear Customer,\n Your activation key is \n $newActivationKey.\nThank you for choosing Netlux Security for Digital India")
                                }
                                .create()
                                .show()


                        }else{
                            showDialogBox("Topup", responseList[5])
                        }


                    }else{
                        val msg = "Please check all the details and try again."
                        showDialogBox("Topup", msg)
                    }

                },
                Response.ErrorListener {
                    val msg ="SERVER SEEMS TO BE BUSY! KINDLY CONTACT CUSTOMER CARE NUMBER 7843000437. AND GET ACTIVATION KEY IN SECONDS."
                    showDialogBox("Server Busy", msg)
                    Log.e("TopupError", it.toString())
                }
            ){}
            getTopupResponse.setRetryPolicy( DefaultRetryPolicy(5000, 4, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT))
            VolleySingleton(activity as Context).addToRequestQueue(getTopupResponse)

        }


    }





    private fun showDialogBox(title:String, msg:String){
        val dialog = AlertDialog.Builder(activity as Context)
            .setTitle(title)
            .setMessage(msg)
            .setPositiveButton("OK") { text, listener ->

            }
            .create()
            .show()
    }


    override fun onDestroyView() {
        (activity as MainActivity).supportActionBar?.title =getString(R.string.app_name)
        super.onDestroyView()
    }

}