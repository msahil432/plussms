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
package com.msahil432.sms.feature.conversations

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.msahil432.sms.R
import com.msahil432.sms.common.Navigator
import com.msahil432.sms.common.base.QkRealmAdapter
import com.msahil432.sms.common.base.QkViewHolder
import com.msahil432.sms.common.util.DateFormatter
import com.msahil432.sms.common.util.extensions.resolveThemeColor
import com.msahil432.sms.model.Conversation
import com.msahil432.sms.SmsClassifier
import kotlinx.android.synthetic.main.conversation_list_item.view.*
import javax.inject.Inject

class ConversationsAdapter @Inject constructor(
    private val context: Context,
    private val dateFormatter: DateFormatter,
    private val navigator: Navigator
) : QkRealmAdapter<Conversation>() {

    init {
        setHasStableIds(true)
    }

    var showCategory: Boolean = true
    fun showCategory(showCategory: Boolean){
        this.showCategory = showCategory
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.conversation_list_item, parent, false)

        if (viewType == 1) {
            val textColorPrimary = parent.context.resolveThemeColor(android.R.attr.textColorPrimary)

            view.title.setTypeface(view.title.typeface, Typeface.BOLD)

            view.snippet.setTypeface(view.snippet.typeface, Typeface.BOLD)
            view.snippet.setTextColor(textColorPrimary)
            view.snippet.maxLines = 5

            view.date.setTypeface(view.date.typeface, Typeface.BOLD)
            view.date.setTextColor(textColorPrimary)
        }

        return QkViewHolder(view).apply {
            view.setOnClickListener {
                val conversation = getItem(adapterPosition)!!
                when (toggleSelection(conversation.id, false)) {
                    true -> view.isActivated = isSelected(conversation.id)
                    false -> navigator.showConversation(conversation.id, conversation.category)
                }
            }
            view.setOnLongClickListener {
                val conversation = getItem(adapterPosition)!!
                toggleSelection(conversation.id)
                view.isActivated = isSelected(conversation.id)
                true
            }
        }
    }

    override fun onBindViewHolder(viewHolder: QkViewHolder, position: Int) {
        val conversation = getItem(position)!!
        val view = viewHolder.containerView

        view.isActivated = isSelected(conversation.id)

        view.avatars.contacts = conversation.recipients
        view.title.text = conversation.getTitle()
        view.date.text = dateFormatter.getConversationTimestamp(conversation.date)
        view.snippet.text = when (conversation.me) {
            true -> context.getString(R.string.main_sender_you, conversation.snippet)
            false -> conversation.snippet
        }
        view.pinned.isVisible = conversation.pinned
        if(showCategory && conversation.category!=SmsClassifier.NONE_CATEGORY)
            view.category.text = conversation.category
        else
            view.category.visibility = View.GONE
    }

    override fun getItemId(index: Int): Long {
        return getItem(index)!!.vid
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position)!!.read) 0 else 1
    }
}