package com.msahil432.sms.setupActivity

import android.content.Context
import android.provider.Telephony
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.msahil432.sms.common.BaseViewModel
import com.msahil432.sms.database.SMS
import com.msahil432.sms.database.SmsDatabase
import com.msahil432.sms.helpers.ContactHelper
import com.msahil432.sms.helpers.ContactHelper.GetPhone
import com.msahil432.sms.models.ServerMessage
import com.msahil432.sms.models.ServerModel
import com.msahil432.sms.services.BackgroundCategorizationService
import java.lang.Exception

/**
 * Created by msahil432
 **/

class SetupViewModel : BaseViewModel() {

  private val totalSMS = MutableLiveData<Float>()

  private val collectedSms = ArrayList<ServerMessage>()

  private lateinit var context: Context
  private lateinit var database : SmsDatabase

  val networkOk = MutableLiveData<Boolean>()

  fun getTotalSMS() : LiveData<Float> { return totalSMS}

  fun startProcess(context: Context, database: SmsDatabase){
    this.context = context
    this.database = database
    WorkThread.execute{
      BackgroundCategorizationService.pingServer()
      collectSms()
    }
    DownloadThread.execute {
      provideTotalCount()
      log("Starting Personal SMS")
      processPersonalSms()
    }
  }

  private val collectSms = Runnable {

    collectSms()

    if(collectedSms.isEmpty()) {
      return@Runnable
    }

    for(i in 0 .. collectedSms.size/50){
      val tem2 = ServerModel()
      tem2.texts = ArrayList<ServerMessage>()

      for(a in 0 until 50){
        tem2.texts.add(collectedSms[(i*50)+a])
        if((i*50)+a+1 == collectedSms.size)
          break
      }

      var net = false
      var v = ServerModel()
      while(!net) {
        try {
          val call = retrofit.categorizeSMS(tem2).execute()
          if(!call.isSuccessful){
            throw Exception("Unsuccessful Call!")
          }
          v = call.body()!!
          net = true
          networkOk.postValue(true)
        }catch (e: Exception){
          net = false
          networkOk.postValue(false)
        }
      }

      saveToDb(v)
    }

  }

  private fun collectSms(){
    val cursor = context.contentResolver.query(Telephony.Sms.Inbox.CONTENT_URI, null,
      "body IS NOT NULL", null, "date DESC")
    val bodyIndex = cursor!!.getColumnIndex(Telephony.Sms.Inbox.BODY)
    val idIndex = cursor.getColumnIndex(Telephony.Sms.Inbox._ID)
    val threadIndex = cursor.getColumnIndex(Telephony.Sms.Inbox.THREAD_ID)
    val addressIndex = cursor.getColumnIndex(Telephony.Sms.Inbox.ADDRESS)
    if (!cursor.moveToFirst()) {
      return
    }
    do{
      try {
        val temp = ServerMessage()
        temp.id = "t${cursor.getInt(threadIndex)}m${cursor.getInt(idIndex)}"
        try {
          val address = cursor.getString(addressIndex)
          if (database.userDao().getCat(temp.id)[0] == "PERSONAL"
            || ContactHelper.getName(context, address)==address ) {
            continue
          }
        }catch (e: Exception){}
        temp.textMessage = cursor.getString(bodyIndex)
        collectedSms.add(temp)
        if(collectedSms.size==50){
          sendToServer(collectedSms)
          collectedSms.clear()
        }
      }catch (e : Exception){
        e.printStackTrace()
      }
    }while (cursor.moveToNext())
    cursor.close()

    sendToServer(collectedSms)
  }

  private fun provideTotalCount(){
    var tCount = 0
    var c = context.contentResolver.query(Telephony.Sms.Inbox.CONTENT_URI, null,
      "body IS NOT NULL", null, null)
    if(c==null) {
      totalSMS.postValue(collectedSms.size.toFloat())
      return
    }
    tCount += c.count
    c.close()

    c = context.contentResolver.query(Telephony.Sms.Sent.CONTENT_URI, null,
      "body IS NOT NULL", null, null)
    if(c==null) {
      totalSMS.postValue(tCount.toFloat())
      return
    }
    tCount += c.count
    c.close()

    c = context.contentResolver.query(Telephony.Sms.Outbox.CONTENT_URI, null,
      "body IS NOT NULL", null, null)
    if(c==null) {
      totalSMS.postValue(tCount.toFloat())
      return
    }
    tCount += c.count
    c.close()

    totalSMS.postValue(tCount.toFloat())
  }

  private fun sendToServer(collected: ArrayList<ServerMessage>){
    if(collectedSms.size==0)
      return

    val tem2 = ServerModel()
    tem2.texts = collectedSms

    var net = false
    var v = ServerModel()
    while(!net) {
      try {
        val call = retrofit.categorizeSMS(tem2).execute()
        if(!call.isSuccessful){
          throw Exception("Unsuccessful Call!")
        }
        v = call.body()!!
        net = true
        networkOk.postValue(true)
      }catch (e: Exception){
        net = false
        networkOk.postValue(false)
      }
    }
    saveToDb(v)
  }

