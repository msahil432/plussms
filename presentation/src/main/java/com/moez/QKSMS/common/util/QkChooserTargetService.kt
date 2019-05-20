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
package com.moez.QKSMS.common.util

import android.content.ComponentName
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.os.Build
import android.service.chooser.ChooserTarget
import android.service.chooser.ChooserTargetService
import android.telephony.PhoneNumberUtils
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import com.msahil432.sms.R
import com.msahil432.sms.feature.compose.ComposeActivity
import com.msahil432.sms.injection.appComponent
import com.msahil432.sms.model.Conversation
import com.msahil432.sms.repository.ConversationRepository
import com.msahil432.sms.util.GlideApp
import com.msahil432.sms.util.tryOrNull
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.M)
class QkChooserTargetService : ChooserTargetService() {

    @Inject lateinit var conversationRepo: ConversationRepository

    override fun onCreate() {
        appComponent.inject(this)
        super.onCreate()
    }

    override fun onGetChooserTargets(targetActivityName: ComponentName?, matchedFilter: IntentFilter?): List<ChooserTarget> {
        return conversationRepo.getTopConversations()
                .take(3)
                .map(this::createShortcutForConversation)
    }

    private fun createShortcutForConversation(conversation: Conversation): ChooserTarget {
        val icon = when {
            conversation.recipients.size == 1 -> {
                val address = conversation.recipients.first()!!.address
                val request = GlideApp.with(this)
                        .asBitmap()
                        .circleCrop()
                        .load(PhoneNumberUtils.stripSeparators(address))
                        .submit()
                val bitmap = tryOrNull(false) { request.get() }

                if (bitmap != null) Icon.createWithBitmap(bitmap)
                else Icon.createWithResource(this, R.mipmap.ic_shortcut_person)
            }

            else -> Icon.createWithResource(this, R.mipmap.ic_shortcut_people)
        }

        val componentName = ComponentName(this, ComposeActivity::class.java)

        return ChooserTarget(conversation.getTitle(), icon, 1f, componentName, bundleOf("threadId" to conversation.id))
    }

}