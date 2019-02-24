package com.msahil432.sms.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;
import android.util.Log;
import androidx.annotation.Nullable;
import com.msahil432.sms.SmsApplication;
import com.msahil432.sms.common.BaseViewModel;
import com.msahil432.sms.common.RetroFit;
import com.msahil432.sms.database.SMS;
import com.msahil432.sms.database.SmsDatabase;
import com.msahil432.sms.models.ServerMessage;
import com.msahil432.sms.models.ServerModel;
import com.msahil432.sms.settingsActivity.BasicPrefs;
import retrofit2.Response;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static com.msahil432.sms.helpers.ContactHelper.GetPhone;

public class BackgroundCategorizationService extends IntentService {

  public static final String PARAM_TIMESTAMP = "timestamp";
  public static final String TAG = "BackgroundCategorizationService";

  private SharedPreferences prefs;
  private SmsDatabase smsDb;

  public BackgroundCategorizationService() {
    super(TAG);
  }

  @Override
  protected void onHandleIntent(@Nullable Intent intent) {
    if(!BasicPrefs.getInstance(getApplicationContext()).setupDone())
      return;

    pingServer();

    if(prefs==null)
      prefs = getSharedPreferences("bg-service", Context.MODE_PRIVATE);
    if(smsDb == null)
      smsDb = SmsApplication.getSmsDatabase(getApplicationContext());

    Set<String> set = prefs.getStringSet("uncat", new HashSet<String>());
    try{
      if(intent!=null) {
        long ts = intent.getLongExtra(PARAM_TIMESTAMP, -1);
        if(ts!=-1){
          set.add(ts+"");
          Log.e(TAG, "Received ts:"+ts);
        }
      }

      if(set.isEmpty()) {
        Log.e(TAG, "Prefs Set is Empty");
        if (allCats())
          return;
        else{
          set = findNonCats();
          if(set==null||set.size()==0)
            return;
          Log.e(TAG, "Cat SMS are not all! "+set.size());
        }
      }
      Log.e(TAG, "Set Size: "+set.size());
      ArrayList<ServerMessage> messages = new ArrayList<>();
      for(String t: set){
        ServerMessage temp = createInitialMessage(t);
        if(temp!=null)
          messages.add(temp);
      }
      Log.e(TAG, "Categorizing "+messages.size());
      for(int i=0; i<messages.size()/50 +1; i++) {
        try {
          ServerModel model = new ServerModel();
          model.setTexts(new ArrayList<ServerMessage>());
          for (int a = 0; a < 50; a++) {
            model.getTexts().add(messages.get(i * 50 + a));
            if ((i * 50) + a + 1 == messages.size())
              break;
          }

          Response<ServerModel> res = BaseViewModel.Companion.getRetrofit().categorizeSMS(model).execute();
          if (res.body() == null)
            continue;
          for (ServerMessage s : res.body().getTexts()) {
            SMS sms = completeSMS(s);
            if(sms!=null) {
              smsDb.userDao().insertAll(sms);
              Log.e(TAG, "Categorized, ts:"+sms.timestamp);
              set.remove(sms.timestamp+"");
            }
          }
        }catch (Exception e){
          Log.e(TAG, e.getMessage(), e);
        }
      }
    }catch (Exception e){
      Log.e(TAG, e.getMessage(), e);
    }
    prefs.edit().putStringSet("uncat", set).apply();
  }

