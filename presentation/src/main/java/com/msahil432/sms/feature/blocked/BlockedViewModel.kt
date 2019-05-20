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
package com.msahil432.sms.feature.blocked

import android.content.Context
import com.msahil432.sms.common.Navigator
import com.msahil432.sms.common.androidxcompat.scope
import com.msahil432.sms.common.base.QkViewModel
import com.msahil432.sms.interactor.MarkUnblocked
import com.msahil432.sms.manager.AnalyticsManager
import com.msahil432.sms.repository.ConversationRepository
import com.msahil432.sms.util.Preferences
import com.msahil432.sms.util.tryOrNull
import com.uber.autodispose.kotlin.autoDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.withLatestFrom
import javax.inject.Inject

class BlockedViewModel @Inject constructor(
    private val context: Context,
    private val analytics: AnalyticsManager,
    private val conversationRepo: ConversationRepository,
    private val markUnblocked: MarkUnblocked,
    private val navigator: Navigator,
    private val prefs: Preferences
) : QkViewModel<BlockedView, BlockedState>(BlockedState()) {

    init {
        newState { copy(data = conversationRepo.getBlockedConversations()) }

        disposables += prefs.sia.asObservable()
                .subscribe { enabled -> newState { copy(siaEnabled = enabled) } }
    }

    override fun bindView(view: BlockedView) {
        super.bindView(view)

        view.siaClickedIntent
                .map {
                    tryOrNull(false) { context.packageManager.getApplicationInfo("org.mistergroup.shouldianswerpersonal", 0).enabled }
                            ?: tryOrNull(false) { context.packageManager.getApplicationInfo("org.mistergroup.muzutozvednout", 0).enabled }
                            ?: false
                }
                .doOnNext { installed -> if (!installed) navigator.showSia() }
                .withLatestFrom(prefs.sia.asObservable()) { installed, enabled ->
                    analytics.track("Clicked SIA", Pair("enable", !enabled), Pair("installed", installed))
                    installed && !enabled
                }
                .autoDisposable(view.scope())
                .subscribe { shouldEnable -> prefs.sia.set(shouldEnable) }

        // Show confirm unblock conversation dialog
        view.unblockIntent
                .autoDisposable(view.scope())
                .subscribe { threadId -> view.showUnblockDialog(threadId) }

        // Unblock conversation
        view.confirmUnblockIntent
                .autoDisposable(view.scope())
                .subscribe { threadId -> markUnblocked.execute(listOf(threadId)) }
    }

}