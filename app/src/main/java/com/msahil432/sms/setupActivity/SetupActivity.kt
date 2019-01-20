package com.msahil432.sms.setupActivity

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.IdRes
import android.view.View
import com.github.florent37.androidslidr.Slidr
import com.msahil432.sms.R
import com.transferwise.sequencelayout.SequenceStep
import java.util.*

class SetupActivity : AppCompatActivity() {

  private val starting_step by bind<SequenceStep>(R.id.setup_starting)
  private val collecting_sms_step by bind<SequenceStep>(R.id.setup_collecting_sms)
  private val collected_sms_step by bind<SequenceStep>(R.id.setup_collected_sms)
  private val categorizing_sms_step by bind<SequenceStep>(R.id.setup_categorizing_sms)
  private val done_step by bind<SequenceStep>(R.id.setup_done)

  private val progressBar by bind<Slidr>(R.id.setup_progress)
  private var cat1 = 15
  private var cat2 = 25
  private var cat3 = 35
  private var doneSMS = cat1 + cat2 +cat3
  private var totalSMS = 95

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_setup)

    starting_step.setActive(true)
    starting_step.setAnchor(getString(R.string.now))

    progressBar.max = 100f
    progressBar.currentValue = 0f
    progressBar.setTextFormatter { value: Float -> "${value.toInt()} %" }
    progressBar.setRegionTextFormatter { _, value -> "${value.toInt()} SMS" }
    progressBar.setListener( object : Slidr.Listener{
      override fun bubbleClicked(slidr: Slidr?) {}
      override fun valueChanged(slidr: Slidr?, currentValue: Float) {
        slidr!!.currentValue = doneSMS*100f/totalSMS
      }
    })
  }

  override fun onPostResume() {
    super.onPostResume()

    starting_step.setAnchor(getTime())
    starting_step.setActive(false)

    collecting_sms_step.setAnchor(getString(R.string.now))
    collecting_sms_step.setActive(true)

    progressBar.currentValue = doneSMS*100f/totalSMS
    progressBar.addStep(Slidr.Step("Cat1", cat1.toFloat(),
      Color.parseColor("#007E90"), Color.parseColor("#004E90")))
    progressBar.addStep(Slidr.Step("Cat2", cat2.toFloat()+cat1,
      Color.parseColor("#004E90"), Color.parseColor("#002E90")))
    progressBar.addStep(Slidr.Step("Cat3", cat3.toFloat()+cat2+cat1,
      Color.parseColor("#002E90"), Color.parseColor("#007E90")))
  }

  private fun setProgress(){

  }

  private fun getTime() : String{
    val c = Calendar.getInstance()
    return ""+c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE)
  }

  private fun <T : View> AppCompatActivity.bind(@IdRes res : Int) : Lazy<T> {
    @Suppress("UNCHECKED_CAST")
    return lazy { findViewById<T>(res) }
  }


}
