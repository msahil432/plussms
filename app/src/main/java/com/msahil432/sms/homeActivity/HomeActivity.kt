package com.msahil432.sms.homeActivity

import android.app.Activity
import android.content.Context
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.core.view.GravityCompat
import androidx.appcompat.widget.AppCompatTextView
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.msahil432.sms.R
import com.msahil432.sms.SmsApplication
import com.msahil432.sms.common.BaseActivity
import com.msahil432.sms.homeActivity.mainFragment.HomeFragment
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.app_bar_home.*
import android.content.Intent
import android.provider.ContactsContract
import android.provider.Telephony
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import com.msahil432.sms.common.Event
import com.msahil432.sms.conversationActivity.ConversationActivity
import com.msahil432.sms.helpers.ContactHelper
import com.msahil432.sms.helpers.ContactHelper.CleanPhone
import com.msahil432.sms.homeActivity.convoFragment.ConvoFragment
import com.msahil432.sms.settingsActivity.SettingsActivity
import org.greenrobot.eventbus.EventBus
import java.lang.Exception

class HomeActivity : BaseActivity<HomeViewModel>(), NavigationView.OnNavigationItemSelectedListener {

  private var convoFragment= ConvoFragment()
  private var isConvo = false

  private val ContactPickerCode = 1997

  override fun setLayout(): Int { return R.layout.activity_home }

  override fun setViewModelClass(): Class<HomeViewModel>? {
    search_view.attachNavigationDrawerToMenuButton(drawer_layout)
    nav_view.setNavigationItemSelectedListener(this)

    return HomeViewModel::class.java
  }

  override fun initialFragment() : Fragment{
    return HomeFragment()
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    resolveIntent(intent)
  }

