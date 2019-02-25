package com.msahil432.sms.conversationActivity

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.inputmethodservice.InputMethodService
import android.net.Uri
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.msahil432.sms.R
import com.msahil432.sms.common.BaseActivity
import com.msahil432.sms.common.Event
import com.msahil432.sms.database.SMS
import com.msahil432.sms.helpers.ContactHelper
import kotlinx.android.synthetic.main.activity_conversation.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.Exception

class ConversationActivity : BaseActivity<ConversationViewModel>(), SearchView.OnQueryTextListener {

  private lateinit var personName: String
  private lateinit var address: String
  private lateinit var category: String
  private lateinit var threadId: String
  private var textSent : String? = null
  private var isPhone = false

  override fun setLayout(): Int = R.layout.activity_conversation

  override fun setViewModelClass(): Class<ConversationViewModel>? {
    personName = intent.getStringExtra("name")
    address = intent.getStringExtra("address")
    category = intent.getStringExtra("cat")
    threadId = intent.getStringExtra("threadId")
    textSent = intent.getStringExtra("text")
    isPhone = ContactHelper.isPhoneNumber(address)

    texts_list.layoutManager = LinearLayoutManager(applicationContext,
      RecyclerView.VERTICAL, true)

    val t = ConvoPagedRecyclerAdapter(applicationContext, address, personName)
    texts_list.adapter = t
    val factory: DataSource.Factory<Int, SMS> = getSmsDb()!!.userDao().getSmsForThread(threadId)
    val pagedListBuilder= LivePagedListBuilder<Int, SMS>(factory,25)
    pagedListBuilder.build().observe(this, Observer {
      if(it!=null)  t.submitList(it)
    })

    return ConversationViewModel::class.java
  }

  override fun attachViewModelListeners(viewModel: ConversationViewModel) {

  }

  override fun doWork() {

    if(!isPhone){
      send_fab.visibility = View.GONE
      msg_box.visibility = View.GONE
    }else{
      if(textSent!=null)
        msg_text.setText(textSent)
      msg_text.requestFocus()
      try{
        ( getSystemService(Context.INPUT_METHOD_SERVICE)
           as InputMethodManager ).showSoftInput(msg_text, InputMethodManager.SHOW_IMPLICIT)
      }catch (e: Exception){
        log("showing keyboard", e)
      }
    }
    log("textSent $textSent")
    setupActionBar(personName, true)
    supportActionBar!!.setBackgroundDrawable(
      ColorDrawable(ContactHelper.colorGenerator.getColor(address))
    )
    send_fab.backgroundTintList =
      ColorStateList.valueOf(ContactHelper.colorGenerator.getColor(address))
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  fun newMessageArrived(event: Event){
    if(event==Event.NEW_SMS_RECEIVED) {

    }else if(event==Event.LOAD_MORE_TEXTS){

    }
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.activity_conversation_menu, menu)
    val searchView = menu?.findItem(R.id.convo_search)?.actionView as SearchView
    searchView.setOnCloseListener {
      true
    }
    searchView.setOnQueryTextListener(this)
    return super.onCreateOptionsMenu(menu)
  }

  override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
    if(!isPhone){
      menu!!.findItem(R.id.convo_call).isVisible = false
      menu.findItem(R.id.convo_open_contact).isVisible = false
    }
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when(item.itemId){
      R.id.convo_call ->{
        startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$address")))
      }
      R.id.convo_open_contact ->{
        ContactHelper.viewContact(applicationContext, address)
      }
      android.R.id.home, R.id.homeAsUp ->{
        onBackPressed()
      }
      R.id.convo_delete, R.id.convo_report_cat ->
        Toast.makeText(applicationContext, "Not Yet Implemented", Toast.LENGTH_SHORT).show()
    }
    return true
  }

  override fun onQueryTextSubmit(query: String?): Boolean {
    log("search submit: $query")
    return true
  }

  var last = 0L
  override fun onQueryTextChange(newText: String?): Boolean {
    if(last+500<System.currentTimeMillis()) {
      last = System.currentTimeMillis()
      log("search: $newText")
    }
    return true
  }

  companion object {

    fun OpenThread(threadId : String, cat: String, name: String,
                   text: String?, address: String, context: Context){
      val i = Intent(context, ConversationActivity::class.java)
      i.putExtra("cat", cat)
      i.putExtra("threadId", threadId)
      i.putExtra("address", address)
      i.putExtra("name", name)
      if(text!=null)
        i.putExtra("text", text)
      i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
      context.startActivity(i)
    }

  }

}
