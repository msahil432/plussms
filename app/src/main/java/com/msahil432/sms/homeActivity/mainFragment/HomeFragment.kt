package com.msahil432.sms.homeActivity.mainFragment

import android.graphics.Color
import androidx.lifecycle.Observer
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.msahil432.sms.R
import com.msahil432.sms.SmsApplication
import com.msahil432.sms.common.BaseFragment
import com.msahil432.sms.homeActivity.HomeViewModel
import kotlinx.android.synthetic.main.fragment_home.*
import java.util.ArrayList
import android.content.Intent
import com.msahil432.sms.setupActivity.SetupActivity


class HomeFragment : BaseFragment<HomeViewModel>() {

  private var totalSMS = 100f
  private var ads = 0f
  private var money = 0f
  private var updates = 0f
  private var others = 0f
  private var pers = 0f

  override fun setLayout(): Int { return R.layout.fragment_home }

  override fun setViewModelClass(): Class<HomeViewModel>? {
    cat_diversity.setUsePercentValues(true)
    cat_diversity.description.isEnabled = false
    cat_diversity.setExtraOffsets(5f, 10f, 5f, 5f)

    cat_diversity.dragDecelerationFrictionCoef = 0.95f
    cat_diversity.centerText = "SMS"
    cat_diversity.setCenterTextSize(16f)
    cat_diversity.setExtraOffsets(10f, 0f, 10f, 0f)
    cat_diversity.isDrawHoleEnabled = true
    cat_diversity.holeRadius = 36f
    cat_diversity.setDrawCenterText(true)
    cat_diversity.rotationAngle = 0f
    cat_diversity.isRotationEnabled = true
    cat_diversity.isHighlightPerTapEnabled = true
//  Todo:   cat_diversity.maxAngle = 180f

//    val typedValue = TypedValue()
//    val theme = context!!.theme
//    theme.resolveAttribute(R.color.white, typedValue, true)
//    @ColorInt val color = typedValue.data
//    cat_diversity.setEntryLabelColor(color)

    cat_diversity.animateY(1200, Easing.EaseInOutQuad)
    cat_diversity.spin(2000, 0f, 270f, Easing.EaseInOutQuad)

    val l = cat_diversity.legend
    l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
    l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
    l.orientation = Legend.LegendOrientation.VERTICAL
    l.setDrawInside(false)
    l.isEnabled = false
    
    return HomeViewModel::class.java
  }

  override fun attachViewModelListeners(viewModel: HomeViewModel) {
    if(activity!!.application is SmsApplication){
      val db = (activity!!.application as SmsApplication).smsDatabase

      db.userDao().getCount().observe(this, Observer {
        totalSMS = it!!.toFloat()
        cat_diversity.centerText = "Total ${totalSMS.toInt()} SMS"
        refreshDiversity()
        if(totalSMS == 0f){
          startActivity(Intent(activity!!, SetupActivity::class.java))
          activity!!.finish()
        }
      })
      db.userDao().getCount("PERSONAL").observe(this, Observer {
        pers = it!!.toFloat()
        refreshDiversity()
      })
      db.userDao().getCount("UPDATES").observe(this, Observer {
        updates = it!!.toFloat()
        refreshDiversity()
      })
      db.userDao().getCount("ADS").observe(this, Observer {
        ads = it!!.toFloat()
        refreshDiversity()
      })
      db.userDao().getCount("MONEY").observe(this, Observer {
        money = it!!.toFloat()
        refreshDiversity()
      })
      db.userDao().getCount("OTHERS").observe(this, Observer {
        others = it!!.toFloat()
        refreshDiversity()
      })


    }
  }

  override fun doWork() {

  }

  private fun refreshDiversity(){
    val entries = ArrayList<PieEntry>()
    val colors = ArrayList<Int>()

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
    if(ads>1){
      if(ads/totalSMS < 0.03)
        entries.add(PieEntry(ads, ""))
      else
        entries.add(PieEntry(ads, getString(R.string.ads_sms)))
      colors.add(ColorTemplate.PASTEL_COLORS[3])
    }
    if(others>1){
      if(others/totalSMS < 0.03)
        entries.add(PieEntry(others, ""))
      else
        entries.add(PieEntry(others, getString(R.string.other_sms)))
      colors.add(ColorTemplate.PASTEL_COLORS[4])
    }

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
    cat_diversity.data = data

    // undo all highlights
    cat_diversity.highlightValues(null)
    cat_diversity.notifyDataSetChanged()
  }

}
