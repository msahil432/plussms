package com.moez.QKSMS.filter

import android.util.Log
import com.moez.QKSMS.extensions.removeAccents
import com.moez.QKSMS.model.Contact
import javax.inject.Inject

class ContactFilter @Inject constructor(private val phoneNumberFilter: PhoneNumberFilter) : Filter<Contact>() {

    override fun filter(item: Contact, query: CharSequence): Boolean {
        return item.name.removeAccents().contains(query, true) || // Name
                item.numbers.map { it.address }.any { address -> phoneNumberFilter.filter(address, query) } // Number
    }

}