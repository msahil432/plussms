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

public class HomeViewModel : BaseViewModel(){

  private lateinit var smsList : LiveData<PagedList<SMS>>

  public fun loadSms(smsDatabase: SmsDatabase, cat: String){
    val factory: DataSource.Factory<Int, SMS> = smsDatabase.userDao().getForCat(cat)
    val pagedListBuilder= LivePagedListBuilder<Int, SMS>(factory,25)
    smsList = pagedListBuilder.build()
  }

  public fun getSMS() = smsList

}