package com.msahil432.sms.conversationActivity

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.msahil432.sms.R
import com.msahil432.sms.database.SMS
import com.msahil432.sms.helpers.ContactHelper
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by msahil432
 **/

class ConvoPagedRecyclerAdapter(val context: Context, val address: String, val name : String) :
    PagedListAdapter<SMS, ConvoPagedRecyclerAdapter.MyViewHolder>
      (com.msahil432.sms.homeActivity.convoFragment.PagedRecyclerAdapter.SmsDiffCallback()){

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
    return if(viewType<10)
     MyViewHolder( LayoutInflater.from(parent.context)
        .inflate(R.layout.item_text_received, parent, false) )
    else
      MyViewHolder( LayoutInflater.from(context)
          .inflate(R.layout.item_text_sent, parent, false) )
  }

  val dateFormat = SimpleDateFormat("EEE, dd MMM yy")
  val timeFormat = SimpleDateFormat("hh:mm a")

  override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
    val text = getItem(position)!!
    if(text.status<10){
      holder.senderImage.setImageBitmap(ContactHelper.GetThumbnail(context, address, name))
    }
    holder.body.text = getItem(position)!!.text
    val date = Date(text.timestamp)
    holder.time.text = timeFormat.format(date)

    val dateFormatted = dateFormat.format(date)
    if(position>0){
      val t = dateFormat.format(Date(getItem(position-1)!!.timestamp))
      if(dateFormatted == t) {
        holder.dateBubble.visibility = View.GONE
        return
      }
    }
    holder.dateBubble.visibility = View.VISIBLE
    holder.textDate.text = dateFormatted
  }

  override fun getItemViewType(position: Int): Int = getItem(position)!!.status

  class MyViewHolder(view: View) : RecyclerView.ViewHolder(view){
    val body = itemView.findViewById<AppCompatTextView>(R.id.text_body)
    val senderImage = itemView.findViewById<AppCompatImageView>(R.id.sender_image)
    val time = itemView.findViewById<AppCompatTextView>(R.id.messageTime)

    val dateBubble = itemView.findViewById<RelativeLayout>(R.id.date_bubble)
    val textDate = itemView.findViewById<AppCompatTextView>(R.id.text_date)
  }

}