  private fun saveToDb(v: ServerModel){
    for(t in v.texts) {
      val threadId = t.id.substring(1, t.id.indexOf('m'))
      val mId = t.id.substring(t.id.indexOf('m') + 1)
      val phone = GetPhone(context, threadId)
      val cur2 = context.contentResolver.query(Telephony.Sms.Inbox.CONTENT_URI,
        arrayOf(Telephony.Sms.Inbox.READ, Telephony.Sms.DATE, Telephony.Sms._ID),
        Telephony.Sms._ID + "=" + mId, null, null)
      if(cur2==null || !cur2.moveToFirst())
        continue
      cur2.moveToFirst()
      val read = cur2.getInt(0)
      val timestamp = cur2.getLong(1)
      cur2.close()
      val sms = SMS(t.id, "OTHERS", threadId, mId, t.textMessage, phone, read, timestamp)
      try {
        when (t.cat) {
          "PROMOTIONAL", "PROMO", "ADS" -> {
            sms.cat = "ADS"
          }
          "PERSONAL", "URGENT" -> {
            sms.cat = "PERSONAL"
          }
          "MONEY", "OTP", "BANK" -> {
            sms.cat = "MONEY"
          }
          "UPDATES", "WALLET/APP", "ORDER" -> {
            sms.cat = "UPDATES"
          }
        }
        database.userDao().insertAll(sms)
      }catch (e: Exception){
        log("SetupViewModel - error", e)
      }
    }
  }

  private fun processPersonalSms(){
    var count =0
    var c = context.contentResolver.query(Telephony.Sms.Sent.CONTENT_URI,
      null, null, null, null)
    if(c==null || !c.moveToFirst()){
      return
    }
    log("Sent SMS ${c.count}")
    var threadIndex = c.getColumnIndex(Telephony.Sms.Sent.THREAD_ID)
    var addressIndex = c.getColumnIndex(Telephony.Sms.Sent.ADDRESS)
    var idIndex = c.getColumnIndex(Telephony.Sms.Sent._ID)
    var timeIndex = c.getColumnIndex(Telephony.Sms.Sent.DATE_SENT)
    var sentIndex = c.getColumnIndex(Telephony.Sms.Sent.STATUS)
    var bodyIndex = c.getColumnIndex(Telephony.Sms.Sent.BODY)
    do {
      try {
        val threadId = c.getInt(threadIndex)
        val address = c.getString(addressIndex)
        val mid = c.getInt(idIndex)
        val timestamp = c.getLong(timeIndex)
        val sent = c.getInt(sentIndex)+10
        val sms = SMS("t${threadId}m$mid", "PERSONAL", threadId.toString(), mid.toString(),
          c.getString(bodyIndex), address, sent, timestamp)
        database.userDao().insertAll(sms)
        log("Sent SMS adding ${++count}")
        markAllPersonal(threadId, count)
      }catch (e: Exception){
        log("Personal Sent SMS Error", e)
      }
    }while (c.moveToNext())
    c.close()

    count =0
    c = context.contentResolver.query(Telephony.Sms.Outbox.CONTENT_URI,
      null, null, null, null)
    if(c==null || !c.moveToFirst()){
      return
    }
    log("Outbox SMS ${c.count}")
    threadIndex = c.getColumnIndex(Telephony.Sms.Outbox.THREAD_ID)
    addressIndex = c.getColumnIndex(Telephony.Sms.Outbox.ADDRESS)
    idIndex = c.getColumnIndex(Telephony.Sms.Outbox._ID)
    timeIndex = c.getColumnIndex(Telephony.Sms.Outbox.DATE_SENT)
    sentIndex = c.getColumnIndex(Telephony.Sms.Outbox.STATUS)
    bodyIndex = c.getColumnIndex(Telephony.Sms.Outbox.BODY)
    do {
      try {
        val threadId = c.getInt(threadIndex)
        val address = c.getString(addressIndex)
        val mid = c.getInt(idIndex)
        val timestamp = c.getLong(timeIndex)
        val sent = c.getInt(sentIndex)+20
        val sms = SMS("t${threadId}m$mid", "PERSONAL", threadId.toString(),
          mid.toString(), c.getString(bodyIndex), address, sent, timestamp)
        database.userDao().insertAll(sms)
        log("Outbox SMS adding ${++count}")
        markAllPersonal(threadId, count)
      }catch (e: Exception){
        log("Personal Outbox SMS Error", e)
      }
    }while (c.moveToNext())
    c.close()
  }

  private fun markAllPersonal(threadId: Int, count: Int){
    val cur = context.contentResolver.query(Telephony.Sms.Inbox.CONTENT_URI, null,
      "${Telephony.Sms.Inbox.THREAD_ID}=$threadId", null, null)
    if(cur==null || !cur.moveToFirst())
      return
    log("Outbox SMS adding $count - size: ${cur.count}")
    val addressIndex2 = cur.getColumnIndex(Telephony.Sms.Inbox.ADDRESS)
    val idIndex2 = cur.getColumnIndex(Telephony.Sms.Inbox._ID)
    val timeIndex2 = cur.getColumnIndex(Telephony.Sms.Inbox.DATE)
    val readIndex = cur.getColumnIndex(Telephony.Sms.Inbox.READ)
    val bodyIndex = cur.getColumnIndex(Telephony.Sms.Inbox.BODY)
    var inCont =0
    do {
      try {
        val mId = cur.getInt(idIndex2)
        val address2 = cur.getString(addressIndex2)
        val timestamp2 = cur.getLong(timeIndex2)
        val read = cur.getInt(readIndex)
        val insms = SMS("t${threadId}m$mId", "PERSONAL", threadId.toString(), mId.toString(),
          cur.getString(bodyIndex), address2, read, timestamp2)
        database.userDao().insertAll(insms)
        log("Outbox SMS adding $count - ${++inCont}")
      }catch (e: Exception){
        log("Personal Inbox SMS Error", e)
      }
    }while (cur.moveToNext())
    cur.close()
  }

}