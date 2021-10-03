package com.jleung.tsukare

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView


class StationAdapter(context: Context, private val resource: Int, stationList: List<Station>):
    ArrayAdapter<Station>(context, resource, stationList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        // Inflate the view if not given
        val view = convertView ?: LayoutInflater.from(context).inflate(resource, null)

        // Find the corresponding textview
        val stationTextView = view.findViewById<TextView>(R.id.station_text)

        // Set the station name
        val stationName = getItem(position)?.name ?: ""
        stationTextView.text = stationName

        return view
    }

}