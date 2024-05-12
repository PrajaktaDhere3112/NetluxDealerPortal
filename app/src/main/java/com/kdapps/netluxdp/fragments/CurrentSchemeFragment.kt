package com.kdapps.netluxdp.fragments

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kdapps.netluxdp.R
import com.kdapps.netluxdp.activities.MainActivity
import com.kdapps.netluxdp.entities.ButtonClickedEntity
import com.kdapps.netluxdp.utils.RoomDatabaseBuilder


class CurrentSchemeFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        (activity as MainActivity).supportActionBar?.title ="Current Scheme"
        val view = inflater.inflate(R.layout.fragment_current_scheme, container, false)

        val list = getButtonAnalysisDetails(activity as Context).execute().get()
        Log.e("buttonList", list.toString())
        return view
    }

    class getButtonAnalysisDetails(val context: Context):AsyncTask<Void, Void, List<ButtonClickedEntity>>() {

        override fun doInBackground(vararg p0: Void?): List<ButtonClickedEntity> {
            val db = RoomDatabaseBuilder(context).getRoomDatabase()
            return db.buttonClickedDetailsDao().getAllButtonDetails()
        }
    }
    override fun onDestroyView() {
        (activity as MainActivity).supportActionBar?.title =getString(R.string.app_name)
        super.onDestroyView()
    }

}