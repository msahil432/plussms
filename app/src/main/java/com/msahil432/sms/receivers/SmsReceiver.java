package com.msahil432.sms.receivers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.msahil432.sms.R;
import com.msahil432.sms.SmsApplication;
import com.msahil432.sms.SplashActivity;
import com.msahil432.sms.notifications.NotificationHelper;
import com.msahil432.sms.services.BackgroundCategorizationService;

public class SmsReceiver extends BroadcastReceiver {

  private static final String TAG = "SmsReceiver";

  @Override
  public void onReceive(Context context, Intent intent) {

    if(SmsApplication.AmIDefaultApp(context)){
      doDefaultAppStuff(context, intent);
    }

    Log.e(TAG, "SMS Received");
    Cursor c = context.getContentResolver().query(Uri.parse("content://sms/"),
        null, "body IS NOT NULL", null, "date desc");
    Intent i = new Intent(context, BackgroundCategorizationService.class);
    if(c != null && !c.moveToFirst()){
      i.putExtra(BackgroundCategorizationService.PARAM_TIMESTAMP,
          c.getLong(c.getColumnIndex("date")));
      c.close();
    }else {
      i.putExtra(BackgroundCategorizationService.PARAM_TIMESTAMP, -1L);
    }
    context.startService(i);
  }

  private void doDefaultAppStuff(Context context, Intent intent){
    Bundle bundle = intent.getExtras();
    if(bundle==null)
      return;
    Object pdus[] = (Object[]) bundle.get("pdus");
    if(pdus==null)
      return;
    for(Object obj : pdus){
      try{
        SmsMessage message = SmsMessage.createFromPdu((byte[]) obj, bundle.getString("format"));
        String senderNo = message.getDisplayOriginatingAddress();
        String text = message.getDisplayMessageBody();

        issueNotification(context, senderNo, text);

        ContentValues values = new ContentValues();
        values.put(Telephony.Sms.Inbox.ADDRESS, senderNo);
        values.put(Telephony.Sms.Inbox.BODY, text);
        values.put(Telephony.Sms.Inbox.DATE_SENT, message.getTimestampMillis());
        context.getContentResolver().insert(Telephony.Sms.Inbox.CONTENT_URI, values);
      }catch (Exception e){
        Log.e(TAG, e.getMessage(), e);
      }
    }
    this.abortBroadcast();
  }

  private void issueNotification(Context context, String senderNo, String message) {
    Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
        R.mipmap.ic_launcher);

    NotificationCompat.Builder mBuilder =
        new NotificationCompat.Builder(context, NotificationHelper.OTHERS_CHANNEL_ID)
            .setLargeIcon(icon)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(senderNo)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)
            .setContentText(message);

    Intent resultIntent = new Intent(context, SplashActivity.class);
//    resultIntent.putExtra(SyncStateContract.Constants.CONTACT_NAME,senderNo);
//    resultIntent.putExtra(SyncStateContract.Constants.FROM_SMS_RECIEVER,true);
    PendingIntent resultPendingIntent =
        PendingIntent.getActivity(
            context,
            0,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        );

    mBuilder.setContentIntent(resultPendingIntent);

    NotificationManager mNotifyMgr =
        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    mNotifyMgr.notify(101, mBuilder.build());
  }

}
