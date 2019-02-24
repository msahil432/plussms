package com.msahil432.sms.homeActivity.convoFragment

import android.content.Context
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.msahil432.sms.R
import com.msahil432.sms.conversationActivity.ConversationActivity
import com.msahil432.sms.database.SMS
import com.msahil432.sms.helpers.ContactHelper
import com.msahil432.sms.helpers.TimeHelper
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.Exception

/**
 * Created by msahil432
 **/

class PagedRecyclerAdapter(val context: Context) : PagedListAdapter<SMS, PagedRecyclerAdapter.MyViewHolder>(SmsDiffCallback()){

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
    return MyViewHolder(LayoutInflater.from(context)
      .inflate(R.layout.item_conversation, parent, false))
  }

  override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
    try {
        val sms = getItem(position)!!
        if(sms.timeAgo == null) {
          sms.timeAgo = TimeHelper.TimeAgo(System.currentTimeMillis()-sms.timestamp)
          getItem(position)!!.timeAgo = sms.timeAgo
        }
        if(sms.name == null) {
          sms.name = ContactHelper.getName(context, sms.phone)
          getItem(position)!!.name = sms.name
        }
        if(sms.thumbnail == null) {
          sms.thumbnail = ContactHelper.GetThumbnail(context, sms.phone, sms.name)
          getItem(position)!!.thumbnail = sms.thumbnail
        }
        holder.bind(getItem(position)!!)
    }catch (e : Exception){
      Log.e("Paged Adapter", "bindViewHolder", e)
    }
  }

  class MyViewHolder(view: View) : RecyclerView.ViewHolder(view){
    init {
      EventBus.getDefault().register(this)
    }

    private var thumbnail = view.findViewById<AppCompatImageView>(R.id.thumbnail)!!
    private var name = view.findViewById<AppCompatTextView>(R.id.convo_name)!!
    private var snippet = view.findViewById<AppCompatTextView>(R.id.snippet)!!
    private var convoTime = view.findViewById<AppCompatTextView>(R.id.convo_time)!!

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun bind(sms: SMS){
      if(sms.status == 0) {
        name.setTypeface(name.typeface, Typeface.BOLD)
        snippet.setTypeface(snippet.typeface, Typeface.BOLD)
        snippet.maxLines = 3
      }else{
        snippet.typeface = Typeface.DEFAULT
        snippet.maxLines = 1
      }
      while(sms.text.contains("\n"))
        sms.text = sms.text.replace("\n", " ")
      thumbnail.setImageBitmap(sms.thumbnail)
      snippet.text = sms.text
      convoTime.text = sms.timeAgo
      name.text = sms.name
      super.itemView.setOnClickListener {
        Log.e("PagedRecycler "+sms.id, sms.name)
        ConversationActivity.OpenThread(sms.threadId, sms.cat, sms.name, sms.phone, it.context)
      }
    }

  }

  class SmsDiffCallback : DiffUtil.ItemCallback<SMS>(){
    override fun areItemsTheSame(oldItem: SMS, newItem: SMS): Boolean {
      return  oldItem.id==newItem.id
    }

    override fun areContentsTheSame(oldItem: SMS, newItem: SMS): Boolean {
      return oldItem.name==newItem.name && oldItem.text==newItem.text && oldItem.timeAgo==newItem.timeAgo
    }
  }

}