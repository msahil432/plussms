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
