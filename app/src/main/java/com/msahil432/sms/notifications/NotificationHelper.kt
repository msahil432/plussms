package com.msahil432.sms.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Telephony
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.graphics.drawable.IconCompat
import com.msahil432.sms.R
import com.msahil432.sms.SmsApplication
import com.msahil432.sms.database.SmsDatabase
import com.msahil432.sms.helpers.ContactHelper
import com.msahil432.sms.homeActivity.HomeActivity
import com.msahil432.sms.services.BackgroundCategorizationService
import java.lang.Exception
import java.util.concurrent.Executors
import java.util.concurrent.ThreadLocalRandom

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
    private const val TAG = "NotificationHelper"

    var activeNonCatNotifications = HashMap<String, ContentValues>()
    var activeNotifications = HashMap<String, ContentValues>()
    private lateinit var db : SmsDatabase
    private val BgThread = Executors.newSingleThreadExecutor()

    fun createInitialNotification(context: Context, values : ContentValues){
      BgThread.execute {
        try {
          val threadId = values.getAsInteger(Telephony.Sms.THREAD_ID)
          val notificationId = createNotificationId(threadId)

          // Check if OTP
          val otp = SmsApplication.findOtp(values.getAsString(Telephony.Sms.BODY))
          if(otp!=null){
            createOthersNotification(context, values, OTHERS_CHANNEL_ID, notificationId, otp[0])
            return@execute
          }

          // Check if it's from Personal Category
          if (!::db.isInitialized) {
            db = SmsApplication.getSmsDatabase(context)
          }
          val t = db.userDao().getCatOfThread(threadId.toString())
          if (!t.isEmpty()) {
            when (t[0]) {
              "PERSONAL" -> {
                createPersonalNotification(context, values)
                activeNotifications[threadId.toString()] = values
                val ts = values.getAsString(Telephony.Sms.DATE)
                BackgroundCategorizationService.removeTsFromNonCat(context, ts)
                return@execute
              }
              "ADS" -> {
                createOthersNotification(context, values, ADS_CHANNEL_ID, notificationId)
              }
              "UPDATES" -> {
                createOthersNotification(context, values, UPDATES_CHANNEL_ID, notificationId)
              }
              "MONEY" -> {
                createOthersNotification(context, values, MONEY_CHANNEL_ID, notificationId)
              }
              else ->{
                createOthersNotification(context, values, OTHERS_CHANNEL_ID, notificationId)
              }
            }
          }else{
            createOthersNotification(context, values, OTHERS_CHANNEL_ID, notificationId)
          }
          activeNonCatNotifications[notificationId.toString()] = values
        } catch (e: Exception) {
          Log.e(TAG, e.message, e)
        }
      }
    }

    fun catFoundForNotification(context: Context, values: ContentValues){
      val threadId = values.getAsString(Telephony.Sms.THREAD_ID)
      if (!::db.isInitialized) {
        return
      }
      val t = db.userDao().getCatOfThread(threadId)
      if(!t.isEmpty() && t[0] == "PERSONAL"){
          createPersonalNotification(context, values)
        return
      }
    }

    private fun createOthersNotification(context: Context, values: ContentValues,
                                         channel: String, notificationId : Int, otp: String? = null){
      val address = values.getAsString(Telephony.Sms.ADDRESS)
      val name = ContactHelper.getName(context, address)
      val image = ContactHelper.getContactsPhoto(context, address, name)

      var builder = when(channel){
        MONEY_CHANNEL_ID ->{
          NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_attach_money_black_24dp)
        }
        UPDATES_CHANNEL_ID ->{
          NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_info_black_24dp)
        }
        ADS_CHANNEL_ID ->{
          NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_delete_sweep_black_24dp)
        }
        else-> {
          NotificationCompat.Builder(context, OTHERS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_speaker_notes_black_24dp)
        }
      }

      builder
        .setLargeIcon(image)
        .setContentTitle(name)
        .setStyle(NotificationCompat.BigTextStyle()
          .bigText(values.getAsString(Telephony.Sms.BODY)))
        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
        .setOnlyAlertOnce(true)
        .setAutoCancel(true)

      if(otp!=null){
        builder = addOtpCopyIntent(context, otp, builder, values, notificationId)
      }

      val threadId = values.getAsString(Telephony.Sms.THREAD_ID)
      if(ContactHelper.isPhoneNumber(address)){
        builder.addAction(getReplyAction(context, threadId.toInt(), notificationId, values))
      }

      builder = addGeneralActions(context, builder, notificationId, values)

      notifyAndAddDot(context, builder, notificationId)
    }

    fun createPersonalNotification(context: Context, values: ContentValues){
      val address = values.getAsString(Telephony.Sms.ADDRESS)
      val name = ContactHelper.getName(context, address)
      val image = ContactHelper.getContactsPhoto(context, address, name)

      val threadId = values.getAsString(Telephony.Sms.THREAD_ID)

      val person = Person.Builder()
        .setIcon(IconCompat.createWithBitmap(image))
        .setName(name)
        .build()

      val me : String? = null
      val style = NotificationCompat.MessagingStyle(person)
        .setConversationTitle(name)

      var i = 5
      val cur = context.contentResolver.query(Telephony.Sms.CONTENT_URI, null,
        "${Telephony.Sms.THREAD_ID}=$threadId", null,
        "${Telephony.Sms.DATE} ASC")

      if(cur!=null && cur.moveToFirst()){
        val bodyIndex = cur.getColumnIndex(Telephony.Sms.BODY)
        val timeIndex = cur.getColumnIndex(Telephony.Sms.DATE)
        if(cur.count>i)
          cur.moveToPosition(cur.count-i)
        do{
          style.addMessage(cur.getString(bodyIndex), cur.getLong(timeIndex), person)
          i--
        }while(cur.moveToNext() && i<3)
        cur.close()
      }

      var builder = NotificationCompat.Builder(context, PERSONAL_CHANNEL_ID)
        .setSmallIcon(R.drawable.icon)
        .setStyle(style)
        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setOnlyAlertOnce(true)
        .setGroup(PERSONAL_CHANNEL_ID)
        .setAutoCancel(true)

      builder.addAction(getReplyAction(context, threadId.toInt(), threadId.toInt(), values))
      builder = addGeneralActions(context, builder, threadId.toInt(), values)

      notifyAndAddDot(context, builder, threadId.toInt())
    }

    private fun notifyAndAddDot(context: Context, builder: NotificationCompat.Builder, id: Int){
      val c = context.contentResolver.query(Uri.parse("content://sms/inbox"), null,
        "read=0", null, null)
      Log.e(PERSONAL_CHANNEL_ID, "setting count")
      if(c!=null && c.count>0) {
        Log.e(PERSONAL_CHANNEL_ID, "setting count")
        builder.setNumber(c.count)
        c.close()
      }
      with(NotificationManagerCompat.from(context)) {
        notify(id, builder.build())
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

    private fun addGeneralActions(context: Context, builder: NotificationCompat.Builder,
                 notificationId: Int, values: ContentValues): NotificationCompat.Builder{
      val threadId = values.getAsInteger(Telephony.Sms.THREAD_ID)
      val oType = if(1==threadId) 10 else 1
      val openIntent = Intent(context, NotificationActionReceiver::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
      }
      openIntent.putExtra(NotificationActionReceiver.ID_PARAM, notificationId)
      openIntent.putExtra(NotificationActionReceiver.CONTENT_VALUE_PARAM, values)
      openIntent.putExtra(NotificationActionReceiver.ACTION_TYPE_PARAM,
        NotificationActionReceiver.Companion.ActionTypes.OpenIntent.type)
      val pendingOpen = PendingIntent.getBroadcast(context, oType, openIntent, 0)
      builder.setContentIntent(pendingOpen)

      val cType = if(2==threadId) 10 else 2

      val clearIntent = Intent(context, NotificationActionReceiver::class.java)
      clearIntent.putExtra(NotificationActionReceiver.CONTENT_VALUE_PARAM, values)
      clearIntent.putExtra(NotificationActionReceiver.ID_PARAM, notificationId)
      clearIntent.putExtra(NotificationActionReceiver.ACTION_TYPE_PARAM,
        NotificationActionReceiver.Companion.ActionTypes.ClearIntent.type)
      clearIntent.putExtra(Telephony.Sms._ID, notificationId)
      val pendingClear = PendingIntent.getBroadcast(context, cType, clearIntent, 0)
      builder.setDeleteIntent(pendingClear)

      if(!SmsApplication.AmIDefaultApp(context))
        return builder

      val rType = if(4==threadId) 10 else 4
      val readIntent = Intent(context, NotificationActionReceiver::class.java)
      readIntent.putExtra(NotificationActionReceiver.CONTENT_VALUE_PARAM, values)
      readIntent.putExtra(NotificationActionReceiver.ACTION_TYPE_PARAM,
        NotificationActionReceiver.Companion.ActionTypes.MarkRead.type)
      val pendingRead = PendingIntent.getBroadcast(context, rType, readIntent, 0)
      builder.addAction(R.drawable.ic_delete_black_24dp, context.getString(R.string.mark_read), pendingRead)

      val dType = if(3==threadId) 10 else 3
      val delete = Intent(context, NotificationActionReceiver::class.java)
      delete.putExtra(NotificationActionReceiver.CONTENT_VALUE_PARAM, values)
      delete.putExtra(NotificationActionReceiver.ID_PARAM, notificationId)
      delete.putExtra(NotificationActionReceiver.ACTION_TYPE_PARAM,
        NotificationActionReceiver.Companion.ActionTypes.Delete.type)
      val pendingDelete = PendingIntent.getBroadcast(context, dType, delete, 0)
      builder.addAction(R.drawable.ic_delete_black_24dp, context.getString(R.string.delete), pendingDelete)

      return builder
    }

    private fun addOtpCopyIntent(context: Context, otp: String, builder: NotificationCompat.Builder,
                                 values: ContentValues, notificationId: Int): NotificationCompat.Builder{
      val threadId = values.getAsInteger(Telephony.Sms.THREAD_ID)
      val type = if(5==threadId) 10 else 5

      val copyOtpIntent = Intent(context, NotificationActionReceiver::class.java)
      copyOtpIntent.putExtra(NotificationActionReceiver.CONTENT_VALUE_PARAM, values)
      copyOtpIntent.putExtra(NotificationActionReceiver.ID_PARAM, notificationId)
      copyOtpIntent.putExtra(NotificationActionReceiver.OTP_PARAM, otp)
      copyOtpIntent.putExtra(NotificationActionReceiver.ACTION_TYPE_PARAM,
        NotificationActionReceiver.Companion.ActionTypes.CopyOtp.type)
      val pending = PendingIntent.getBroadcast(context, type, copyOtpIntent, 0)
      builder.addAction(R.drawable.ic_delete_black_24dp,
        "${context.getString(R.string.copy)} $otp", pending)

      return builder
    }

    const val KEY_TEXT_REPLY = "key_text_reply"

    private fun getReplyAction(context: Context, threadId: Int, notificationId: Int,
                               values: ContentValues): NotificationCompat.Action{
      val replyLabel: String = context.getString(R.string.reply)
      val remoteInput = RemoteInput.Builder(KEY_TEXT_REPLY).run {
        setLabel(replyLabel)
        build()
      }

      val replyIntent = Intent(context, NotificationActionReceiver::class.java)
      replyIntent.putExtra(NotificationActionReceiver.CONTENT_VALUE_PARAM, values)
      replyIntent.putExtra(NotificationActionReceiver.ID_PARAM, notificationId)
      replyIntent.putExtra(NotificationActionReceiver.ACTION_TYPE_PARAM,
        NotificationActionReceiver.Companion.ActionTypes.Reply.type)

      val replyPendingIntent: PendingIntent =
        PendingIntent.getBroadcast(context,
          threadId,
          replyIntent,
          PendingIntent.FLAG_UPDATE_CURRENT)

      return NotificationCompat.Action.Builder(R.drawable.icon,
        context.getString(R.string.reply), replyPendingIntent)
        .addRemoteInput(remoteInput)
        .build()
    }

    private fun createNotificationId(threadId: Int): Int{
      try{
        for(t : String in activeNonCatNotifications.keys){
          val tem = t.toInt()%1000000
          if(tem==threadId){
            return t.toInt()+1000000
          }
        }
        for(t : String in activeNotifications.keys){
          val tem = t.toInt()%1000000
          if(tem==threadId){
            return t.toInt()+1000000
          }
        }
        return threadId
      }catch (e: Exception){ }
      return ThreadLocalRandom.current().nextInt(0, 9999)
    }
    
  }
}