package com.msahil432.sms

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.msahil432.sms.common.BaseActivity
import com.transferwise.sequencelayout.SequenceStep
import dagger.android.AndroidInjection
import java.util.*

/**
 * Created by msahil432
 **/

class CategorizationActivity : BaseActivity<CatViewModel>() {

    private val startingStep by bind<SequenceStep>(R.id.setup_starting)
    private val collectingSmsStep by bind<SequenceStep>(R.id.setup_collecting_sms)
    private val collectedSmsStep by bind<SequenceStep>(R.id.setup_collected_sms)
    private val categorizingSmsStep by bind<SequenceStep>(R.id.setup_categorizing_sms)
    private val doneStep by bind<SequenceStep>(R.id.setup_done)

    private val progressBar by bind<PieChart>(R.id.setup_progress)

    private var totalSMS = 3000f
    private var ads = 0f
    private var money = 0f
    private var updates = 0f
    private var others = 0f
    private var pers = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_FULLSCREEN)
        super.onCreate(savedInstanceState)
    }

    override fun setLayout(): Int = R.layout.activity_setup

    override fun setViewModelClass(): Class<CatViewModel> {
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

        return CatViewModel::class.java
    }

    override fun attachViewModelListeners(viewModel: CatViewModel) {
        viewModel.total.observe(this, Observer<Int> { t ->
            totalSMS = t!!.toFloat()
            smsCollected()
            updateProgress()
        })
        viewModel.personal.observe(this, Observer<Int> { t ->
            pers = t!!.toFloat()
            updateProgress()
        })
        viewModel.money.observe(this, Observer<Int> { t ->
            money = t!!.toFloat()
            updateProgress()
        })
        viewModel.ads.observe(this, Observer<Int> { t ->
            ads = t!!.toFloat()
            updateProgress()
        })
        viewModel.updates.observe(this, Observer<Int> { t ->
            calledFinish=false
            updates = t!!.toFloat()
            updateProgress()
        })
        viewModel.others.observe(this, Observer<Int> { t ->
            others = t!!.toFloat()
            updateProgress()
        })
        viewModel.status.observe(this, Observer {
            if(it==-1){
                showLoading(getString(R.string.waiting_for_internet))
            }else if (it==1){
                workFinished()
            }else{
                hideLoading()
            }
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

        viewModel.startProcess(applicationContext)
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
        val doneSMS = (updates+others+ads+pers+money)

        if(pers>1){
            if(pers/totalSMS < 0.03)
                entries.add(PieEntry(pers, ""))
            else
                entries.add(PieEntry(pers, getString(R.string.drawer_personal_messages)))
            colors.add(ColorTemplate.PASTEL_COLORS[0])
        }
        if(money>1){
            if(money/totalSMS < 0.03)
                entries.add(PieEntry(money, ""))
            else
                entries.add(PieEntry(money, getString(R.string.drawer_money_sms)))
            colors.add(ColorTemplate.PASTEL_COLORS[1])
        }
        if(updates>1){
            if(updates/totalSMS < 0.03)
                entries.add(PieEntry(updates, ""))
            else
                entries.add(PieEntry(updates, getString(R.string.drawer_updates_sms)))
            colors.add(ColorTemplate.PASTEL_COLORS[2])
        }
        if(ads>1){
            if(ads/totalSMS < 0.03)
                entries.add(PieEntry(ads, ""))
            else
                entries.add(PieEntry(ads, getString(R.string.drawer_ads_sms)))
            colors.add(ColorTemplate.PASTEL_COLORS[4])
        }
        if(others>1){
            if(others/totalSMS < 0.03)
                entries.add(PieEntry(others, ""))
            else
                entries.add(PieEntry(others, getString(R.string.drawer_other_sms)))
            colors.add(ColorTemplate.PASTEL_COLORS[3])
        }

        if(totalSMS-doneSMS>1) {
            entries.add(PieEntry(totalSMS - doneSMS, "Remaining"))
            colors.add(ColorTemplate.getHoloBlue())
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
        progressBar.data = data

        // undo all highlights
        progressBar.highlightValues(null)
        progressBar.notifyDataSetChanged()
    }

    private fun workFinished(){
        val doneSMS = (updates+others+ads+pers+money)
        if(doneSMS >= totalSMS && !calledFinish) {
            calledFinish = true
            categorizingSmsStep.setAnchor(getTime())
            categorizingSmsStep.setActive(false)
            doneStep.setActive(true)
            doneStep.setAnchor(getString(R.string.now))

            Toast.makeText(this, R.string.we_are_done, Toast.LENGTH_LONG).show()
            setResult(Activity.RESULT_OK)
            Handler().postDelayed({ finish() }, 1000)
        }
    }

    var calledFinish = true

    private fun getTime() : String{
        val c = Calendar.getInstance()
        val m = c.get(Calendar.MINUTE).toString()
        return if(m.length == 1)
            c.get(Calendar.HOUR_OF_DAY).toString() + ":" + "0"+m
        else
            c.get(Calendar.HOUR_OF_DAY).toString() + ":" + m
    }

}
