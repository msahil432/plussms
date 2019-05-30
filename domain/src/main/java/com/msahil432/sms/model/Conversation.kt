package com.msahil432.sms.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey

open class Conversation(
    @PrimaryKey var vid : Long =0,
    var id: Long = 0,
    @Index var archived: Boolean = false,
    @Index var blocked: Boolean = false,
    @Index var pinned: Boolean = false,
    var recipients: RealmList<Recipient> = RealmList(),
    var count: Int = 0,
    var date: Long = 0,
    var snippet: String = "",
    var read: Boolean = true,
    var me: Boolean = false,
    var draft: String = "",
    var category: String = "",

    // For group chats, the user is allowed to set a custom title for the conversation
    var name: String = ""
) : RealmObject() {

    fun getTitle(): String {
        return name.takeIf { it.isNotBlank() } ?: recipients.joinToString { recipient -> recipient.getDisplayName() }
    }
}
