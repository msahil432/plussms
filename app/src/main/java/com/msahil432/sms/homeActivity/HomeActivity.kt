package com.msahil432.sms.homeActivity

import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.core.view.GravityCompat
import androidx.appcompat.widget.AppCompatTextView
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.msahil432.sms.R
import com.msahil432.sms.SmsApplication
import com.msahil432.sms.common.BaseActivity
import com.msahil432.sms.homeActivity.mainFragment.HomeFragment
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.app_bar_home.*
import android.content.Intent
import com.msahil432.sms.common.Event
import com.msahil432.sms.homeActivity.convoFragment.ConvoFragment
import com.msahil432.sms.settingsActivity.SettingsActivity
import org.greenrobot.eventbus.EventBus

class HomeActivity : BaseActivity<HomeViewModel>(), NavigationView.OnNavigationItemSelectedListener {

  private lateinit var convoFragment: ConvoFragment
  private var isConvo = false

  override fun setLayout(): Int { return R.layout.activity_home }

  override fun setViewModelClass(): Class<HomeViewModel>? {
    search_view.attachNavigationDrawerToMenuButton(drawer_layout)
    nav_view.setNavigationItemSelectedListener(this)

    return HomeViewModel::class.java
  }

  override fun initialFragment() : Fragment{
    title = getString(R.string.home)
    return HomeFragment()
  }

  override fun attachViewModelListeners(viewModel: HomeViewModel) {  }

  override fun doWork() {
    if(!SmsApplication.AmIDefaultApp(applicationContext))
      Snackbar.make(findViewById(R.id.fab),
        getString(R.string.deletion_and_sending_unavailable), Snackbar.LENGTH_LONG)
        .setAction(getString(R.string.fix_this)) {
          startActivity(SmsApplication.MakeDefaultApp())
        }.show()

    fab.setOnClickListener { view ->
      Snackbar.make(view, "To be implemented", Snackbar.LENGTH_LONG)
        .setAction("Action", null).show()
    }
    setupMenuBadges(nav_view.menu)

    convoFragment = ConvoFragment()
  }

  override fun onBackPressed() {
    if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
      drawer_layout.closeDrawer(GravityCompat.START)
    } else if(isConvo){
      replaceFragment(HomeFragment(), "home", false)
      search_view.setSearchHint(getString(R.string.search))
      isConvo=false
    }else {
      super.onBackPressed()
    }
  }

  private fun setupMenuBadges(menu: Menu){
    val banking = menu.findItem(R.id.nav_money).actionView
    banking.findViewById<AppCompatTextView>(R.id.menu_count_text).text = "1"
//    banking.visibility = View.GONE
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
        onBackPressed()
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
        loadConvoFor("PROMOTIONAL")
        search_view.setSearchHint(getString(R.string.search_in)+getString(R.string.promotion_sms))
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
}
