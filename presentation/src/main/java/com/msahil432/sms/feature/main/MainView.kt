package com.msahil432.sms.feature.main

import com.moez.QKSMS.common.base.QkView
import io.reactivex.Observable

interface MainView : QkView<MainState> {

    val activityResumedIntent: Observable<*>
    val queryChangedIntent: Observable<CharSequence>
    val composeIntent: Observable<Unit>
    val drawerOpenIntent: Observable<Boolean>
    val homeIntent: Observable<*>
    val drawerItemIntent: Observable<DrawerItem>
    val optionsItemIntent: Observable<Int>
    val plusBannerIntent: Observable<*>
    val dismissRatingIntent: Observable<*>
    val rateIntent: Observable<*>
    val conversationsSelectedIntent: Observable<List<Long>>
    val confirmDeleteIntent: Observable<List<Long>>
    val swipeConversationIntent: Observable<Pair<Long, Int>>
    val undoArchiveIntent: Observable<Unit>
    val snackbarButtonIntent: Observable<Unit>
    val backPressedIntent: Observable<Unit>

    fun requestPermissions()
    fun clearSearch()
    fun clearSelection()
    fun showDeleteDialog(conversations: List<Long>)
    fun showReportDialog(conversations: List<Long>)
    fun showArchivedSnackbar()

}

enum class DrawerItem { INBOX, ARCHIVED, BACKUP, SCHEDULED, BLOCKING, SETTINGS, PLUS, HELP, INVITE, PERSONAL, OTHERS, ADS, UPDATES, FINANCE }