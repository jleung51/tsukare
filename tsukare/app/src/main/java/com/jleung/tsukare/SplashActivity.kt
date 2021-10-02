package com.jleung.tsukare

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Log.d(Companion.TAG, "onCreate")

        Handler(Looper.getMainLooper()).postDelayed(Runnable {
                val intent = Intent(this, StationActivity::class.java)
                startActivity(intent)
                finish()
                Log.d(Companion.TAG, "Started intent to move to StationActivity")
        }, Companion.DELAY)

    }

    companion object {
        private val TAG = SplashActivity::class.toString()

        private const val DELAY = 1000L
    }


}