package com.msahil432.sms.feature.blocked

import android.view.LayoutInflater
import android.view.ViewGroup
import com.msahil432.sms.R
import com.moez.QKSMS.common.base.QkRealmAdapter
import com.moez.QKSMS.common.base.QkViewHolder
import com.msahil432.sms.model.Conversation
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.blocked_list_item.view.*
import javax.inject.Inject

class BlockedAdapter @Inject constructor() : QkRealmAdapter<Conversation>() {

    val unblock: PublishSubject<Long> = PublishSubject.create()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.blocked_list_item, parent, false)
        return QkViewHolder(view).apply {
            view.setOnClickListener {
                val conversation = getItem(adapterPosition)!!
                unblock.onNext(conversation.id)
            }
        }
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        val conversation = getItem(position)!!
        val view = holder.containerView

        view.avatars.contacts = conversation.recipients
        view.title.text = conversation.getTitle()
    }

}