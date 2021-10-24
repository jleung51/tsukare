package com.jleung.tsukare

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class TsukareBroadcastReceiver : BroadcastReceiver() {

    private val tag = TsukareBroadcastReceiver::class.java.name

    override fun onReceive(context: Context, intent: Intent) {
        val log = StringBuilder()
            .append("Action: ${intent.action}\n")
            .append("URI: ${intent.toUri(Intent.URI_INTENT_SCHEME)}\n")
            .toString()
        Log.i(tag, log)

        Log.d(tag, "Alarm activated; opening Tsukare.")
        val intentToActivity = Intent(context, SplashActivity::class.java)
        intentToActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intentToActivity)
    }

}