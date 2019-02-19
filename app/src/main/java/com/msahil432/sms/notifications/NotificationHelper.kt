package com.msahil432.sms.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
import com.msahil432.sms.R
import com.msahil432.sms.SplashActivity
import com.msahil432.sms.helpers.ContactHelper

/**
 * Created by msahil432
 **/

class NotificationHelper{
  companion object {
    const val PERSONAL_CHANNEL_ID = "com.msahil432.sms.PERSONAL"
    const val ADS_CHANNEL_ID = "com.msahil432.sms.ADS"
    const val MONEY_CHANNEL_ID = "com.msahil432.sms.MONEY"
    const val UPDATES_CHANNEL_ID = "com.msahil432.sms.UPDATES"
    const val OTHERS_CHANNEL_ID = "com.msahil432.sms.OTHERS"

    fun createPersonalNotification(context: Context, address: String, name: String, text: String){

      val intent = Intent(context, SplashActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
      }
      val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

      val personBuilder = Person.Builder()
      personBuilder.setIcon(IconCompat.createWithBitmap(ContactHelper.GetThumbnail(context, address, name)))
      personBuilder.setName(name)

      val me : String? = null
      val mBuilder = NotificationCompat.Builder(context, PERSONAL_CHANNEL_ID)
        .setSmallIcon(R.drawable.icon)
        .setStyle(NotificationCompat.MessagingStyle(personBuilder.build())
          .setConversationTitle(name)
          .addMessage("Hi", System.currentTimeMillis()-2900, personBuilder.build())
          .addMessage("> My Hi", System.currentTimeMillis()-2000, me)
          .addMessage(context.getString(R.string.invite_msg),
            System.currentTimeMillis()-360, personBuilder.build())
          .addMessage("> Not much", System.currentTimeMillis()-32, me)
          .addMessage("How about lunch?", System.currentTimeMillis(), personBuilder.build()))
        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(pendingIntent)
        .setGroup(PERSONAL_CHANNEL_ID)
        .setAutoCancel(true)

      notifyAndAddDot(context, mBuilder)
    }

    private fun notifyAndAddDot(context: Context, buidler: NotificationCompat.Builder){

      val c = context.contentResolver.query(Uri.parse("content://sms/inbox"), null,
        "read=0", null, null)
      Log.e(PERSONAL_CHANNEL_ID, "setting count")
      if(c!=null && c.count>0) {
        Log.e(PERSONAL_CHANNEL_ID, "setting count")
        buidler.setNumber(c.count)
        c.close()
      }
      with(NotificationManagerCompat.from(context)) {
        notify(0, buidler.build())
      }
    }

    fun CreateChannels(context: Context){
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val personalChannel = NotificationChannel(PERSONAL_CHANNEL_ID,
          context.getString(R.string.personal_sms), NotificationManager.IMPORTANCE_HIGH)
        personalChannel.description = context.getString(R.string.personal_sms)
        personalChannel.setShowBadge(true)
        val adsChannel = NotificationChannel(ADS_CHANNEL_ID,
          context.getString(R.string.ads_sms), NotificationManager.IMPORTANCE_LOW)
        adsChannel.description = context.getString(R.string.ads_sms)
        val moneyChannel = NotificationChannel(MONEY_CHANNEL_ID,
          context.getString(R.string.money_sms), NotificationManager.IMPORTANCE_HIGH)
        moneyChannel.description = context.getString(R.string.money_sms)
        moneyChannel.setShowBadge(true)
        val updatesChannel = NotificationChannel(UPDATES_CHANNEL_ID,
          context.getString(R.string.updates_sms), NotificationManager.IMPORTANCE_DEFAULT)
        updatesChannel.description = context.getString(R.string.updates_sms)
        updatesChannel.setShowBadge(true)
        val othersChannel = NotificationChannel(OTHERS_CHANNEL_ID,
          context.getString(R.string.other_sms), NotificationManager.IMPORTANCE_DEFAULT)
        othersChannel.description = context.getString(R.string.other_sms)
        othersChannel.setShowBadge(true)

        val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannels( listOf( personalChannel, adsChannel,
                  moneyChannel, updatesChannel, othersChannel ) )
      }

    }
  }
}