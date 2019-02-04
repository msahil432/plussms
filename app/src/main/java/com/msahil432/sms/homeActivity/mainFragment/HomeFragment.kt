package com.msahil432.sms.homeActivity.mainFragment

import com.msahil432.sms.R
import com.msahil432.sms.common.BaseFragment
import com.msahil432.sms.homeActivity.HomeViewModel

class HomeFragment : BaseFragment<HomeViewModel>() {

  override fun setLayout(): Int { return R.layout.fragment_home }

  override fun setViewModelClass(): Class<HomeViewModel>? { return HomeViewModel::class.java }

  override fun attachViewModelListeners(viewModel: HomeViewModel) {

  }

  override fun doWork() {

  }

}
