package com.msahil432.sms.common

import android.app.ProgressDialog
import android.content.pm.ActivityInfo
import org.greenrobot.eventbus.EventBus
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.annotation.IdRes
import androidx.lifecycle.ViewModelProviders
import com.msahil432.sms.R
import com.msahil432.sms.SmsApplication
import com.msahil432.sms.database.SmsDatabase

/**
 * Created by msahil432
 *
 *     /**
 *      * Flow of execution of methods:
 *      *  1.  SetLayout    - use this for requesting window features etc.
 *      *  2.  Initial Fragment     - Not Recommended for any work
 *      *  3.  setViewModelClass    - use this to initialize variables
 *      *  4.  attachViewModelListeners - observe the data from viewModel
 *      *  5.  doWork   - all work should be done here
 *      *
 *      *  After this, attachViewModelListeners is called on each resume.
 *      *
 *      **/
 *
 *      Loading Dialogs are already provided, so use them.
 *
 * @param <VM> ViewModel of Activity
*/

abstract class BaseActivity<VM : ViewModel> : AppCompatActivity() {

  protected lateinit var viewModel: VM

  protected lateinit var progressDialog: ProgressDialog

  protected abstract fun setLayout(): Int

  protected abstract fun doWork()

  protected abstract fun setViewModelClass(): Class<VM>?

  protected abstract fun attachViewModelListeners(viewModel: VM)

  override fun onCreate(savedInstanceState: Bundle?) {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    super.onCreate(savedInstanceState)
    setContentView(setLayout())

    val fragment = initialFragment()
    if (fragment != null)
      supportFragmentManager.beginTransaction()
        .setCustomAnimations(
          R.anim.slide_in_right, R.anim.slide_out_left,
          R.anim.slide_in_left, R.anim.slide_out_right
        )
        .add(R.id.container, fragment, "initial")
        .commit()

    val t = setViewModelClass()
    if (t != null) {
      viewModel = ViewModelProviders.of(this).get(setViewModelClass()!!)
      attachViewModelListeners(viewModel)
    }
  }

  override fun onPostResume() {
    super.onPostResume()
    if (::viewModel.isInitialized) {
      attachViewModelListeners(viewModel)
    }
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)
    try {
      EventBus.getDefault().register(this)
    } catch (e: Exception) { }
    doWork()
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      onBackPressed()
      return true
    }
    return super.onOptionsItemSelected(item)
  }

  override fun onDestroy() {
    super.onDestroy()
    try {
      EventBus.getDefault().unregister(this)
    }catch (e: Exception){
      Log.e("BaseActivity", "Error with EventBus Subscription")
    }
  }

  protected fun setupActionBar(title: String?, homeAsUp: Boolean) {
    if (supportActionBar == null) return
    if (title != null) supportActionBar!!.title = title
    supportActionBar!!.setDisplayHomeAsUpEnabled(homeAsUp)
  }

  protected open fun initialFragment(): Fragment? {
    return null
  }

  protected fun addFragment(frag: Fragment, tag: String, backStack: Boolean) {
    val t = supportFragmentManager.beginTransaction()
      .setCustomAnimations(
        R.anim.slide_in_right, R.anim.slide_out_left,
        R.anim.slide_in_left, R.anim.slide_out_right
      )
      .add(R.id.container, frag, tag)
    if (backStack)
      t.addToBackStack(tag)
    t.commit()
  }

  protected fun replaceFragment(frag: Fragment, tag: String, backStack: Boolean) {
    val t = supportFragmentManager.beginTransaction()
      .setCustomAnimations(
        R.anim.slide_in_right, R.anim.slide_out_left,
        R.anim.slide_in_left, R.anim.slide_out_right
      )
      .replace(R.id.container, frag, tag)
    if (backStack)
      t.addToBackStack(tag)
    t.commit()
  }

  protected fun showLoading(text: String = getString(R.string.loading)) {
    if (!::progressDialog.isInitialized)
      progressDialog = ProgressDialog(this)
    progressDialog.setMessage(text)
    progressDialog.setCancelable(false)
    progressDialog.show()
  }

  protected fun hideLoading() {
    if (::progressDialog.isInitialized)
      progressDialog.hide()
  }

  protected fun getSmsDb() : SmsDatabase?{
    if(application is SmsApplication)
      return SmsApplication.getSmsDatabase(applicationContext)
    return null
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
