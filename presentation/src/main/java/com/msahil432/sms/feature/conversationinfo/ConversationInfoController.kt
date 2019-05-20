package com.msahil432.sms.feature.conversationinfo

import android.text.InputFilter
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.bluelinelabs.conductor.RouterTransaction
import com.jakewharton.rxbinding2.view.clicks
import com.msahil432.sms.R
import com.moez.QKSMS.common.Navigator
import com.moez.QKSMS.common.QkChangeHandler
import com.moez.QKSMS.common.base.QkController
import com.moez.QKSMS.common.util.extensions.*
import com.moez.QKSMS.common.widget.QkEditText
import com.msahil432.sms.feature.conversationinfo.injection.ConversationInfoModule
import com.msahil432.sms.feature.themepicker.ThemePickerController
import com.msahil432.sms.injection.appComponent
import com.uber.autodispose.kotlin.autoDisposable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.conversation_info_controller.*
import javax.inject.Inject

class ConversationInfoController(val threadId: Long = 0) : QkController<ConversationInfoView, ConversationInfoState, ConversationInfoPresenter>(), ConversationInfoView {

    @Inject override lateinit var presenter: ConversationInfoPresenter
    @Inject lateinit var navigator: Navigator
    @Inject lateinit var recipientAdapter: ConversationRecipientAdapter
    @Inject lateinit var mediaAdapter: ConversationMediaAdapter
    @Inject lateinit var itemDecoration: GridSpacingItemDecoration

    private val nameChangeSubject: Subject<String> = PublishSubject.create()
    private val confirmDeleteSubject: Subject<Unit> = PublishSubject.create()

    init {
        appComponent
                .conversationInfoBuilder()
                .conversationInfoModule(ConversationInfoModule(this))
                .build()
                .inject(this)

        layoutRes = R.layout.conversation_info_controller
    }

    override fun onViewCreated() {
        items.postDelayed({ items?.animateLayoutChanges = true }, 100)

        recipients.adapter = recipientAdapter

        media.adapter = mediaAdapter
        media.addItemDecoration(itemDecoration)

        themedActivity
                ?.theme
                ?.autoDisposable(scope())
                ?.subscribe { recipients?.scrapViews() }
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        presenter.bindIntents(this)
        setTitle(R.string.info_title)
        showBackButton(true)
    }

    override fun nameClicks(): Observable<*> = name.clicks()

    override fun nameChanges(): Observable<String> = nameChangeSubject

    override fun notificationClicks(): Observable<*> = notifications.clicks()

    override fun themeClicks(): Observable<*> = themePrefs.clicks()

    override fun archiveClicks(): Observable<*> = archive.clicks()

    override fun blockClicks(): Observable<*> = block.clicks()

    override fun deleteClicks(): Observable<*> = delete.clicks()

    override fun confirmDelete(): Observable<*> = confirmDeleteSubject

    override fun render(state: ConversationInfoState) {
        if (state.hasError) {
            activity?.finish()
            return
        }

        themedActivity?.threadId?.onNext(state.threadId)
        recipientAdapter.threadId = state.threadId
        recipientAdapter.updateData(state.recipients)

        name.setVisible(state.recipients?.size ?: 0 >= 2)
        name.summary = state.name

        notifications.setVisible(!state.blocked)

        archive.setVisible(!state.blocked)
        archive.title = activity?.getString(when (state.archived) {
            true -> R.string.info_unarchive
            false -> R.string.info_archive
        })

        block.title = activity?.getString(when (state.blocked) {
            true -> R.string.info_unblock
            false -> R.string.info_block
        })

        mediaAdapter.updateData(state.media)
    }

    override fun showNameDialog(name: String) {
        val editText = QkEditText(activity!!).apply {
            val padding = 8.dpToPx(activity!!)
            setPadding(padding * 3, padding, padding * 3, padding)
            setSingleLine(true)
            setHint(R.string.info_name_hint)
            setText(name)
            setHintTextColor(context.resolveThemeColor(android.R.attr.textColorTertiary))
            setTextColor(context.resolveThemeColor(android.R.attr.textColorPrimary))
            filters = arrayOf(InputFilter.LengthFilter(30))
            background = null
        }

        AlertDialog.Builder(activity!!)
                .setTitle(R.string.info_name)
                .setView(editText)
                .setPositiveButton(R.string.button_save) { _, _ -> nameChangeSubject.onNext(editText.text.toString()) }
                .setNegativeButton(R.string.button_cancel, null)
                .show()
    }

    override fun showThemePicker(threadId: Long) {
        router.pushController(RouterTransaction.with(ThemePickerController(threadId))
                .pushChangeHandler(QkChangeHandler())
                .popChangeHandler(QkChangeHandler()))
    }

    override fun showDeleteDialog() {
        AlertDialog.Builder(activity!!)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(resources?.getQuantityString(R.plurals.dialog_delete_message, 1))
                .setPositiveButton(R.string.button_delete) { _, _ -> confirmDeleteSubject.onNext(Unit) }
                .setNegativeButton(R.string.button_cancel, null)
                .show()
    }

}