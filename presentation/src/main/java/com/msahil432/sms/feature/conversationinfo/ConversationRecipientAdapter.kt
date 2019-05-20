/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.msahil432.sms.feature.conversationinfo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.msahil432.sms.R
import com.msahil432.sms.common.Navigator
import com.msahil432.sms.common.base.QkRealmAdapter
import com.msahil432.sms.common.base.QkViewHolder
import com.msahil432.sms.common.util.extensions.setVisible
import com.msahil432.sms.model.Recipient
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.conversation_recipient_list_item.view.*
import javax.inject.Inject

class ConversationRecipientAdapter @Inject constructor(
    private val navigator: Navigator
) : QkRealmAdapter<Recipient>() {

    var threadId: Long = 0L

    private val disposables = CompositeDisposable()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.conversation_recipient_list_item, parent, false)
        return QkViewHolder(view).apply {
            view.setOnClickListener {
                val recipient = getItem(adapterPosition)!!
                if (recipient.contact == null) {
                    navigator.addContact(recipient.address)
                } else {
                    view.avatar.callOnClick()
                }
            }
        }
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        val recipient = getItem(position)!!
        val view = holder.containerView

        view.avatar.threadId = threadId
        view.avatar.setContact(recipient)

        view.name.text = recipient.contact?.name ?: recipient.address

        view.address.text = recipient.address
        view.address.setVisible(recipient.contact != null)

        view.add.setVisible(recipient.contact == null)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        disposables.clear()
    }

}