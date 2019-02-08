package com.msahil432.sms

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatDelegate
import android.view.View
import com.msahil432.sms.homeActivity.HomeActivity
import com.msahil432.sms.services.BackgroundCategorizationService
import com.msahil432.sms.settingsActivity.BasicPrefs
import com.msahil432.sms.setupActivity.SetupActivity
import com.msahil432.sms.welcomeActivity.WelcomeActivity

class SplashActivity : AppCompatActivity() {
  var prefs : BasicPrefs? = null
  val INTRO_CODE = 1504
  val SETUP_CODE = 1404

  override fun onCreate(savedInstanceState: Bundle?) {
//    Fabric.with(this, Crashlytics())

    prefs = BasicPrefs.getInstance(applicationContext)
    if(prefs!!.darkMode())
      delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    else
      delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
    super.onCreate(savedInstanceState)

    window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
       or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_FULLSCREEN)
    setContentView(R.layout.activity_splash)

    Handler().postDelayed({
      if(prefs!!.firstRun()){
        startActivityForResult(Intent(applicationContext, WelcomeActivity::class.java), INTRO_CODE)
      } else if(!prefs!!.setupDone()){
        startActivityForResult(Intent(applicationContext, SetupActivity::class.java), SETUP_CODE)
      } else{
        startActivity(Intent(applicationContext, HomeActivity::class.java))
        finish()
      }
    }, 800)
    val i = Intent(applicationContext, BackgroundCategorizationService::class.java)
    i.putExtra(BackgroundCategorizationService.PARAM_TIMESTAMP, -1L)
    applicationContext.startService(i)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if(requestCode == INTRO_CODE){
      if(resultCode == Activity.RESULT_OK){
        prefs!!.setFirstRun()
        startActivityForResult(Intent(applicationContext, SetupActivity::class.java), SETUP_CODE)
      }else
        finish()
    }else if (requestCode == SETUP_CODE){
      if(resultCode == Activity.RESULT_OK) {
        prefs!!.setSetup()
        startActivity(Intent(this, HomeActivity::class.java))
      }
      finish()
    }
  }
}
