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
package com.msahil432.sms.repository

import com.msahil432.sms.model.Conversation
import com.msahil432.sms.model.SearchResult
import io.realm.RealmResults

interface ConversationRepository {

    fun getConversations(archived: Boolean = false): RealmResults<Conversation>

    fun getConversations(category: String): RealmResults<Conversation>

    fun getConversationsSnapshot(): List<Conversation>

    /**
     * Returns the top conversations that were active in the last week
     */
    fun getTopConversations(): List<Conversation>

    fun setConversationName(id: Long, name: String)

    fun searchConversations(query: String): List<SearchResult>

    fun getBlockedConversations(): RealmResults<Conversation>

    fun getConversationAsync(threadId: Long): Conversation

    fun getConversation(threadId: Long): Conversation?

    fun getThreadId(recipient: String): Long?

    fun getThreadId(recipients: Collection<String>): Long?

    fun getOrCreateConversation(threadId: Long): Conversation?

    fun getOrCreateConversation(address: String): Conversation?

    fun getOrCreateConversation(addresses: List<String>): Conversation?

    fun saveDraft(threadId: Long, draft: String)

    /**
     * Updates message-related fields in the conversation, like the date and snippet
     */
    fun updateConversations(vararg threadIds: Long)

    fun markArchived(vararg threadIds: Long)

    fun markUnarchived(vararg threadIds: Long)

    fun markPinned(vararg threadIds: Long)

    fun markUnpinned(vararg threadIds: Long)

    fun markBlocked(vararg threadIds: Long)

    fun markUnblocked(vararg threadIds: Long)

    fun deleteConversations(vararg threadIds: Long)

}