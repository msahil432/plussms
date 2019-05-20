package com.msahil432.sms.filter

import com.msahil432.sms.model.Conversation
import javax.inject.Inject

class ConversationFilter @Inject constructor(private val recipientFilter: RecipientFilter) : Filter<Conversation>() {

    override fun filter(item: Conversation, query: CharSequence): Boolean {
        return item.recipients.any { recipient ->
            recipientFilter.filter(recipient, query) }
    }

}