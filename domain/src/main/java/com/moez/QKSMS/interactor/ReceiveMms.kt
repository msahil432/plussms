package com.moez.QKSMS.interactor

import android.net.Uri
import com.moez.QKSMS.extensions.mapNotNull
import com.moez.QKSMS.manager.ActiveConversationManager
import com.moez.QKSMS.manager.ExternalBlockingManager
import com.moez.QKSMS.manager.NotificationManager
import com.moez.QKSMS.repository.ConversationRepository
import com.moez.QKSMS.repository.MessageRepository
import com.moez.QKSMS.repository.SyncRepository
import io.reactivex.Flowable
import javax.inject.Inject

class ReceiveMms @Inject constructor(
    private val activeConversationManager: ActiveConversationManager,
    private val conversationRepo: ConversationRepository,
    private val externalBlockingManager: ExternalBlockingManager,
    private val syncManager: SyncRepository,
    private val messageRepo: MessageRepository,
    private val notificationManager: NotificationManager,
    private val updateBadge: UpdateBadge
) : Interactor<Uri>() {

    override fun buildObservable(params: Uri, DoNotUse: String): Flowable<*> {
        val category = "Personal"
        return Flowable.just(params)
                .mapNotNull(syncManager::syncMessage) // Sync the message
                .doOnNext { message ->
                    // TODO: Ideally this is done when we're saving the MMS to ContentResolver
                    // This change can be made once we move the MMS storing code to the Data module
                    if (activeConversationManager.getActiveConversation() == message.threadId) {
                        messageRepo.markRead(message.threadId, category = category)
                    }
                }
                .filter { message ->
                    // Because we use the smsmms library for receiving and storing MMS, we'll need
                    // to check if it should be blocked after we've pulled it into realm. If it
                    // turns out that it should be blocked, then delete it
                    // TODO Don't store blocked messages in the first place
                    !externalBlockingManager.shouldBlock(message.address).blockingGet().also { blocked ->
                        if (blocked) messageRepo.deleteMessages(message.id, category = category)
                    }
                }
                .doOnNext { message ->
                    conversationRepo.updateConversations(message.threadId, category = category) } // Update the conversation
                .mapNotNull { message ->
                    conversationRepo.getOrCreateConversation(message.threadId) } // Map message to conversation
                .filter { conversation -> !conversation.blocked } // Don't notify for blocked conversations
                .doOnNext { conversation ->
                    if (conversation.archived)
                        conversationRepo.markUnarchived(conversation.id, category = category) } // Unarchive conversation if necessary
                .map { conversation -> conversation.id } // Map to the id because [delay] will put us on the wrong thread
                .doOnNext(notificationManager::update) // Update the notification
                .flatMap { updateBadge.buildObservable(Unit) } // Update the badge
    }

}