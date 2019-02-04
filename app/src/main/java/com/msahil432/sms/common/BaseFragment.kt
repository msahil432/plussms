package com.msahil432.sms.common

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import org.greenrobot.eventbus.EventBus
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel

/**
 * Created by msahil432
 *
 *     /**
 *      * Flow of execution of methods:
 *      *  1.  SetLayout    - use this for requesting window features etc.
 *      *  2.  setViewModelClass    - use this to initialize variables
 *      *  3.  attachViewModelListeners - observe the data from viewModel
 *      *  4.  doWork   - all work should be done here
 *      *
 *      *  After this, attachViewModelListeners is called on each resume.
 *      **/
 *
 *      Loading Dialogs are already provided, so use them.
 *
 * @param <VM> ViewModel of Parent Activity
*/

abstract class BaseFragment<VM : ViewModel> : Fragment() {

  protected lateinit var viewModel: VM

  protected abstract fun setLayout(): Int

  protected abstract fun doWork()

  protected abstract fun setViewModelClass(): Class<VM>?

  protected abstract fun attachViewModelListeners(viewModel: VM)

  override fun onCreateView(inflater: LayoutInflater,
                            container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    val v = inflater.inflate(setLayout(), container, false)
    return v
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    try {
      EventBus.getDefault().register(this)
    } catch (e: Exception) { }
    doWork()
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)

    val t = setViewModelClass()
    if (t != null) {
      viewModel = ViewModelProviders.of(activity!!).get(setViewModelClass()!!)
      attachViewModelListeners(viewModel)
    }
  }

  override fun onResume() {
    super.onResume()
    if (setViewModelClass() != null) {
      attachViewModelListeners(viewModel)
    }
  }

  protected fun <T : View> AppCompatActivity.bind(@IdRes res : Int) : Lazy<T> {
    @Suppress("UNCHECKED_CAST")
    return lazy { findViewById<T>(res) }
  }

  protected fun log(what: String){
    Log.e("msahil432", what)
  }

  protected fun log(what: String, e: Exception){
    Log.e("msahil432", what, e)
  }

}