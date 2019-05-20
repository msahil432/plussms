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
package com.msahil432.sms.feature.main

import com.msahil432.sms.model.Conversation
import com.msahil432.sms.model.SearchResult
import com.msahil432.sms.repository.SyncRepository
import io.realm.RealmResults

data class MainState(
    val hasError: Boolean = false,
    val page: MainPage = Inbox(),
    val drawerOpen: Boolean = false,
    val upgraded: Boolean = true,
    val showRating: Boolean = false,
    val syncing: SyncRepository.SyncProgress = SyncRepository.SyncProgress.Idle(),
    val defaultSms: Boolean = true,
    val smsPermission: Boolean = true,
    val contactPermission: Boolean = true
)

sealed class MainPage

data class Inbox(
    val markPinned: Boolean = true,
    val markRead: Boolean = false,
    val data: RealmResults<Conversation>? = null,
    val selected: Int = 0
) : MainPage()

data class PersonalInbox(
        val markPinned: Boolean = true,
        val markRead: Boolean = false,
        val data: RealmResults<Conversation>? = null,
        val selected: Int = 0
) : MainPage()

data class UpdatesInbox(
        val markPinned: Boolean = true,
        val markRead: Boolean = false,
        val data: RealmResults<Conversation>? = null,
        val selected: Int = 0
) : MainPage()

data class AdsInbox(
        val markPinned: Boolean = true,
        val markRead: Boolean = false,
        val data: RealmResults<Conversation>? = null,
        val selected: Int = 0
) : MainPage()

data class OthersInbox(
        val markPinned: Boolean = true,
        val markRead: Boolean = false,
        val data: RealmResults<Conversation>? = null,
        val selected: Int = 0
) : MainPage()

data class FinanceInbox(
        val markPinned: Boolean = true,
        val markRead: Boolean = false,
        val data: RealmResults<Conversation>? = null,
        val selected: Int = 0
) : MainPage()

data class Searching(
    val loading: Boolean = false,
    val data: List<SearchResult>? = null
) : MainPage()

data class Archived(
    val markPinned: Boolean = true,
    val markRead: Boolean = false,
    val data: RealmResults<Conversation>? = null,
    val selected: Int = 0
) : MainPage()