package com.msahil432.sms;

import android.app.Application;
import android.content.*;
import android.provider.Telephony;
import android.util.Log;
import android.widget.Toast;
import androidx.room.Room;
import com.msahil432.sms.database.SmsDatabase;
import com.msahil432.sms.notifications.NotificationHelper;
import com.msahil432.sms.receivers.BootCompletedReceiver;
import com.msahil432.sms.services.BackgroundCategorizationService;

public class SmsApplication extends Application {

  private SmsDatabase smsDatabase;

  @Override
  public void onCreate() {
    super.onCreate();
    smsDatabase = Room.databaseBuilder(getApplicationContext(), SmsDatabase.class, "database-sms")
        .fallbackToDestructiveMigration()
        .build();
    NotificationHelper.Companion.CreateChannels(getApplicationContext());
    BackgroundCategorizationService.StartService(getApplicationContext());
    IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
    getApplicationContext().registerReceiver(new BootCompletedReceiver(), filter);
  }

  public SmsDatabase getSmsDatabase() {
    if(smsDatabase== null || !smsDatabase.isOpen())
      Room.databaseBuilder(getApplicationContext(), SmsDatabase.class, "database-sms")
          .fallbackToDestructiveMigration().build();
    return smsDatabase;
  }

  public static boolean AmIDefaultApp(Context context){
    try{
      String defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(context);
      return defaultSmsPackage != null && defaultSmsPackage.equals(context.getPackageName());
    }catch (Exception e){
      Log.e("amIDefaultApp", e.getMessage());
      return false;
    }
  }

  public static Intent MakeDefaultApp(){
    Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
    intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
        "com.msahil432.sms");
    return intent;
  }

  public static boolean copyOTP(Context context, String text) {
    try {
      ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
      ClipData clip = ClipData.newPlainText("OTP", text);
      assert clipboard != null;
      clipboard.setPrimaryClip(clip);
      Toast.makeText(context, text + context.getString(R.string.copied), Toast.LENGTH_SHORT).show();
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

}
