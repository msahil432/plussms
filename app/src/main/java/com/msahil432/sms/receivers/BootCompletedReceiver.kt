package com.msahil432.sms.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.msahil432.sms.common.RetroFit
import com.msahil432.sms.services.BackgroundCategorizationService
import java.net.InetAddress

/**
 * Created by msahil432
 **/

open class BootCompletedReceiver: BroadcastReceiver(){
  override fun onReceive(context: Context?, intent: Intent?) {
    try {
      val ipAddr = InetAddress.getByName(RetroFit.hostUrl)
      if(!ipAddr.equals(""))
        BackgroundCategorizationService.StartService(context)
    } catch (e: Exception) {
      return
    }
  }
}