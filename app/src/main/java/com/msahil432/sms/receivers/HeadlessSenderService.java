package com.msahil432.sms.receivers;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;

public class HeadlessSenderService extends Service {
  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}
