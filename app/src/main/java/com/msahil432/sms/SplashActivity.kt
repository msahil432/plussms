package com.msahil432.sms

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatDelegate
import android.view.View
import com.crashlytics.android.Crashlytics
import com.msahil432.sms.homeActivity.HomeActivity
import com.msahil432.sms.prefs.BasicPrefs
import com.msahil432.sms.welcomeActivity.WelcomeActivity
import io.fabric.sdk.android.Fabric

class SplashActivity : AppCompatActivity() {
  var prefs : BasicPrefs? = null
  override fun onCreate(savedInstanceState: Bundle?) {
    Fabric.with(this, Crashlytics())

    prefs = BasicPrefs.getInstance(applicationContext)
    if(prefs!!.darkMode())
      delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    else
      delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

    super.onCreate(savedInstanceState)

    window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
       or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_FULLSCREEN)
    setContentView(R.layout.activity_splash)
  }

  override fun onPostResume() {
    super.onPostResume()
    Handler().postDelayed({
      if(prefs!!.firstRun()){
        startActivity(Intent(applicationContext, WelcomeActivity::class.java))
      } else{
        startActivity(Intent(applicationContext, HomeActivity::class.java))
      }
      finish()
    }, 400)
  }
}
