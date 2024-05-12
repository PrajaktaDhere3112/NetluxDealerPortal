package com.kdapps.netluxdp.utils

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import com.kdapps.netluxdp.R
import kotlinx.android.synthetic.main.progress_bar_layout.*

class showProgressDialog() {

    fun showProgressDialog(dialog: Dialog, text: String){

        dialog.setContentView(R.layout.progress_bar_layout)
        dialog.progress_text.text = text
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        //start progress dialog
        dialog.show()
    }

    fun hideProgressDialog(dialog: Dialog){
        dialog.dismiss()
    }
}