  private fun resolveIntent(intent: Intent?){
    log("HomeActivity - Handling Intent")
    try{
      if(intent!!.action == Intent.ACTION_SEND && "text/plain" == intent.type){
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
          viewModel.textSent=it
          fab.performClick()
          log("textSent ${viewModel.textSent}")
        }
      } else if(intent.action == Intent.ACTION_SENDTO){
        var phone = intent.data!!.toString()
        phone = CleanPhone(phone.substring(phone.indexOf(":")+1))
        val body = intent.getStringExtra("sms_body")
        ConversationActivity.OpenThread(getThreadId(phone), "PERSONAL",
          ContactHelper.getName(applicationContext, phone), body, phone, applicationContext)
      }else if(intent.hasExtra("CAT")){
        when (intent.getIntExtra("CAT", -1)){
          Companion.CAT.PERSONAL.type ->{
            nav_view.setCheckedItem(R.id.nav_personal)
            onNavigationItemSelected(nav_view.menu.findItem(R.id.nav_personal))
          }
          Companion.CAT.MONEY.type ->{
            nav_view.setCheckedItem(R.id.nav_money)
            onNavigationItemSelected(nav_view.menu.findItem(R.id.nav_money))
          }
          Companion.CAT.UPDATES.type ->{
            nav_view.setCheckedItem(R.id.nav_updates)
            onNavigationItemSelected(nav_view.menu.findItem(R.id.nav_updates))
          }
          Companion.CAT.ADS.type ->{
            nav_view.setCheckedItem(R.id.nav_ads)
            onNavigationItemSelected(nav_view.menu.findItem(R.id.nav_ads))
          }
          Companion.CAT.OTHERS.type ->{
            nav_view.setCheckedItem(R.id.nav_others)
            onNavigationItemSelected(nav_view.menu.findItem(R.id.nav_others))
          }
        }
      }
    }catch (e: Exception){
      Log.e("HomeActivity", e.message, e)
    }
  }

  override fun attachViewModelListeners(viewModel: HomeViewModel) {}

  override fun doWork() {
    if(!SmsApplication.AmIDefaultApp(applicationContext))
      Snackbar.make(findViewById(R.id.fab),
        getString(R.string.deletion_and_sending_unavailable), Snackbar.LENGTH_LONG)
        .setAction(getString(R.string.fix_this)) {
          startActivity(SmsApplication.MakeDefaultApp())
        }.show()
    fab.setOnClickListener {
      val i = Intent(Intent.ACTION_PICK)
      i.type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
      startActivityForResult(i, ContactPickerCode)
    }

    setupMenuBadges(nav_view.menu)

    convoFragment = ConvoFragment()
    resolveIntent(intent)
  }



  override fun onBackPressed() {
    when {
      drawer_layout.isDrawerOpen(GravityCompat.START) ->
        drawer_layout.closeDrawer(GravityCompat.START)
      isConvo -> {
        replaceFragment(HomeFragment(), "home", false)
        search_view.setSearchHint(getString(R.string.search))
        nav_view.setCheckedItem(R.id.nav_home)
        isConvo=false
      }
      else -> super.onBackPressed()
    }
  }

  private fun setupMenuBadges(menu: Menu){
    viewModel.loadUnreads(getSmsDb()!!)

    viewModel.updatesUnread.observe(this, Observer {
      val t = menu.findItem(R.id.nav_updates).actionView
      if(it.isEmpty())
        t.visibility = View.GONE
      else
        t.findViewById<AppCompatTextView>(R.id.menu_count_text).text = it.size.toString()
    })
    viewModel.adsUnread.observe(this, Observer {
      val t = menu.findItem(R.id.nav_ads).actionView
      if(it.isEmpty())
        t.visibility = View.GONE
      else
        t.findViewById<AppCompatTextView>(R.id.menu_count_text).text = it.size.toString()
    })
    viewModel.moneyUnread.observe(this, Observer {
      val t = menu.findItem(R.id.nav_money).actionView
      if(it.isEmpty())
        t.visibility = View.GONE
      else
        t.findViewById<AppCompatTextView>(R.id.menu_count_text).text = it.size.toString()
    })
    viewModel.othersUnread.observe(this, Observer {
      val t = menu.findItem(R.id.nav_others).actionView
      if(it.isEmpty())
        t.visibility = View.GONE
      else
        t.findViewById<AppCompatTextView>(R.id.menu_count_text).text = it.size.toString()
    })
    viewModel.personalUnread.observe(this, Observer {
      val t = menu.findItem(R.id.nav_personal).actionView
      if(it.isEmpty())
        t.visibility = View.GONE
      else
        t.findViewById<AppCompatTextView>(R.id.menu_count_text).text = it.size.toString()
    })
  }

  private fun loadConvoFor(cat: String){
    viewModel.loadSms(getSmsDb()!!, cat)
    EventBus.getDefault().post(Event.SMS_CONVO_REFRESH)
    if(!isConvo)
      replaceFragment(convoFragment, "convo", false)
    isConvo = true
  }

  override fun onNavigationItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.nav_home ->{
        if(!isConvo)
          return true
        replaceFragment(HomeFragment(), "home", false)
        search_view.setSearchHint(getString(R.string.search))
        isConvo=false
      }
      R.id.nav_money -> {
        loadConvoFor("MONEY")
        search_view.setSearchHint(getString(R.string.search_in)+getString(R.string.money_sms))
      }
      R.id.nav_personal -> {
        loadConvoFor("PERSONAL")
        search_view.setSearchHint(getString(R.string.search_in)+getString(R.string.personal_sms))
      }
      R.id.nav_updates -> {
        loadConvoFor("UPDATES")
        search_view.setSearchHint(getString(R.string.search_in)+getString(R.string.updates_sms))
      }
      R.id.nav_others -> {
        loadConvoFor("OTHERS")
        search_view.setSearchHint(getString(R.string.search_in)+getString(R.string.other_sms))
      }
      R.id.nav_ads -> {
        loadConvoFor("ADS")
        search_view.setSearchHint(getString(R.string.search_in)+getString(R.string.ads_sms))
      }
      R.id.nav_share -> {
        val shareBody = getString(R.string.invite_msg)
        val sharingIntent = Intent(android.content.Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.invite_friends))
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody)
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_using)))
      }
      R.id.nav_settings -> {
        startActivity(Intent(this, SettingsActivity::class.java))
      }
    }

    drawer_layout.closeDrawer(GravityCompat.START)
    return true
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if(requestCode == ContactPickerCode && resultCode == Activity.RESULT_OK){
      val c = contentResolver.query(data!!.data!!, arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
        null, null, null)
      if(c==null || !c.moveToFirst())
        return
      var phone = c.getString(0)
      c.close()
      phone = CleanPhone(phone)
      log(phone)
      if(!ContactHelper.isPhoneNumber(phone))
        return
      log("textSent ${viewModel.textSent}")
      ConversationActivity.OpenThread(getThreadId(phone), "PERSONAL",
        ContactHelper.getName(applicationContext, phone),viewModel.textSent, phone, applicationContext)
      viewModel.textSent = null
      return
    }
    super.onActivityResult(requestCode, resultCode, data)
  }

  private fun getThreadId(phone: String): String{
    val cur = contentResolver.query(Telephony.Sms.CONTENT_URI, null,
      "${Telephony.Sms.ADDRESS}=\"$phone\"", null, null)
    var threadId = "-1"
    if(cur != null && cur.moveToFirst()) {
      threadId = cur.getInt(cur.getColumnIndex(Telephony.Sms.THREAD_ID)).toString()
      cur.close()
    }
    log("threadId- $threadId")
    return threadId
  }

  companion object {

    public enum class CAT(var type: Int){
      PERSONAL(1),
      MONEY(2),
      UPDATES(3),
      ADS(4),
      OTHERS(5)
    }

    public fun openCat(context: Context, cat: CAT): Intent{
      val i = Intent(context, HomeActivity::class.java)
      i.putExtra("CAT", cat.type)
      return i
    }

  }

}
