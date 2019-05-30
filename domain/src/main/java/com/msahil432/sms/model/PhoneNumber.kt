package com.msahil432.sms.model

import io.realm.RealmObject

open class PhoneNumber(
    var address: String = "",
    var type: String = ""
) : RealmObject()