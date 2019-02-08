package com.msahil432.sms.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.msahil432.sms.services.BackgroundCategorizationService;

public class SmsReceiver extends BroadcastReceiver {

  private static final String TAG = "SmsReceiver";

  @Override
  public void onReceive(Context context, Intent intent) {
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
}
