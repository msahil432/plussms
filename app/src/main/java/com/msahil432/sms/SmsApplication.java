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

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsApplication extends Application {

  private static SmsDatabase smsDatabase;

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

  public static SmsDatabase getSmsDatabase(Context context) {
    if(smsDatabase== null || !smsDatabase.isOpen())
      Room.databaseBuilder(context, SmsDatabase.class, "database-sms")
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

  public static boolean copyText(Context context, String text) {
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

  public static ArrayList<String> findOtp(String text){
    try{
      text = text.toLowerCase();
      if( ! (text.contains("code") || text.contains("one time password")
              || (text.contains("otp")) ))
        return null;

      ArrayList<String> codes = new ArrayList<>();
      Pattern p = Pattern.compile("\\d{4,8}");
      Matcher m = p.matcher(text);
      if(! m.find())
        return null;
      try {
        for (int i = 0; i < 3; i++)
          codes.add(m.group(i));
      }catch (Exception e){}
      return codes;
    }catch (Exception e){
      Log.e("FindOtp", "findOtp: "+e.getMessage(), e);
    }
    return null;
  }

}
