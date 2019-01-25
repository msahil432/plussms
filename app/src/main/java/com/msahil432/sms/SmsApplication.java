package com.msahil432.sms;

import android.app.Application;
import androidx.room.Room;
import com.msahil432.sms.database.SmsDatabase;

public class SmsApplication extends Application {

  private SmsDatabase smsDatabase;

  @Override
  public void onCreate() {
    super.onCreate();
    smsDatabase = Room.databaseBuilder(getApplicationContext(), SmsDatabase.class, "database-sms").build();
  }

  public SmsDatabase getSmsDatabase() {
    if(smsDatabase== null || !smsDatabase.isOpen())
      Room.databaseBuilder(getApplicationContext(), SmsDatabase.class, "database-sms").build();
    return smsDatabase;
  }
}
