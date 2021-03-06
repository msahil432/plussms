package com.msahil432.sms.repository

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.provider.Telephony
import android.telephony.PhoneNumberUtils
import android.telephony.SmsManager
import android.widget.Toast
import com.google.android.mms.ContentType
import com.google.android.mms.MMSPart
import com.klinker.android.send_message.SmsManagerFactory
import com.klinker.android.send_message.StripAccents
import com.klinker.android.send_message.Transaction
import com.msahil432.sms.compat.TelephonyCompat
import com.msahil432.sms.extensions.anyOf
import com.msahil432.sms.manager.ActiveConversationManager
import com.msahil432.sms.manager.KeyManager
import com.msahil432.sms.model.Attachment
import com.msahil432.sms.model.Conversation
import com.msahil432.sms.model.Message
import com.msahil432.sms.model.MmsPart
import com.msahil432.sms.receiver.SmsDeliveredReceiver
import com.msahil432.sms.receiver.SmsSentReceiver
import com.msahil432.sms.service.SendSmsService
import com.msahil432.sms.util.ImageUtils
import com.msahil432.sms.util.Preferences
import com.msahil432.sms.util.tryOrNull
import com.msahil432.sms.SmsClassifier
import com.msahil432.sms.SmsClassifier.Companion.CATEGORY_PERSONAL
import com.msahil432.sms.common.JavaHelper
import io.realm.Case
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepositoryImpl @Inject constructor(
    private val activeConversationManager: ActiveConversationManager,
    private val context: Context,
    private val messageIds: KeyManager,
    private val imageRepository: ImageRepository,
    private val prefs: Preferences,
    private val syncRepository: SyncRepository
) : MessageRepository {

    override fun getMessages(threadId: Long, query: String, category: String): RealmResults<Message> {
        return Realm.getDefaultInstance()
                .where(Message::class.java)
                .equalTo("threadId", threadId)
                .let { if (query.isEmpty()) it else it.contains("body", query, Case.INSENSITIVE) }
                .let { if (category.isEmpty() || category=="") it else it.equalTo("category", category) }
                .sort("date").findAllAsync()
    }

    override fun getMessage(id: Long): Message? {
        return Realm.getDefaultInstance()
                .where(Message::class.java)
                .equalTo("id", id)
                .findFirst()
    }

    override fun getMessageForPart(id: Long): Message? {
        return Realm.getDefaultInstance()
                .where(Message::class.java)
                .equalTo("parts.id", id)
                .findFirst()
    }

    override fun getUnreadCount(): Long {
        return Realm.getDefaultInstance()
                .where(Conversation::class.java)
                .equalTo("archived", false)
                .equalTo("blocked", false)
                .equalTo("read", false)
                .count()
    }

    override fun getPart(id: Long): MmsPart? {
        return Realm.getDefaultInstance()
                .where(MmsPart::class.java)
                .equalTo("id", id)
                .findFirst()
    }

    override fun getPartsForConversation(threadId: Long): RealmResults<MmsPart> {
        return Realm.getDefaultInstance()
                .where(MmsPart::class.java)
                .equalTo("messages.threadId", threadId)
                .beginGroup()
                .contains("type", "image/")
                .or()
                .contains("type", "video/")
                .endGroup()
                .sort("id", Sort.DESCENDING)
                .findAllAsync()
    }

    /**
     * Retrieves the list of messages which should be shown in the notification
     * for a given conversation
     */
    override fun getUnreadUnseenMessages(threadId: Long): RealmResults<Message> {
        return Realm.getDefaultInstance()
                .also { it.refresh() }
                .where(Message::class.java)
                .equalTo("seen", false)
                .equalTo("read", false)
                .equalTo("threadId", threadId)
                .sort("date")
                .findAll()
    }

    override fun getUnreadUnseenMessages(threadId: Long, category: String): RealmResults<Message> {
        return Realm.getDefaultInstance()
                .also { it.refresh() }
                .where(Message::class.java)
                .equalTo("category", category)
                .equalTo("seen", false)
                .equalTo("read", false)
                .equalTo("threadId", threadId)
                .sort("date")
                .findAll()
    }

    override fun getUnreadMessages(threadId: Long): RealmResults<Message> {
        return Realm.getDefaultInstance()
                .where(Message::class.java)
                .equalTo("read", false)
                .equalTo("threadId", threadId)
                .sort("date")
                .findAll()
    }

    override fun markAllSeen() {
        val realm = Realm.getDefaultInstance()
        val messages = realm.where(Message::class.java).equalTo("seen", false).findAll()
        realm.executeTransaction { messages.forEach { message -> message.seen = true } }
        realm.close()
    }

    override fun markSeen(threadId: Long) {
        val realm = Realm.getDefaultInstance()
        val messages = realm.where(Message::class.java)
                .equalTo("threadId", threadId)
                .equalTo("seen", false)
                .findAll()

        realm.executeTransaction {
            messages.forEach { message ->
                message.seen = true
            }
        }
        realm.close()
    }

    override fun markRead(vararg threadIds: Long) {
        Realm.getDefaultInstance()?.use { realm ->
            val messages = realm.where(Message::class.java)
                    .anyOf("threadId", threadIds)
                    .beginGroup()
                    .equalTo("read", false)
                    .or()
                    .equalTo("seen", false)
                    .endGroup()
                    .findAll()

            realm.executeTransaction {
                messages.forEach { message ->
                    message.seen = true
                    message.read = true
                }
            }
        }

        val values = ContentValues()
        values.put(Telephony.Sms.SEEN, true)
        values.put(Telephony.Sms.READ, true)

        threadIds.forEach { threadId ->
            try {
                val uri = ContentUris.withAppendedId(Telephony.MmsSms.CONTENT_CONVERSATIONS_URI, threadId)
                context.contentResolver.update(uri, values, "${Telephony.Sms.READ} = 0", null)
            } catch (exception: Exception) {
                Timber.w(exception)
            }
        }
    }

    override fun markUnread(vararg threadIds: Long) {
        Realm.getDefaultInstance()?.use { realm ->
            val conversation = realm.where(Conversation::class.java)
                    .anyOf("id", threadIds)
                    .equalTo("read", true)
                    .findAll()

            realm.executeTransaction {
                conversation.forEach { it.read = false }
            }
        }
    }

    override fun sendMessage(subId: Int, threadId: Long, addresses: List<String>,
                             body: String, attachments: List<Attachment>, delay: Int) {
        if (addresses.size == 1 && attachments.isEmpty()) { // SMS
            val address = PhoneNumberUtils.stripSeparators(addresses.first())
            if(!JavaHelper.validPhoneNumber(address)){
                Toast.makeText(context, "Invalid Address", Toast.LENGTH_LONG).show()
                return
            }
            if (delay > 0) { // With delay
                val sendTime = System.currentTimeMillis() + delay
                val message = insertSentSms(subId, threadId, addresses.first(), body, sendTime)

                val intent = getIntentForDelayedSms(message.id)

                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, sendTime, intent)
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, sendTime, intent)
                }
            } else { // No delay
                val message = insertSentSms(subId, threadId, addresses.first(), body, System.currentTimeMillis())
                sendSms(message)
            }
        } else { // MMS
            Toast.makeText(context, "This message will be sent as a MMS", Toast.LENGTH_LONG).show()
            
            val parts = arrayListOf<MMSPart>()

            if (body.isNotBlank()) {
                parts += MMSPart("text", ContentType.TEXT_PLAIN, body.toByteArray())
            }

            // Add the GIFs as attachments
            parts += attachments
                    .mapNotNull { attachment -> attachment as? Attachment.Image }
                    .filter { attachment -> attachment.isGif(context) }
                    .mapNotNull { attachment -> attachment.getUri() }
                    .map { uri -> ImageUtils.compressGif(context, uri, prefs.mmsSize.get() * 1024) }
                    .map { bitmap -> MMSPart("image", ContentType.IMAGE_GIF, bitmap) }

            // Compress the images and add them as attachments
            var totalImageBytes = 0
            parts += attachments
                    .mapNotNull { attachment -> attachment as? Attachment.Image }
                    .filter { attachment -> !attachment.isGif(context) }
                    .mapNotNull { attachment -> attachment.getUri() }
                    .mapNotNull { uri -> tryOrNull { imageRepository.loadImage(uri) } }
                    .also { totalImageBytes = it.sumBy { it.allocationByteCount } }
                    .map { bitmap ->
                        val byteRatio = bitmap.allocationByteCount / totalImageBytes.toFloat()
                        ImageUtils.compressBitmap(bitmap, (prefs.mmsSize.get() * 1024 * byteRatio).toInt())
                    }
                    .map { bitmap -> MMSPart("image", ContentType.IMAGE_JPEG, bitmap) }

            // Send contacts
            parts += attachments
                    .mapNotNull { attachment -> attachment as? Attachment.Contact }
                    .map { attachment -> attachment.vCard.toByteArray() }
                    .map { vCard -> MMSPart("contact", ContentType.TEXT_VCARD, vCard) }

            val transaction = Transaction(context)
            transaction.sendNewMessage(subId, threadId, addresses.map(PhoneNumberUtils::stripSeparators), parts, null)
        }
    }

    override fun sendSms(message: Message) {
        
        
        val smsManager = message.subId.takeIf { it != -1 }
                ?.let(SmsManagerFactory::createSmsManager)
                ?: SmsManager.getDefault()

        val parts = smsManager.divideMessage(if (prefs.unicode.get()) StripAccents.stripAccents(message.body) else message.body)
                ?: arrayListOf()

        val sentIntents = parts.map {
            context.registerReceiver(SmsSentReceiver(), IntentFilter(SmsSentReceiver.ACTION))
            val intent = Intent(SmsSentReceiver.ACTION).putExtra("id", message.id)
            PendingIntent.getBroadcast(context, message.id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val deliveredIntents = parts.map {
            context.registerReceiver(SmsDeliveredReceiver(), IntentFilter(SmsDeliveredReceiver.ACTION))
            val intent = Intent(SmsDeliveredReceiver.ACTION).putExtra("id", message.id)
            val pendingIntent = PendingIntent.getBroadcast(context, message.id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
            if (prefs.delivery.get()) pendingIntent else null
        }

        smsManager.sendMultipartTextMessage(message.address, null, parts, ArrayList(sentIntents), ArrayList(deliveredIntents))
    }

    override fun cancelDelayedSms(id: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(getIntentForDelayedSms(id))
    }

    private fun getIntentForDelayedSms(id: Long): PendingIntent {
        val intent = Intent(context, SendSmsService::class.java).putExtra("id", id)
        return PendingIntent.getService(context, id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun insertSentSms(subId: Int, threadId: Long, address: String, body: String, date: Long): Message {

        // Insert the message to Realm
        val message = Message().apply {
            this.threadId = threadId
            this.address = address
            this.body = body
            this.date = date
            this.subId = subId
            this.category = CATEGORY_PERSONAL

            id = messageIds.newId()
            boxId = Telephony.Sms.MESSAGE_TYPE_OUTBOX
            type = "sms"
            read = true
            seen = true
        }
        val realm = Realm.getDefaultInstance()
        var managedMessage: Message? = null
        realm.executeTransaction { managedMessage = realm.copyToRealmOrUpdate(message) }

        // Insert the message to the native content provider
        val values = ContentValues().apply {
            put(Telephony.Sms.ADDRESS, address)
            put(Telephony.Sms.BODY, body)
            put(Telephony.Sms.DATE, System.currentTimeMillis())
            put(Telephony.Sms.READ, true)
            put(Telephony.Sms.SEEN, true)
            put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_OUTBOX)
            put(Telephony.Sms.THREAD_ID, threadId)
            put(Telephony.Sms.SUBSCRIPTION_ID, subId)
        }
        val uri = context.contentResolver.insert(Telephony.Sms.CONTENT_URI, values)

        // Update the contentId after the message has been inserted to the content provider
        // The message might have been deleted by now, so only proceed if it's valid
        //
        // We do this after inserting the message because it might be slow, and we want the message
        // to be inserted into Realm immediately. We don't need to do this after receiving one
        realm.executeTransaction { managedMessage?.takeIf { it.isValid }?.contentId = uri.lastPathSegment.toLong() }
        realm.close()

        // On some devices, we can't obtain a threadId until after the first message is sent in a
        // conversation. In this case, we need to update the message's threadId after it gets added
        // to the native ContentProvider
        if (threadId == 0L) {
            uri?.let(syncRepository::syncMessage)
        }

        return message
    }

    override fun insertReceivedSms(subId: Int, address: String, body: String, sentTime: Long): Message {

        val realm = Realm.getDefaultInstance()
        val previous = realm.where(Message::class.java)
                .equalTo("address", address).findAll()
                .takeIf { it.size>0 }

        // Insert the message to Realm
        val message = Message().apply {
            this.address = address
            this.body = body
            this.dateSent = sentTime
            this.date = System.currentTimeMillis()
            this.subId = subId

            id = messageIds.newId()
            threadId = TelephonyCompat.getOrCreateThreadId(context, address)
            boxId = Telephony.Sms.MESSAGE_TYPE_INBOX
            type = "sms"
            category = if (isMe() || JavaHelper.getContactName(address, context)!=address
                            || (previous!=null && previous[0]?.category == CATEGORY_PERSONAL))
                                CATEGORY_PERSONAL
                        else SmsClassifier.classify(body)
            read = activeConversationManager.getActiveConversation() == threadId
        }
        var managedMessage: Message? = null
        realm.executeTransaction { managedMessage = realm.copyToRealmOrUpdate(message) }

        // Insert the message to the native content provider
        val values = ContentValues().apply {
            put(Telephony.Sms.ADDRESS, address)
            put(Telephony.Sms.BODY, body)
            put(Telephony.Sms.DATE_SENT, sentTime)
            put(Telephony.Sms.SUBSCRIPTION_ID, subId)
        }

        context.contentResolver.insert(Telephony.Sms.Inbox.CONTENT_URI, values)?.let { uri ->
            // Update the contentId after the message has been inserted to the content provider
            realm.executeTransaction { managedMessage?.contentId = uri.lastPathSegment.toLong() }
        }

        realm.close()

        return message
    }

    /**
     * Marks the message as sending, in case we need to retry sending it
     */
    override fun markSending(id: Long) {
        Realm.getDefaultInstance().use { realm ->
            realm.refresh()

            val message = realm.where(Message::class.java).equalTo("id", id).findFirst()
            message?.let {
                // Update the message in realm
                realm.executeTransaction {
                    message.boxId = Telephony.Sms.MESSAGE_TYPE_OUTBOX
                }

                // Update the message in the native ContentProvider
                val values = ContentValues()
                values.put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_OUTBOX)
                context.contentResolver.update(message.getUri(), values, null, null)
            }
        }
    }

    override fun markSent(id: Long) {
        Realm.getDefaultInstance().use { realm ->
            realm.refresh()

            val message = realm.where(Message::class.java).equalTo("id", id).findFirst()
            message?.let {
                // Update the message in realm
                realm.executeTransaction {
                    message.boxId = Telephony.Sms.MESSAGE_TYPE_SENT
                }

                // Update the message in the native ContentProvider
                val values = ContentValues()
                values.put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_SENT)
                context.contentResolver.update(message.getUri(), values, null, null)
            }
        }
    }

    override fun markFailed(id: Long, resultCode: Int) {
        Realm.getDefaultInstance().use { realm ->
            realm.refresh()

            val message = realm.where(Message::class.java).equalTo("id", id).findFirst()
            message?.let {
                // Update the message in realm
                realm.executeTransaction {
                    message.boxId = Telephony.Sms.MESSAGE_TYPE_FAILED
                    message.errorCode = resultCode
                }

                // Update the message in the native ContentProvider
                val values = ContentValues()
                values.put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_FAILED)
                values.put(Telephony.Sms.ERROR_CODE, resultCode)
                context.contentResolver.update(message.getUri(), values, null, null)
            }
        }
    }

    override fun markDelivered(id: Long) {
        Realm.getDefaultInstance().use { realm ->
            realm.refresh()

            val message = realm.where(Message::class.java).equalTo("id", id).findFirst()
            message?.let {
                // Update the message in realm
                realm.executeTransaction {
                    message.deliveryStatus = Telephony.Sms.STATUS_COMPLETE
                    message.dateSent = System.currentTimeMillis()
                    message.read = true
                }

                // Update the message in the native ContentProvider
                val values = ContentValues()
                values.put(Telephony.Sms.STATUS, Telephony.Sms.STATUS_COMPLETE)
                values.put(Telephony.Sms.DATE_SENT, System.currentTimeMillis())
                values.put(Telephony.Sms.READ, true)
                context.contentResolver.update(message.getUri(), values, null, null)
            }
        }
    }

    override fun markDeliveryFailed(id: Long, resultCode: Int) {
        Realm.getDefaultInstance().use { realm ->
            realm.refresh()

            val message = realm.where(Message::class.java).equalTo("id", id).findFirst()
            message?.let {
                // Update the message in realm
                realm.executeTransaction {
                    message.deliveryStatus = Telephony.Sms.STATUS_FAILED
                    message.dateSent = System.currentTimeMillis()
                    message.read = true
                    message.errorCode = resultCode
                }

                // Update the message in the native ContentProvider
                val values = ContentValues()
                values.put(Telephony.Sms.STATUS, Telephony.Sms.STATUS_FAILED)
                values.put(Telephony.Sms.DATE_SENT, System.currentTimeMillis())
                values.put(Telephony.Sms.READ, true)
                values.put(Telephony.Sms.ERROR_CODE, resultCode)
                context.contentResolver.update(message.getUri(), values, null, null)
            }
        }
    }

    override fun deleteMessages(vararg messageIds: Long) {
        Realm.getDefaultInstance().use { realm ->
            realm.refresh()

            val messages = realm.where(Message::class.java)
                    .anyOf("id", messageIds)
                    .findAll()

            val uris = messages.map { it.getUri() }

            realm.executeTransaction { messages.deleteAllFromRealm() }

            uris.forEach { uri -> context.contentResolver.delete(uri, null, null) }
        }
    }

}