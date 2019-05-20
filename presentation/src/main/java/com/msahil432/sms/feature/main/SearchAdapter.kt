package com.msahil432.sms.feature.main

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import com.msahil432.sms.R
import com.msahil432.sms.common.Navigator
import com.msahil432.sms.common.base.QkAdapter
import com.msahil432.sms.common.base.QkViewHolder
import com.msahil432.sms.common.util.Colors
import com.msahil432.sms.common.util.DateFormatter
import com.msahil432.sms.common.util.extensions.setVisible
import com.msahil432.sms.model.SearchResult
import kotlinx.android.synthetic.main.search_list_item.view.*
import javax.inject.Inject

class SearchAdapter @Inject constructor(
    colors: Colors,
    private val context: Context,
    private val dateFormatter: DateFormatter,
    private val navigator: Navigator
) : QkAdapter<SearchResult>() {

    private val highlightColor: Int by lazy { colors.theme().highlight }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.search_list_item, parent, false)
        return QkViewHolder(view).apply {
            view.setOnClickListener {
                val result = getItem(adapterPosition)
                navigator.showConversation(result.conversation.id, result.conversation.category, result.query.takeIf { result.messages > 0 })
            }
        }
    }

    override fun onBindViewHolder(viewHolder: QkViewHolder, position: Int) {
        val previous = data.getOrNull(position - 1)
        val result = getItem(position)
        val view = viewHolder.containerView

        view.resultsHeader.setVisible(result.messages > 0 && previous?.messages == 0)

        val query = result.query
        val title = SpannableString(result.conversation.getTitle())
        var index = title.indexOf(query, ignoreCase = true)

        while (index >= 0) {
            title.setSpan(BackgroundColorSpan(highlightColor), index, index + query.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            index = title.indexOf(query, index + query.length, true)
        }
        view.title.text = title
        view.category.text = result.conversation.category
        view.avatars.contacts = result.conversation.recipients

        when (result.messages == 0) {
            true -> {
                view.date.setVisible(true)
                view.date.text = dateFormatter.getConversationTimestamp(result.conversation.date)
                view.snippet.text = when (result.conversation.me) {
                    true -> context.getString(R.string.main_sender_you, result.conversation.snippet)
                    false -> result.conversation.snippet
                }
            }
            false -> {
                view.date.setVisible(false)
                view.snippet.text = context.getString(R.string.main_message_results, result.messages)
            }
        }
    }

    override fun areItemsTheSame(old: SearchResult, new: SearchResult): Boolean {
        return old.conversation.vid == new.conversation.vid && old.messages > 0 == new.messages > 0
    }

    override fun areContentsTheSame(old: SearchResult, new: SearchResult): Boolean {
        return old.query == new.query && // Queries are the same
                old.conversation.id == new.conversation.id // Conversation id is the same
                && old.messages == new.messages // Result count is the same
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).conversation.vid
    }
}