  private ServerMessage createInitialMessage(String timestamp){
    try {
      Cursor c = getContentResolver().query(Telephony.Sms.Inbox.CONTENT_URI,
          new String[]{Telephony.Sms.Inbox.THREAD_ID, Telephony.Sms.Inbox.BODY, Telephony.Sms.Inbox._ID,
              Telephony.Sms.Inbox.DATE}, Telephony.Sms.Inbox.DATE + "=" + timestamp,
          null, null);
      if(c==null || !c.moveToFirst()) {
        // Try in Sent SMS
        c = getContentResolver().query(Telephony.Sms.Sent.CONTENT_URI,
            new String[]{Telephony.Sms.Sent.THREAD_ID, Telephony.Sms.Sent.BODY, Telephony.Sms.Sent._ID,
                Telephony.Sms.Sent.DATE}, Telephony.Sms.Sent.DATE_SENT + "=" + timestamp,
            null, null);
        if(c==null || !c.moveToFirst()) {
          return null;
        }
      }
      ServerMessage s = new ServerMessage();
      s.setId("t"+c.getInt(0)+"m"+c.getInt(2));
      s.setTextMessage(c.getString(1));
      c.close();
      return s;
    }catch (Exception e){
      Log.e(TAG, e.getMessage(), e);
    }
    return null;
  }

  private SMS completeSMS(ServerMessage t){
    try{
      String threadId = t.getId().substring(1, t.getId().indexOf('m'));
      String mId = t.getId().substring(t.getId().indexOf('m') + 1);
      String phone = GetPhone(getApplicationContext(), threadId);
      Cursor cur2 = getContentResolver().query(Telephony.Sms.CONTENT_URI,
          new String[]{Telephony.Sms.STATUS, Telephony.Sms.DATE, Telephony.Sms._ID},
          Telephony.Sms._ID + "=" + mId, null, null);
      if(cur2==null || !cur2.moveToFirst())
        return null;
      int read = cur2.getInt(0);
      long timestamp = cur2.getLong(1);
      cur2.close();
      SMS sms = new SMS(t.getId(), "OTHERS", t.getTextMessage(), threadId, mId, phone, read, timestamp);
      switch (sms.cat){
        case "PROMOTIONAL": case "PROMO" : {
          sms.cat = "ADS";
        }
        case "PERSONAL": case  "URGENT" : {
          sms.cat = "PERSONAL";
        }
        case "MONEY": case "OTP": case "BANK" : {
          sms.cat = "MONEY";
        }
        case "UPDATES": case "WALLET/APP": case "ORDER" : {
          sms.cat = "UPDATES";
        }
      }
      return sms;
    }catch (Exception e){
      Log.e(TAG, e.getMessage(), e);
    }
    return null;
  }

  private boolean allCats(){
    Cursor c = getContentResolver().query(Uri.parse("content://sms/"),
        null, "body IS NOT NULL", null, null);
    if(c==null)
      return true;
    int shouldBe = c.getCount();
    c.close();
    return shouldBe==smsDb.userDao().getAll().size();
  }

  private Set<String> findNonCats(){
    try {
      Cursor c = getContentResolver().query(Telephony.Sms.CONTENT_URI,
          null, "body IS NOT NULL", null, null);
      if (c == null || !c.moveToFirst())
        return null;
      int tsIndex = c.getColumnIndex(Telephony.Sms.DATE);
      int tsSIndex = c.getColumnIndex(Telephony.Sms.DATE_SENT);
      HashSet<String> set = new HashSet<>();
      do{
        try {
          long ts = c.getLong(tsIndex);
          if(ts==0)
            ts = c.getLong(tsSIndex);
          if (smsDb.userDao().getCat(ts).isEmpty()) {
            set.add(ts + "");
          }
        }catch (Exception e){
          Log.e(TAG, e.getMessage(), e);
        }
      }while (c.moveToNext());
      c.close();
      return set;
    }catch (Exception e){
      Log.e(TAG, e.getMessage(), e);
    }
    return null;
  }

  public static void StartService(Context context){
    Intent i = new Intent(context, BackgroundCategorizationService.class);
    i.putExtra(PARAM_TIMESTAMP, -1L);
    context.startService(i);
  }

  public static void pingServer(){
    try{
      URL url = new URL(RetroFit.hostUrl);
      HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
      urlc.setRequestProperty("Connection", "close");
      urlc.setConnectTimeout(1000 * 30);
      urlc.connect();

      if (urlc.getResponseCode() == 200) {
        Log.i(TAG, "Ping Successful to Server");
      }
    }catch (Exception e){
      Log.i(TAG, "Ping unsuccessful to Server");
    }
  }

}
