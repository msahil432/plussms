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
        .title("Plus SMS")
        .description("Welcome")
        .image(R.drawable.icon)
        .background(R.color.white)
//        .backgroundDark(R.color.colorPrimaryDark)
        .scrollable(true)
//        .permission(Manifest.permission.CAMERA)
        .build())

    addSlide(
      SimpleSlide.Builder()
        .title("Plus SMS")
        .description("SMS Permission")
        .image(R.drawable.icon)
        .background(R.color.white_shaded)
//        .backgroundDark(R.color.colorPrimaryDark)
        .scrollable(true)
        .permission(Manifest.permission.SEND_SMS)
        .build())

    addSlide(
      SimpleSlide.Builder()
        .title("Plus SMS")
        .description("Contacts Permission")
        .image(R.drawable.icon)
        .background(R.color.white_shaded)
//        .backgroundDark(R.color.colorPrimaryDark)
        .scrollable(true)
        .permission(Manifest.permission.READ_CONTACTS)
        .build())

    addSlide(
      SimpleSlide.Builder()
        .title("Plus SMS")
        .description("All Done! Let's Go.")
        .image(R.drawable.icon)
        .background(R.color.white)
//        .backgroundDark(R.color.colorPrimaryDark)
        .scrollable(true)
        .build())
  }
}
