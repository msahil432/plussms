package com.msahil432.sms

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import com.moez.QKSMS.common.base.QkThemedActivity
import com.moez.QKSMS.feature.main.MainActivity
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_splash.*

/**
 * Created by msahil432
 **/

class SplashActivity : QkThemedActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_FULLSCREEN)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val night = prefs.night.get()
        val black = prefs.black.get()
        ImageViewCompat.setImageTintList(app_icon, ColorStateList.valueOf(
                ContextCompat.getColor(applicationContext,
                    when(getActivityThemeRes(night, black)){
                        R.style.AppThemeBlack, R.style.AppThemeDark -> R.color.white
                        else -> R.color.black
                    }
                )
            )
        )

        Handler().postDelayed({
            startActivity(Intent(applicationContext, MainActivity::class.java))
            finish()
        }, 800)
    }

}
