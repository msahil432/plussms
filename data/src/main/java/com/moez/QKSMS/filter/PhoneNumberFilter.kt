package com.moez.QKSMS.filter

import android.telephony.PhoneNumberUtils
import javax.inject.Inject

class PhoneNumberFilter @Inject constructor() : Filter<String>() {

    override fun filter(item: String, query: CharSequence): Boolean {
        val allCharactersDialable = query.all { PhoneNumberUtils.isReallyDialable(it) }

        return (PhoneNumberUtils.compare(item, query.toString()) ||
                PhoneNumberUtils.stripSeparators(item).contains(PhoneNumberUtils.stripSeparators(query.toString()), true))
    }

}