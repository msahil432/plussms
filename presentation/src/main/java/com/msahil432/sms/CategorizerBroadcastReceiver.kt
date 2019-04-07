package com.msahil432.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class CategorizerBroadcastReceiver : BroadcastReceiver() {

    val TAG = "CategorizerBrdcstRcr"

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action!="com.msahil432.sms.START_CATEGORIZATION"){
            Log.e(TAG, "Unknown Intent received")
        }
        Log.e(TAG, "Starting Activity")
        val i = Intent(context, CategorizationActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(i)
    }
}
