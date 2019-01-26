package com.msahil432.sms.homeActivity

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.core.view.GravityCompat
import androidx.core.view.MenuItemCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.msahil432.sms.R
import com.msahil432.sms.SmsApplication
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.app_bar_home.*

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_home)
    setSupportActionBar(toolbar)

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

    val toggle = ActionBarDrawerToggle(
      this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
    )
    drawer_layout.addDrawerListener(toggle)
    toggle.syncState()

    nav_view.setNavigationItemSelectedListener(this)

    setupMenuBadges(nav_view.menu)
  }

  override fun onBackPressed() {
    if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
      drawer_layout.closeDrawer(GravityCompat.START)
    } else {
      super.onBackPressed()
    }
  }

  private fun setupMenuBadges(menu: Menu){
    val banking = menu.findItem(R.id.nav_banking).actionView
    banking.findViewById<AppCompatTextView>(R.id.menu_count_text).text = "1"
    banking.visibility = View.GONE
  }

  override fun onNavigationItemSelected(item: MenuItem): Boolean {
    // Handle navigation view item clicks here.
    when (item.itemId) {

    }

    drawer_layout.closeDrawer(GravityCompat.START)
    return true
  }
}
