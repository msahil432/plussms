package com.msahil432.sms.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.msahil432.sms.common.BaseViewModel
import com.msahil432.sms.common.RetroFit
import com.msahil432.sms.notifications.NotificationHelper
import com.msahil432.sms.services.BackgroundCategorizationService
import com.msahil432.sms.settingsActivity.BasicPrefs
import java.net.InetAddress

/**
 * Created by msahil432
 **/

open class BootCompletedReceiver: BroadcastReceiver(){
  override fun onReceive(context: Context?, intent: Intent?) {
    try {
      if(!BasicPrefs.getInstance(context!!).setupDone())
        return
      val ipAddr = InetAddress.getByName(RetroFit.hostUrl)
      if (!ipAddr.equals(""))
        BackgroundCategorizationService.StartService(context)

      if(intent!!.action!! != Intent.ACTION_BOOT_COMPLETED)
        return

      NotificationHelper.showSummary(context)
    } catch (e: Exception) {
      Log.e("BootCompleteReceiver", e.message, e)
      return
    }
  }
}