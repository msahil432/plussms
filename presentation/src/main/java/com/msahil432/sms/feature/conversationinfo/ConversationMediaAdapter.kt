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

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.msahil432.sms.R
import com.msahil432.sms.common.Navigator
import com.msahil432.sms.common.base.QkRealmAdapter
import com.msahil432.sms.common.base.QkViewHolder
import com.msahil432.sms.common.util.extensions.setVisible
import com.msahil432.sms.extensions.isVideo
import com.msahil432.sms.model.MmsPart
import com.msahil432.sms.util.GlideApp
import kotlinx.android.synthetic.main.conversation_media_list_item.view.*
import javax.inject.Inject

class ConversationMediaAdapter @Inject constructor(
    private val context: Context,
    private val navigator: Navigator
) : QkRealmAdapter<MmsPart>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.conversation_media_list_item, parent, false)
        return QkViewHolder(view).apply {
            view.thumbnail.setOnClickListener {
                val part = getItem(adapterPosition)!!
                navigator.showMedia(part.id)
            }
        }
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        val part = getItem(position)!!
        val view = holder.containerView

        GlideApp.with(context)
                .load(part.getUri())
                .fitCenter()
                .into(view.thumbnail)

        view.video.setVisible(part.isVideo())
    }

}