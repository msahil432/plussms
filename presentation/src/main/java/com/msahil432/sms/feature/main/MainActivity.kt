package com.msahil432.sms.feature.main

import android.Manifest
import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewStub
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.core.view.accessibility.AccessibilityEventCompat.setAction
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.textChanges
import com.msahil432.sms.R
import com.moez.QKSMS.common.Navigator
import com.moez.QKSMS.common.base.QkThemedActivity
import com.moez.QKSMS.common.androidxcompat.drawerOpen
import com.moez.QKSMS.common.androidxcompat.scope
import com.moez.QKSMS.common.util.extensions.*
import com.msahil432.sms.feature.conversations.ConversationItemTouchCallback
import com.msahil432.sms.feature.conversations.ConversationsAdapter
import com.msahil432.sms.repository.SyncRepository
import com.uber.autodispose.kotlin.autoDisposable
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.drawer_view.*
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.main_permission_hint.*
import kotlinx.android.synthetic.main.main_syncing.*
import javax.inject.Inject

class MainActivity : QkThemedActivity(), MainView {

    @Inject lateinit var disposables: CompositeDisposable
    @Inject lateinit var navigator: Navigator
    @Inject lateinit var conversationsAdapter: ConversationsAdapter
    @Inject lateinit var drawerBadgesExperiment: DrawerBadgesExperiment
    @Inject lateinit var searchAdapter: SearchAdapter
    @Inject lateinit var itemTouchCallback: ConversationItemTouchCallback
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    override val activityResumedIntent: Subject<Unit> = PublishSubject.create()
    override val queryChangedIntent by lazy { toolbarSearch.textChanges() }
    override val composeIntent by lazy { compose.clicks() }
    override val drawerOpenIntent: Observable<Boolean> by lazy {
        drawerLayout
                .drawerOpen(Gravity.START)
                .doOnNext { dismissKeyboard() }
    }
    override val homeIntent: Subject<Unit> = PublishSubject.create()
    override val drawerItemIntent: Observable<DrawerItem> by lazy {
        Observable.merge(listOf(
                inbox.clicks().map { DrawerItem.INBOX },
                personal_inbox.clicks().map { DrawerItem.PERSONAL },
                ads_inbox.clicks().map { DrawerItem.ADS },
                money_inbox.clicks().map { DrawerItem.FINANCE },
                update_inbox.clicks().map { DrawerItem.UPDATES },
                others_inbox.clicks().map { DrawerItem.OTHERS },
                archived.clicks().map { DrawerItem.ARCHIVED },
                backup.clicks().map { DrawerItem.BACKUP },
                scheduled.clicks().map { DrawerItem.SCHEDULED },
                blocking.clicks().map { DrawerItem.BLOCKING },
                settings.clicks().map { DrawerItem.SETTINGS },
                plus.clicks().map { DrawerItem.PLUS },
                help.clicks().map { DrawerItem.HELP },
                invite.clicks().map { DrawerItem.INVITE }))
    }
    override val optionsItemIntent: Subject<Int> = PublishSubject.create()
    override val plusBannerIntent by lazy { plusBanner.clicks() }
    override val dismissRatingIntent by lazy { rateDismiss.clicks() }
    override val rateIntent by lazy { rateOkay.clicks() }
    override val conversationsSelectedIntent by lazy { conversationsAdapter.selectionChanges }
    override val confirmDeleteIntent: Subject<List<Long>> = PublishSubject.create()
    override val swipeConversationIntent by lazy { itemTouchCallback.swipes }
    override val undoArchiveIntent: Subject<Unit> = PublishSubject.create()
    override val snackbarButtonIntent: Subject<Unit> = PublishSubject.create()
    override val backPressedIntent: Subject<Unit> = PublishSubject.create()

