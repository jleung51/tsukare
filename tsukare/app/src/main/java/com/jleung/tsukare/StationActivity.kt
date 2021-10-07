package com.jleung.tsukare

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView.*
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class StationActivity : AppCompatActivity() {

    private val stationList: MutableList<Station> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_station)

        stationList.add(Station("Joyce-Collingwood", 0.0, 0.0))
        stationList.add(Station("Coquitlam Central", 0.0, 0.0))
        stationList.add(Station("Surrey Central", 0.0, 0.0))
        stationList.add(Station("Waterfront", 0.0, 0.0))

        // Create and set custom adapter to display station list
        val listView: ListView = findViewById(R.id.list_stations)
        listView.adapter = StationAdapter(this, R.layout.layout_station, stationList)

        // Set list items to open alarm activity for the given station, when clicked
        listView.isClickable = true
        listView.onItemClickListener =
            OnItemClickListener { parent, _: View, position, _ ->

                val selectedStation = parent.getItemAtPosition(position) as Station

                val intent = Intent(applicationContext, NapActivity::class.java)
                    .putExtra("station", selectedStation)
                startActivity(intent)
            }
    }

}