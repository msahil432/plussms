package com.msahil432.sms.welcomeActivity

import android.Manifest
import android.os.Bundle
import com.heinrichreimersoftware.materialintro.app.IntroActivity
import com.msahil432.sms.R
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide



class WelcomeActivity : IntroActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    isButtonBackVisible = false
    isFullscreen = true

    addSlide(
      SimpleSlide.Builder()
        .title(R.string.welcome)
        .description(R.string.welcome_description)
        .image(R.drawable.icon)
        .background(R.color.white)
//        .backgroundDark(R.color.colorPrimaryDark)
        .scrollable(true)
        .build())

    addSlide(
      SimpleSlide.Builder()
        .title(R.string.sms_permission)
        .description(R.string.sms_description)
        .image(R.drawable.icon)
        .background(R.color.white)
//        .backgroundDark(R.color.colorPrimaryDark)
        .scrollable(true)
        .permissions(arrayOf(
          Manifest.permission.READ_SMS,
          Manifest.permission.RECEIVE_SMS,
          Manifest.permission.SEND_SMS))
        .build())

    addSlide(
      SimpleSlide.Builder()
        .title(R.string.contacts_permission)
        .description(R.string.contacts_description)
        .image(R.drawable.icon)
        .background(R.color.white)
//        .backgroundDark(R.color.colorPrimaryDark)
        .scrollable(true)
        .permission(Manifest.permission.READ_CONTACTS)
        .build())

    addSlide(
      SimpleSlide.Builder()
        .title("Plus SMS")
        .description(getString(R.string.lets_go))
        .image(R.drawable.icon)
        .background(R.color.white)
//        .backgroundDark(R.color.colorPrimaryDark)
        .scrollable(true)
        .build())
  }
}