    private val viewModel by lazy { ViewModelProviders.of(this, viewModelFactory)[MainViewModel::class.java] }
    private val toggle by lazy { ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.main_drawer_open_cd, 0) }
    private val itemTouchHelper by lazy { ItemTouchHelper(itemTouchCallback) }
    private val progressAnimator by lazy { ObjectAnimator.ofInt(syncingProgress, "progress", 0, 0) }
    private val archiveSnackbar by lazy {
        Snackbar.make(drawerLayout, R.string.toast_archived, Snackbar.LENGTH_LONG).apply {
            setAction(R.string.button_undo) { undoArchiveIntent.onNext(Unit) }
        }
    }
    private val snackbar by lazy { findViewById<View>(R.id.snackbar) }
    private val syncing by lazy { findViewById<View>(R.id.syncing) }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        viewModel.bindView(this)

        (snackbar as? ViewStub)?.setOnInflateListener { _, _ ->
            snackbarButton.clicks().subscribe(snackbarButtonIntent)
        }

        (syncing as? ViewStub)?.setOnInflateListener { _, _ ->
            syncingProgress?.progressTintList = ColorStateList.valueOf(theme.blockingFirst().theme)
            syncingProgress?.indeterminateTintList = ColorStateList.valueOf(theme.blockingFirst().theme)
        }

        toggle.syncState()
        toolbar.setNavigationOnClickListener {
            dismissKeyboard()
            homeIntent.onNext(Unit)
        }

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Don't allow clicks to pass through the drawer layout
        drawer.clicks().subscribe()

        // Set the theme color tint to the recyclerView, progressbar, and FAB
        theme
                .doOnNext { recyclerView.scrapViews() }
                .autoDisposable(scope())
                .subscribe { theme ->
                    // Set the color for the drawer icons
                    val states = arrayOf(intArrayOf(android.R.attr.state_activated), intArrayOf(-android.R.attr.state_activated))
                    ColorStateList(states, intArrayOf(theme.theme, resolveThemeColor(android.R.attr.textColorSecondary)))
                            .let { tintList ->
                                inboxIcon.imageTintList = tintList
                                personal_inboxIcon.imageTintList = tintList
                                update_inboxIcon.imageTintList = tintList
                                money_inboxIcon.imageTintList = tintList
                                others_inboxIcon.imageTintList = tintList
                                ads_inboxIcon.imageTintList = tintList
                                archivedIcon.imageTintList = tintList
                            }

                    // Miscellaneous views
//                    listOf(plusBadge1, plusBadge2).forEach { badge ->
//                        badge.setBackgroundTint(theme.theme)
//                        badge.setTextColor(theme.textPrimary)
//                    }
                    syncingProgress?.progressTintList = ColorStateList.valueOf(theme.theme)
                    syncingProgress?.indeterminateTintList = ColorStateList.valueOf(theme.theme)
                    plusIcon.setTint(theme.theme)
                    rateIcon.setTint(theme.theme)
                    compose.setBackgroundTint(theme.theme)

                    // Set the FAB compose icon color
                    compose.setTint(theme.textPrimary)
                }

        itemTouchCallback.adapter = conversationsAdapter
        conversationsAdapter.autoScrollToStart(recyclerView)

        swiperefresh.setOnRefreshListener {
            swiperefresh.isRefreshing = false
            toolbarSearch.requestFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(toolbarSearch, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    override fun render(state: MainState) {
        if (state.hasError) {
            finish()
            return
        }

        val markPinned = when (state.page) {
            is Inbox -> state.page.markPinned
            is PersonalInbox -> state.page.markPinned
            is FinanceInbox -> state.page.markPinned
            is UpdatesInbox -> state.page.markPinned
            is OthersInbox -> state.page.markPinned
            is AdsInbox -> state.page.markPinned
            is Archived -> state.page.markPinned
            else -> true
        }

        val markRead = when (state.page) {
            is Inbox -> state.page.markRead
            is PersonalInbox -> state.page.markRead
            is FinanceInbox -> state.page.markRead
            is UpdatesInbox -> state.page.markRead
            is OthersInbox -> state.page.markRead
            is AdsInbox -> state.page.markRead
            is Archived -> state.page.markRead
            else -> true
        }

        val selectedConversations = when (state.page) {
            is Inbox -> state.page.selected
            is PersonalInbox -> state.page.selected
            is FinanceInbox -> state.page.selected
            is UpdatesInbox -> state.page.selected
            is OthersInbox -> state.page.selected
            is AdsInbox -> state.page.selected
            is Archived -> state.page.selected
            else -> 0
        }

        toolbarSearch.setVisible(selectedConversations == 0 || state.page is Searching)
        toolbarTitle.setVisible(toolbarSearch.visibility != View.VISIBLE)

        toolbar.menu.findItem(R.id.archive)?.isVisible = state.page is Inbox && selectedConversations != 0
        toolbar.menu.findItem(R.id.unarchive)?.isVisible = state.page is Archived && selectedConversations != 0
        toolbar.menu.findItem(R.id.delete)?.isVisible = selectedConversations != 0
        toolbar.menu.findItem(R.id.pin)?.isVisible = markPinned && selectedConversations != 0
        toolbar.menu.findItem(R.id.unpin)?.isVisible = !markPinned && selectedConversations != 0
        toolbar.menu.findItem(R.id.read)?.isVisible = markRead && selectedConversations != 0
        toolbar.menu.findItem(R.id.unread)?.isVisible = !markRead && selectedConversations != 0
        toolbar.menu.findItem(R.id.block)?.isVisible = selectedConversations != 0

//        listOf(plusBadge1, plusBadge2).forEach { badge ->
//            badge.isVisible = false
//        }
        plus.isVisible = false
        plusBanner.isVisible = false
        rateLayout.setVisible(false)

        val isConvosVisible = state.page is Inbox || state.page is Archived
                || state.page is PersonalInbox || state.page is FinanceInbox
                || state.page is UpdatesInbox || state.page is OthersInbox
                || state.page is AdsInbox

        compose.setVisible(isConvosVisible)
        conversationsAdapter.emptyView = empty.takeIf { isConvosVisible }

        when (state.page) {
            is Inbox-> {
                showBackButton(state.page.selected > 0)
                title = getString(R.string.main_title_selected, state.page.selected)
                if (recyclerView.adapter !== conversationsAdapter)
                    recyclerView.adapter = conversationsAdapter
                conversationsAdapter.updateData(state.page.data)
                itemTouchHelper.attachToRecyclerView(recyclerView)
                conversationsAdapter.showCategory(true)
                toolbarSearch.setHint(R.string.drawer_all_messages)
                empty.setText(R.string.inbox_empty_text)
            }

            is UpdatesInbox -> {
                showBackButton(state.page.selected > 0)
                title = getString(R.string.main_title_selected, state.page.selected)
                if (recyclerView.adapter !== conversationsAdapter)
                    recyclerView.adapter = conversationsAdapter
                conversationsAdapter.updateData(state.page.data)
                conversationsAdapter.showCategory(false)
                itemTouchHelper.attachToRecyclerView(recyclerView)
                toolbarSearch.setHint(R.string.drawer_updates_sms)
                empty.setText(R.string.inbox_empty_text)
            }
            is FinanceInbox -> {
                showBackButton(state.page.selected > 0)
                title = getString(R.string.main_title_selected, state.page.selected)
                if (recyclerView.adapter !== conversationsAdapter)
                    recyclerView.adapter = conversationsAdapter
                conversationsAdapter.updateData(state.page.data)
                conversationsAdapter.showCategory(false)
                toolbarSearch.setHint(R.string.drawer_money_sms)
                itemTouchHelper.attachToRecyclerView(recyclerView)
                empty.setText(R.string.inbox_empty_text)
            }
            is AdsInbox-> {
                showBackButton(state.page.selected > 0)
                title = getString(R.string.main_title_selected, state.page.selected)
                if (recyclerView.adapter !== conversationsAdapter)
                    recyclerView.adapter = conversationsAdapter
                conversationsAdapter.updateData(state.page.data)
                conversationsAdapter.showCategory(false)
                itemTouchHelper.attachToRecyclerView(recyclerView)
                toolbarSearch.setHint(R.string.drawer_ads_sms)
                empty.setText(R.string.inbox_empty_text)
            }
            is OthersInbox -> {
                showBackButton(state.page.selected > 0)
                title = getString(R.string.main_title_selected, state.page.selected)
                if (recyclerView.adapter !== conversationsAdapter)
                    recyclerView.adapter = conversationsAdapter
                conversationsAdapter.updateData(state.page.data)
                conversationsAdapter.showCategory(false)
                itemTouchHelper.attachToRecyclerView(recyclerView)
                toolbarSearch.setHint(R.string.drawer_other_sms)
                empty.setText(R.string.inbox_empty_text)
            }
            is PersonalInbox-> {
                showBackButton(state.page.selected > 0)
                title = getString(R.string.main_title_selected, state.page.selected)
                if (recyclerView.adapter !== conversationsAdapter)
                    recyclerView.adapter = conversationsAdapter
                conversationsAdapter.updateData(state.page.data)
                conversationsAdapter.showCategory(false)
                itemTouchHelper.attachToRecyclerView(recyclerView)
                empty.setText(R.string.inbox_empty_text)
            }

            is Searching -> {
                showBackButton(true)
                if (recyclerView.adapter !== searchAdapter) recyclerView.adapter = searchAdapter
                searchAdapter.data = state.page.data ?: listOf()
                itemTouchHelper.attachToRecyclerView(null)
                empty.setText(R.string.inbox_search_empty_text)
            }

            is Archived -> {
                conversationsAdapter.showCategory(true)
                showBackButton(state.page.selected > 0)
                title = when (state.page.selected != 0) {
                    true -> getString(R.string.main_title_selected, state.page.selected)
                    false -> getString(R.string.title_archived)
                }
                if (recyclerView.adapter !== conversationsAdapter) recyclerView.adapter = conversationsAdapter
                conversationsAdapter.updateData(state.page.data)
                itemTouchHelper.attachToRecyclerView(null)
                empty.setText(R.string.archived_empty_text)
            }
        }

        inbox.isActivated = state.page is Inbox
        personal_inbox.isActivated = state.page is PersonalInbox
        money_inbox.isActivated = state.page is FinanceInbox
        others_inbox.isActivated = state.page is OthersInbox
        update_inbox.isActivated = state.page is UpdatesInbox
        ads_inbox.isActivated = state.page is AdsInbox
        archived.isActivated = state.page is Archived

        if (drawerLayout.isDrawerOpen(GravityCompat.START) && !state.drawerOpen) drawerLayout.closeDrawer(GravityCompat.START)
        else if (!drawerLayout.isDrawerVisible(GravityCompat.START) && state.drawerOpen) drawerLayout.openDrawer(GravityCompat.START)

        when (state.syncing) {
            is SyncRepository.SyncProgress.Idle -> {
                syncing.isVisible = false
                snackbar.isVisible = !state.defaultSms || !state.smsPermission || !state.contactPermission
            }

            is SyncRepository.SyncProgress.Running -> {
                syncing.isVisible = true
                syncingProgress.max = state.syncing.max
                progressAnimator.apply { setIntValues(syncingProgress.progress, state.syncing.progress) }.start()
                syncingProgress.isIndeterminate = state.syncing.indeterminate
                snackbar.isVisible = false
            }
        }

        when {
            !state.defaultSms -> {
                snackbarTitle?.setText(R.string.main_default_sms_title)
                snackbarMessage?.setText(R.string.main_default_sms_message)
                snackbarButton?.setText(R.string.main_default_sms_change)
            }

            !state.smsPermission -> {
                snackbarTitle?.setText(R.string.main_permission_required)
                snackbarMessage?.setText(R.string.main_permission_sms)
                snackbarButton?.setText(R.string.main_permission_allow)
            }

            !state.contactPermission -> {
                snackbarTitle?.setText(R.string.main_permission_required)
                snackbarMessage?.setText(R.string.main_permission_contacts)
                snackbarButton?.setText(R.string.main_permission_allow)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activityResumedIntent.onNext(Unit)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }

    override fun showBackButton(show: Boolean) {
        toggle.onDrawerSlide(drawer, if (show) 1f else 0f)
        toggle.drawerArrowDrawable.color = when (show) {
            true -> resolveThemeColor(android.R.attr.textColorSecondary)
            false -> resolveThemeColor(android.R.attr.textColorPrimary)
        }
    }

    override fun requestPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.READ_SMS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_CONTACTS), 0)
    }

    override fun clearSearch() {
        dismissKeyboard()
        toolbarSearch.text = null
    }

    override fun clearSelection() {
        conversationsAdapter.clearSelection()
    }

    override fun showDeleteDialog(conversations: List<Long>) {
        val count = conversations.size
        AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(resources.getQuantityString(R.plurals.dialog_delete_message, count, count))
                .setPositiveButton(R.string.button_delete) { _, _ -> confirmDeleteIntent.onNext(conversations) }
                .setNegativeButton(R.string.button_cancel, null)
                .show()
    }

    override fun showArchivedSnackbar() {
        archiveSnackbar.show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        optionsItemIntent.onNext(item.itemId)
        return true
    }

    override fun onBackPressed() {
        backPressedIntent.onNext(Unit)
    }

}