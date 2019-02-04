package com.msahil432.sms.homeActivity.convoFragment

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.msahil432.sms.R
import com.msahil432.sms.database.SMS

/**
 * Created by msahil432
 **/

class PagedRecyclerAdapter(val context: Context) : PagedListAdapter<SMS, PagedRecyclerAdapter.MyViewHolder>(SmsDiffCallback()){

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
    return MyViewHolder(LayoutInflater.from(context)
      .inflate(R.layout.item_conversation, parent, false))
  }

  override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
    holder.bindData(getItem(position)!!)
  }

  class MyViewHolder(view: View) : RecyclerView.ViewHolder(view){

    var thumbnail = view.findViewById<AppCompatImageView>(R.id.thumbnail)
    var name = view.findViewById<AppCompatTextView>(R.id.convo_name)

    fun bindData(sms: SMS){
      name.text = sms.phone
    }
  }

  class SmsDiffCallback : DiffUtil.ItemCallback<SMS>(){
    override fun areItemsTheSame(oldItem: SMS, newItem: SMS): Boolean {
      return  oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: SMS, newItem: SMS): Boolean {
      return oldItem == newItem
    }
  }

}