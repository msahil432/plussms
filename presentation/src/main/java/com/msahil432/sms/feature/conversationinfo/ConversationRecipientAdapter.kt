package com.msahil432.sms.feature.conversationinfo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.msahil432.sms.R
import com.moez.QKSMS.common.Navigator
import com.moez.QKSMS.common.base.QkRealmAdapter
import com.moez.QKSMS.common.base.QkViewHolder
import com.moez.QKSMS.common.util.extensions.setVisible
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