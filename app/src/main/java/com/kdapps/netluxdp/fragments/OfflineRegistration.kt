package com.kdapps.netluxdp.fragments

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.kdapps.netluxdp.R
import com.kdapps.netluxdp.activities.MainActivity
import com.kdapps.netluxdp.utils.ButtonAnalysis
import kotlinx.android.synthetic.main.fragment_marketing_helpline.view.*


class OfflineRegistration : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.offlineregistration, container, false)
        val number1 =getString( R.string.marketing_helpline_1).trim()
        val number2 =getString( R.string.marketing_helpline_2).trim()
        val number3 =getString( R.string.marketing_helpline_3).trim()

        view.call_marketing_1.setOnClickListener(){
            ButtonAnalysis(activity as Context).addButtonDetails(it.tag.toString(), number1, "res2")
            openPhoneDialer(number1)
        }
        view.call_marketing_2.setOnClickListener(){
            ButtonAnalysis(activity as Context).addButtonDetails(it.tag.toString(), number2, "res2")
            openPhoneDialer(number2)
        }
        view.call_marketing_3.setOnClickListener(){
            ButtonAnalysis(activity as Context).addButtonDetails(it.tag.toString(), number3, "res2")
            openPhoneDialer(number3)
        }

        view.whatsapp_marketing_1.setOnClickListener(){
            ButtonAnalysis(activity as Context).addButtonDetails(it.tag.toString(), number1, "res2")
            openWhatsapp(number1)
        }
        view.whatsapp_marketing_2.setOnClickListener(){
            ButtonAnalysis(activity as Context).addButtonDetails(it.tag.toString(), number2, "res2")
            openWhatsapp(number2)
        }
        view.whatsapp_marketing_3.setOnClickListener(){
            ButtonAnalysis(activity as Context).addButtonDetails(it.tag.toString(), number3, "res2")
            openWhatsapp(number3)
        }

        //technical section
        view.call_technical_1.setOnClickListener(){
            val number =getString( R.string.technical_helpline_1).trim()
            ButtonAnalysis(activity as Context).addButtonDetails(it.tag.toString(), number, "res2")
            openPhoneDialer(number)
        }
        view.call_technical_2.setOnClickListener(){
            val number =getString( R.string.technical_helpline_2).trim()
            ButtonAnalysis(activity as Context).addButtonDetails(it.tag.toString(), number, "res2")
            openPhoneDialer(number)
        }
        view.call_technical_3.setOnClickListener(){
            val number =getString( R.string.technical_helpline_3).trim()
            ButtonAnalysis(activity as Context).addButtonDetails(it.tag.toString(), number, "res2")
            openPhoneDialer(number)
        }

        view.whatsapp_technical_2.setOnClickListener(){
            openWhatsapp(getString( R.string.technical_helpline_2).trim())
        }

        return view
    }

    private fun openPhoneDialer(phoneNumber:String){
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber))
        startActivity(intent)
    }


    private fun openWhatsapp(phoneNumber:String){
        val whatsappCheck = isAppInstalled("com.whatsapp", activity as Context)
        val whatsappBCheck = isAppInstalled("com.whatsapp.w4b", activity as Context)
        Log.e("Whatsappcheck", whatsappCheck.toString())
        Log.e("Whatsappcheck", whatsappBCheck.toString())
        val url = "https://api.whatsapp.com/send?phone=$phoneNumber"
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        if(whatsappCheck){
            i.setPackage("com.whatsapp")
            startActivity(i)
        }
        else
            if(whatsappBCheck){
                i.setPackage("com.whatsapp.w4b")
                startActivity(i)
            }
            else {

                Toast.makeText(activity as Context, "Whatsapp is not Install", Toast.LENGTH_SHORT).show()
            }
    }
    fun openWhatsappContact(number: String) {
        val uri = Uri.parse(String.format("https://api.whatsapp.com/send?phone=%s",number))
        val i = Intent(Intent.ACTION_SEND, uri)
        i.setPackage("com.whatsapp")

        startActivity(Intent.createChooser(i, ""))
    }



    private fun isAppInstalled(packageName: String, context: Context): Boolean {
        return try {
            val packageManager = context.packageManager
            packageManager.getPackageInfo(packageName,0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
    override fun onDestroyView() {
        (activity as MainActivity).supportActionBar?.title =getString(R.string.app_name)
        super.onDestroyView()
    }

}