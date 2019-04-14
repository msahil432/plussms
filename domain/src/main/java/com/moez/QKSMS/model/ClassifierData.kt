package com.moez.QKSMS.model

import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

/**
 * Created by msahil432 on
 **/

@RealmClass
open class ClassifierData: RealmObject(){
    @PrimaryKey var id: Long = 0
    public var text = ""
    public var category = ""
}