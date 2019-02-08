package com.msahil432.sms.homeActivity

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.msahil432.sms.common.BaseViewModel
import com.msahil432.sms.database.SMS
import com.msahil432.sms.database.SmsDatabase

/**
 * Created by msahil432
 **/

class HomeViewModel : BaseViewModel(){

  private lateinit var smsList : LiveData<PagedList<SMS>>

  lateinit var adsUnread : LiveData<List<String>>
  lateinit var personalUnread : LiveData<List<String>>
  lateinit var othersUnread : LiveData<List<String>>
  lateinit var moneyUnread : LiveData<List<String>>
  lateinit var updatesUnread : LiveData<List<String>>

  fun loadSms(smsDatabase: SmsDatabase, cat: String){
    val factory: DataSource.Factory<Int, SMS> = smsDatabase.userDao().getForCat(cat)
    val pagedListBuilder= LivePagedListBuilder<Int, SMS>(factory,25)
    smsList = pagedListBuilder.build()
  }

  fun getSMS() = smsList

  fun loadUnreads(smsDatabase: SmsDatabase){
    adsUnread = smsDatabase.userDao().getUnreadCount("PROMOTIONAL")
    personalUnread = smsDatabase.userDao().getUnreadCount("PERSONAL")
    othersUnread = smsDatabase.userDao().getUnreadCount("OTHERS")
    moneyUnread = smsDatabase.userDao().getUnreadCount("MONEY")
    updatesUnread = smsDatabase.userDao().getUnreadCount("UPDATES")
  }

}