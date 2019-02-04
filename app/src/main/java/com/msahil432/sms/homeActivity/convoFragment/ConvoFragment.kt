package com.msahil432.sms.homeActivity.convoFragment

import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.msahil432.sms.R
import com.msahil432.sms.common.BaseFragment
import com.msahil432.sms.common.Event
import com.msahil432.sms.homeActivity.HomeViewModel
import kotlinx.android.synthetic.main.fragment_convo.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ConvoFragment : BaseFragment<HomeViewModel>() {

  private lateinit var adapter : PagedRecyclerAdapter

  override fun setLayout(): Int { return R.layout.fragment_convo  }

  override fun setViewModelClass(): Class<HomeViewModel>? { return HomeViewModel::class.java  }

  override fun attachViewModelListeners(viewModel: HomeViewModel) {
    viewModel.getSMS().observe(this, Observer { sms ->
      if(sms != null) adapter.submitList(sms) })
  }

  override fun doWork() {
    adapter = PagedRecyclerAdapter(context!!)
    convo_list.layoutManager = LinearLayoutManager(context)
    convo_list.adapter = adapter
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public fun subscribe(event: Event){
    viewModel.getSMS().observe(this, Observer { sms ->
      if(sms != null) adapter.submitList(sms) })
  }
}
