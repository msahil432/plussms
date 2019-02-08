package com.msahil432.sms.setupActivity

import android.app.Activity
import android.graphics.Color
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.msahil432.sms.R
import com.msahil432.sms.common.BaseActivity
import com.transferwise.sequencelayout.SequenceStep
import java.util.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.data.PieEntry

class SetupActivity : BaseActivity<SetupViewModel>() {

  private val startingStep by bind<SequenceStep>(R.id.setup_starting)
  private val collectingSmsStep by bind<SequenceStep>(R.id.setup_collecting_sms)
  private val collectedSmsStep by bind<SequenceStep>(R.id.setup_collected_sms)
  private val categorizingSmsStep by bind<SequenceStep>(R.id.setup_categorizing_sms)
  private val doneStep by bind<SequenceStep>(R.id.setup_done)

  private val progressBar by bind<PieChart>(R.id.setup_progress)

  private var totalSMS = 100f
  private var promo = 0f
  private var money = 0f
  private var updates = 0f
  private var others = 0f
  private var pers = 0f

  override fun setLayout(): Int {
    window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
       or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_FULLSCREEN)
    return R.layout.activity_setup
  }

  override fun setViewModelClass(): Class<SetupViewModel> {
    startingStep.setActive(true)
    startingStep.setAnchor(getString(R.string.now))

    progressBar.setUsePercentValues(true)
    progressBar.description.isEnabled = false
    progressBar.setExtraOffsets(5f, 10f, 5f, 5f)

    progressBar.dragDecelerationFrictionCoef = 0.95f
    progressBar.centerText = "SMS"
    progressBar.setCenterTextSize(16f)
    progressBar.setExtraOffsets(10f, 0f, 10f, 0f)
    progressBar.isDrawHoleEnabled = true
    progressBar.holeRadius = 36f
    progressBar.setDrawCenterText(true)
    progressBar.rotationAngle = 0f
    progressBar.isRotationEnabled = true
    progressBar.isHighlightPerTapEnabled = true
//    progressBar.setOnChartValueSelectedListener(this);
    progressBar.animateY(1200, Easing.EaseInOutQuad)
    progressBar.spin(2000, 0f, 270f, Easing.EaseInOutQuad)

    val l = progressBar.legend
    l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
    l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
    l.orientation = Legend.LegendOrientation.VERTICAL
    l.setDrawInside(false)
    l.isEnabled = false

    return SetupViewModel::class.java
  }

  override fun attachViewModelListeners(viewModel: SetupViewModel) {
    viewModel.getTotalSMS().observe(this, Observer<Float> { t ->
      totalSMS = t!!
      smsCollected()
      progressBar.invalidate()
    })
    viewModel.getPersonalSMS().observe(this, Observer<Float> { t ->
      pers = t!!
      updateProgress()
    })
    viewModel.getMoneySMS().observe(this, Observer<Float> { t ->
      money = t!!
      updateProgress()
    })
    viewModel.getPromoSMS().observe(this, Observer<Float> { t ->
      promo = t!!
      updateProgress()
    })
    viewModel.getUpdateSMS().observe(this, Observer<Float> { t ->
      updates = t!!
      updateProgress()
    })
    viewModel.getOtherSMS().observe(this, Observer<Float> { t->
      others = t!!
      updateProgress()
    })
  }

  override fun onBackPressed() {
    Toast.makeText(this, R.string.dont_exit_setup, Toast.LENGTH_LONG).show()
  }

  override fun doWork() {
    startingStep.setAnchor(getTime())
    startingStep.setActive(false)

    collectingSmsStep.setAnchor(getString(R.string.now))
    collectingSmsStep.setActive(true)

    viewModel.startProcess(applicationContext, getSmsDb()!!)
  }

  private fun smsCollected(){
    collectingSmsStep.setActive(false)
    collectingSmsStep.setAnchor("")
    collectedSmsStep.setAnchor(getTime())

    progressBar.centerText = "Total ${totalSMS.toInt()} SMS"

    categorizingSmsStep.setAnchor(getString(R.string.now))
    categorizingSmsStep.setActive(true)
  }

  private fun updateProgress(){

    val entries = ArrayList<PieEntry>()
    val colors = ArrayList<Int>()
    val doneSMS = (updates+others+promo+pers+money)

    if(pers>1){
      if(pers/totalSMS < 0.03)
        entries.add(PieEntry(pers, ""))
      else
        entries.add(PieEntry(pers, getString(R.string.personal_sms)))
      colors.add(ColorTemplate.PASTEL_COLORS[0])
    }
    if(money>1){
      if(money/totalSMS < 0.03)
        entries.add(PieEntry(money, ""))
      else
        entries.add(PieEntry(money, getString(R.string.money_sms)))
      colors.add(ColorTemplate.PASTEL_COLORS[1])
    }
    if(updates>1){
      if(updates/totalSMS < 0.03)
        entries.add(PieEntry(updates, ""))
      else
        entries.add(PieEntry(updates, getString(R.string.updates_sms)))
      colors.add(ColorTemplate.PASTEL_COLORS[2])
    }
    if(promo>1){
      if(promo/totalSMS < 0.03)
        entries.add(PieEntry(promo, ""))
      else
        entries.add(PieEntry(promo, getString(R.string.promotion_sms)))
      colors.add(ColorTemplate.PASTEL_COLORS[3])
    }
    if(others>1){
      if(others/totalSMS < 0.03)
        entries.add(PieEntry(others, ""))
      else
        entries.add(PieEntry(others, getString(R.string.other_sms)))
      colors.add(ColorTemplate.PASTEL_COLORS[4])
    }

    entries.add(PieEntry(totalSMS-doneSMS, "Remaining"))
    colors.add(ColorTemplate.getHoloBlue())

    val dataSet = PieDataSet(entries, "SMS Classes")
    dataSet.sliceSpace = 3f
    dataSet.selectionShift = 5f
    dataSet.colors = colors
    dataSet.valueLinePart1OffsetPercentage = 80f
    dataSet.valueLinePart1Length = 0.2f
    dataSet.valueLinePart2Length = 0.4f
    //dataSet.setUsingSliceColorAsValueLineColor(true);
    dataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE

    val data = PieData(dataSet)
    data.setValueFormatter(PercentFormatter())
    data.setValueTextSize(11f)
    data.setValueTextColor(Color.BLACK)
    progressBar.data = data

    // undo all highlights
    progressBar.highlightValues(null)
    progressBar.notifyDataSetChanged()

    if(doneSMS == totalSMS) {
      categorizingSmsStep.setAnchor(getTime())
      categorizingSmsStep.setActive(false)
      doneStep.setActive(true)
      doneStep.setAnchor(getString(R.string.now))

      Toast.makeText(this, R.string.we_are_done, Toast.LENGTH_LONG).show()
      setResult(Activity.RESULT_OK)
      Handler().postDelayed({ finish() }, 2500)
    }
  }

  private fun getTime() : String{
    val c = Calendar.getInstance()
    val m = c.get(Calendar.MINUTE).toString()
    return if(m.length == 1)
      c.get(Calendar.HOUR_OF_DAY).toString() + ":" + "0"+m
    else
      c.get(Calendar.HOUR_OF_DAY).toString() + ":" + m
  }

}
