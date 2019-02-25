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
import com.msahil432.sms.SmsApplication
import com.msahil432.sms.SplashActivity
import com.msahil432.sms.helpers.ContactHelper
import com.msahil432.sms.homeActivity.HomeActivity
import java.lang.Exception

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

    fun showSummary(context: Context) {
      try {
        val SUMMARY_ID = 1011
        val SUMMARY_GROUP_ID = "com.msahil432.sms.SUMMARY"

        val dao = SmsApplication.getSmsDatabase(context).userDao()

        val notiService = NotificationManagerCompat.from(context)

        val adsUnread = dao.getUnreadCount("ADS").size
        if(adsUnread>0){
          val noti = NotificationCompat.Builder(context, OTHERS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_delete_sweep_black_24dp)
            .setContentTitle(context.getString(R.string.ads_sms))
            .setContentText("$adsUnread ${context.getString(R.string.unread_sms)}")
            .setContentIntent(
              PendingIntent.getActivity(
                context, 2,
                HomeActivity.openCat(context, HomeActivity.Companion.CAT.ADS), 0
              )
            )
            .setGroup(SUMMARY_GROUP_ID)
            .setAutoCancel(true)
            .build()
          notiService.notify(2, noti)
        }

        val othersUnread = dao.getUnreadCount("OTHERS").size
        if(othersUnread>0){
          val noti = NotificationCompat.Builder(context, OTHERS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_speaker_notes_black_24dp)
            .setContentTitle(context.getString(R.string.other_sms))
            .setContentText("$othersUnread ${context.getString(R.string.unread_sms)}")
            .setContentIntent(
              PendingIntent.getActivity(
                context, 5,
                HomeActivity.openCat(context, HomeActivity.Companion.CAT.OTHERS), 0
              )
            )
            .setGroup(SUMMARY_GROUP_ID)
            .setAutoCancel(true)
            .build()
          notiService.notify(5, noti)
        }

        val updatesUnread = dao.getUnreadCount("UPDATES").size
        if(updatesUnread>0){
          val noti = NotificationCompat.Builder(context, OTHERS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_info_black_24dp)
            .setContentTitle(context.getString(R.string.updates_sms))
            .setContentText("$updatesUnread ${context.getString(R.string.unread_sms)}")
            .setContentIntent(
              PendingIntent.getActivity(
                context, 4,
                HomeActivity.openCat(context, HomeActivity.Companion.CAT.UPDATES), 0
              )
            )
            .setGroup(SUMMARY_GROUP_ID)
            .setAutoCancel(true)
            .build()
          notiService.notify(4, noti)
        }

        val moneyUnread = dao.getUnreadCount("MONEY").size
        if(moneyUnread>0){
          val noti = NotificationCompat.Builder(context, OTHERS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_attach_money_black_24dp)
            .setContentTitle(context.getString(R.string.money_sms))
            .setContentText("$moneyUnread ${context.getString(R.string.unread_sms)}")
            .setContentIntent(
              PendingIntent.getActivity(
                context, 3,
                HomeActivity.openCat(context, HomeActivity.Companion.CAT.MONEY), 0
              )
            )
            .setGroup(SUMMARY_GROUP_ID)
            .setAutoCancel(true)
            .build()
          notiService.notify(3, noti)
        }

        val persUnread = dao.getUnreadCount("PERSONAL").size
        if(persUnread>0){
          val personalNoti = NotificationCompat.Builder(context, OTHERS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_person_pin_black_24dp)
            .setContentTitle(context.getString(R.string.personal_sms))
            .setContentText("$persUnread ${context.getString(R.string.unread_sms)}")
            .setContentIntent(
              PendingIntent.getActivity(
                context, 1,
                HomeActivity.openCat(context, HomeActivity.Companion.CAT.PERSONAL), 0
              )
            )
            .setGroup(SUMMARY_GROUP_ID)
            .setAutoCancel(true)
            .build()
          notiService.notify(1, personalNoti)
        }

        val totalUnread = persUnread+moneyUnread+adsUnread+othersUnread+updatesUnread

        val summaryNotification = NotificationCompat.Builder(context, OTHERS_CHANNEL_ID)
          .setContentTitle(context.getString(R.string.app_name_summary))
          .setContentText("$totalUnread ${context.getString(R.string.unread_sms)}")
          .setSmallIcon(R.drawable.icon)
          .setStyle(NotificationCompat.InboxStyle()
            .setBigContentTitle("$totalUnread ${context.getString(R.string.unread_sms)}")
            .setSummaryText("$totalUnread ${context.getString(R.string.unread_sms)}"))
          .setGroup(SUMMARY_GROUP_ID)
          .setAutoCancel(true)
          .setGroupSummary(true)
          .build()

        notiService.notify(SUMMARY_ID, summaryNotification)

      }catch (e: Exception){
        Log.e("Notification Helper", e.message, e)
      }
    }

  }
}