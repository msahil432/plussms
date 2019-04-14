package com.moez.QKSMS.interactor

import android.telephony.SmsMessage
import com.moez.QKSMS.extensions.mapNotNull
import com.moez.QKSMS.manager.ExternalBlockingManager
import com.moez.QKSMS.manager.NotificationManager
import com.moez.QKSMS.manager.ShortcutManager
import com.moez.QKSMS.model.Message
import com.moez.QKSMS.repository.ConversationRepository
import com.moez.QKSMS.repository.MessageRepository
import io.reactivex.Flowable
import javax.inject.Inject

class ReceiveSms @Inject constructor(
    private val conversationRepo: ConversationRepository,
    private val externalBlockingManager: ExternalBlockingManager,
    private val messageRepo: MessageRepository,
    private val notificationManager: NotificationManager,
    private val updateBadge: UpdateBadge,
    private val shortcutManager: ShortcutManager
) : Interactor<ReceiveSms.Params>() {

    class Params(val subId: Int, val messages: Array<SmsMessage>)

    override fun buildObservable(params: Params, category: String): Flowable<*> {
        var tMessage = Message()
        return Flowable.just(params)
                .filter { it.messages.isNotEmpty() }
                .filter {
                    // Don't continue if the sender is blocked
                    val address = it.messages[0].displayOriginatingAddress
                    !externalBlockingManager.shouldBlock(address).blockingGet()
                }
                .map {
                    val messages = it.messages
                    val address = messages[0].displayOriginatingAddress
                    val time = messages[0].timestampMillis
                    val body: String = messages
                            .mapNotNull { message -> message.displayMessageBody }
                            .reduce { body, new -> body + new }
                    messageRepo.insertReceivedSms(it.subId, address, body, time) // Add the message to the db
                }
                .doOnNext { message ->
                    tMessage = message
                    conversationRepo.updateConversations(message.threadId, category = message.category) } // Update the conversation
                .mapNotNull { message -> conversationRepo.getOrCreateConversation(message.threadId) } // Map message to conversation
                .filter { conversation -> !conversation.blocked } // Don't notify for blocked conversations
                .doOnNext { conversation -> if (conversation.archived) conversationRepo.markUnarchived(conversation.id, category = tMessage.category) } // Unarchive conversation if necessary
                .map { conversation -> conversation.id } // Map to the id because [delay] will put us on the wrong thread
                .doOnNext { threadId -> notificationManager.update(threadId) } // Update the notification
                .doOnNext { shortcutManager.updateShortcuts() } // Update shortcuts
                .flatMap { updateBadge.buildObservable(Unit) } // Update the badge
    }

}