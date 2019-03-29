package com.msahil432.sms.notifications

import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Telephony
import android.util.Log
import android.widget.Toast
import androidx.core.app.RemoteInput
import com.msahil432.sms.SmsApplication
import com.msahil432.sms.conversationActivity.ConversationActivity
import com.msahil432.sms.helpers.ContactHelper
import com.msahil432.sms.notifications.NotificationHelper.Companion.KEY_TEXT_REPLY
import com.msahil432.sms.notifications.NotificationHelper.Companion.activeNonCatNotifications
import com.msahil432.sms.notifications.NotificationHelper.Companion.activeNotifications

class NotificationActionReceiver : BroadcastReceiver() {

  val TAG = "NotificationActionReceiver"
  override fun onReceive(context: Context, intent: Intent) {
    Log.e(TAG, "Received New One!")

    val bundle = intent.extras
    if(bundle ==null || !bundle.containsKey(ACTION_TYPE_PARAM)
        || !bundle.containsKey(CONTENT_VALUE_PARAM)) {
      Log.e(TAG, "Incomplete Parameters!")
      return
    }

    val values = bundle.getParcelable<ContentValues>(CONTENT_VALUE_PARAM)!!
    val notificationId = bundle.getInt(ID_PARAM)
    Log.e(TAG, bundle.getInt(ACTION_TYPE_PARAM, 12).toString())

    when(intent.getIntExtra(ACTION_TYPE_PARAM, -1)){
      ActionTypes.OpenIntent.type -> openThread(context, values, notificationId)
      ActionTypes.ClearIntent.type -> notificationRemoved(notificationId)
      ActionTypes.Reply.type -> reply(context, values, intent, notificationId)
      ActionTypes.MarkRead.type -> markRead(context, values, notificationId)
      ActionTypes.Delete.type -> delete(context, values, notificationId)
      ActionTypes.CopyOtp.type -> copyOtp(context, values, bundle, notificationId)
    }
  }

  private fun markRead(context: Context, values: ContentValues, notificationId: Int){
    notificationRemoved(notificationId)
  }

  private fun delete(context: Context, values: ContentValues, notificationId: Int){
    notificationRemoved(notificationId)
  }

  private fun copyOtp(context: Context, values: ContentValues, bundle: Bundle, notificationId: Int){
    val otp = bundle.getString(OTP_PARAM) ?: return
    SmsApplication.copyText(context, otp)
    Toast.makeText(context, "OTP is copied.", Toast.LENGTH_SHORT).show()
    markRead(context, values, notificationId)
  }

  private fun reply(context: Context, values: ContentValues, intent: Intent, notificationId: Int){
    val rText = RemoteInput.getResultsFromIntent(intent)?.getCharSequence(KEY_TEXT_REPLY).toString()
    Toast.makeText(context, rText, Toast.LENGTH_SHORT).show()
    values.put(Telephony.Sms.BODY, rText)
    NotificationHelper.createPersonalNotification(context, values)
  }

  private fun openThread(context: Context, values: ContentValues, notificationId: Int){
    val threadId = values.getAsString(Telephony.Sms.THREAD_ID)
    val address = values.getAsString(Telephony.Sms.ADDRESS)
    val name = ContactHelper.getName(context, address)
    ConversationActivity.OpenThread(threadId, "", name, null, address, context)
    notificationRemoved(notificationId)
  }

  private fun notificationRemoved(notificationId: Int){
    if(activeNonCatNotifications.contains(notificationId.toString()))
      activeNonCatNotifications.remove(notificationId.toString())
    if(activeNotifications.contains(notificationId.toString()))
      activeNotifications.remove(notificationId.toString())
  }

  companion object {
    const val CONTENT_VALUE_PARAM = "CONTENT_VALUES"
    const val ACTION_TYPE_PARAM = "ACTION_TYPE"
    const val OTP_PARAM = "OTP"
    const val ID_PARAM = "NOTIFICATION_ID"

    public enum class ActionTypes(var type: Int){
      OpenIntent(6),
      ClearIntent(1),
      MarkRead(2),
      Delete(3),
      Reply(4),
      CopyOtp(5)
    }
  }

}
