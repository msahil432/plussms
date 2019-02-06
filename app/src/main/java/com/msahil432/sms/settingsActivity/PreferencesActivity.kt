package com.msahil432.sms.settingsActivity

import androidx.fragment.app.Fragment
import com.msahil432.sms.R
import com.msahil432.sms.common.BaseActivity

/**
 * Created by msahil432
 **/

public class PreferencesActivity() : BaseActivity<PrefViewModel>(){
  override fun setLayout(): Int {
    return R.layout.activity_empty
  }

  override fun setViewModelClass(): Class<PrefViewModel>? {
    return PrefViewModel::class.java
  }

  override fun initialFragment(): Fragment? {
    return super.initialFragment()
  }

  override fun attachViewModelListeners(viewModel: PrefViewModel) {

  }

  override fun doWork() {

  }

}