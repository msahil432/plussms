package com.msahil432.sms.settingsActivity;

import android.content.Context;
import android.content.SharedPreferences;

public class BasicPrefs {

  private static BasicPrefs instance = null;
  public static BasicPrefs getInstance(Context context){
    if(instance==null){
      instance = new BasicPrefs(context);
    }
    return instance;
  }

  private SharedPreferences prefs;

  private BasicPrefs(Context context){
    prefs = context.getSharedPreferences("com.msahil432.sms_preferences", Context.MODE_PRIVATE);
  }

  public boolean firstRun(){
    return prefs.getBoolean("isItFirstRun", true);
  }

  public void setFirstRun(){
    prefs.edit().putBoolean("isItFirstRun", false).apply();
  }

  public boolean setupDone(){
    return prefs.getBoolean("setupDone", false);
  }

  public void setSetup(){
    prefs.edit().putBoolean("setupDone", true).apply();
  }

  public String darkMode(){
    return prefs.getString("dark_theme", null);
  }

}
