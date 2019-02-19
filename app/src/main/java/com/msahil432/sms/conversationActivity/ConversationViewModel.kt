package com.msahil432.sms.conversationActivity

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import androidx.lifecycle.MutableLiveData
import com.msahil432.sms.common.BaseViewModel
import com.msahil432.sms.database.SmsDatabase
import java.lang.Exception
import java.util.ArrayList

/**
 * Created by msahil432
 **/

class ConversationViewModel : BaseViewModel(){

  var smsList = MutableLiveData<ArrayList<Text>>()
  var totalSms = MutableLiveData<Int>()
  lateinit var dbSms : List<String>

  var loading = false
  lateinit var threadId: String

  fun loadSms(smsDatabase: SmsDatabase, threadId: String, cat : String?){
    smsList.value = ArrayList()
    this.threadId = threadId
    WorkThread.execute {
      dbSms = if (cat == null)
        smsDatabase.userDao().getMessagesForThread(threadId)
      else
        smsDatabase.userDao().getMessagesForThread(threadId)
      totalSms.postValue(dbSms.size)
    }
  }

  fun add50Texts(context: Context){
    val currentSms = smsList.value!!
    if(dbSms.size == currentSms.size)
      return
    loading = true
//    WorkThread.execute {
      for (i in currentSms.size .. currentSms.size + 30) {
        try {
          if (i == dbSms.size)
            break
          log("adding more "+dbSms.size)
          val c = context.contentResolver.query(Telephony.Sms.CONTENT_URI, null,
            "${Telephony.Sms._ID}=${dbSms[i]} and ${Telephony.Sms.THREAD_ID}=$threadId",
            null, null
          )
          if (c == null || !c.moveToFirst())
            continue
          currentSms.add(Text(c.getString(c.getColumnIndex("body"))))
          c.close()
        }catch (e:Exception){
          log(e.message!!, e)
        }
      }
      smsList.value = (currentSms)
      loading = false
//    }
  }

}