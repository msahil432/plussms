package com.msahil432.sms.prefs;

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
    prefs = context.getSharedPreferences("basics", Context.MODE_PRIVATE);
  }

  public boolean firstRun(){
    return prefs.getBoolean("isItFirstRun", true);
  }

  public void setFirstRun(){
    prefs.edit().putBoolean("isItFirstRun", false).apply();
  }

  public boolean darkMode(){
    return prefs.getBoolean("darkMode", false);
  }

  public void setDarkMode(boolean b){
    prefs.edit().putBoolean("darkMode", b).apply();
  }

}
