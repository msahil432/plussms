package com.msahil432.sms.setupActivity

import android.content.Context
import android.provider.Telephony
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.msahil432.sms.common.BaseViewModel
import com.msahil432.sms.database.SMS
import com.msahil432.sms.database.SmsDatabase
import com.msahil432.sms.helpers.ContactHelper
import com.msahil432.sms.helpers.SmsHelper.*
import com.msahil432.sms.models.ServerMessage
import com.msahil432.sms.models.ServerModel
import com.msahil432.sms.services.BackgroundCategorizationService
import java.lang.Exception

/**
 * Created by msahil432
 **/

class SetupViewModel : BaseViewModel() {

  private val totalSMS = MutableLiveData<Float>()
  private val personalSMS = MutableLiveData<Int>()
  private val moneySMS = MutableLiveData<Float>()
  private val adsSMS = MutableLiveData<Float>()
  private val updateSMS = MutableLiveData<Float>()
  private val otherSMS = MutableLiveData<Float>()

  private val collectedSms = ArrayList<ServerMessage>()

  private lateinit var context: Context
  private lateinit var database : SmsDatabase

  val networkOk = MutableLiveData<Boolean>()

  init {
    personalSMS.value = 0
    moneySMS.value = 0f
    adsSMS.value = 0f
    updateSMS.value = 0f
    otherSMS.value = 0f
  }

  fun getTotalSMS() : LiveData<Float> { return totalSMS}
  fun getPersonalSMS() : LiveData<Int> { return personalSMS}
  fun getMoneySMS() : LiveData<Float> { return moneySMS}
  fun getAdsSMS() : LiveData<Float> { return adsSMS}
  fun getUpdateSMS() : LiveData<Float> { return updateSMS}
  fun getOtherSMS() : LiveData<Float> { return otherSMS }

  fun startProcess(context: Context, database: SmsDatabase){
    this.context = context
    this.database = database
    WorkThread.execute(collectSms)
  }

  private val collectSms = Runnable {
    BackgroundCategorizationService.pingServer()
    collectSms()

    if(collectedSms.isEmpty()) {
      personalSMS.postValue(0)
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

      processPersonalSms()
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
        val address = cursor.getString(addressIndex)
        if(address != ContactHelper.getName(context, address))
          continue
        val temp = ServerMessage()
        temp.id = "t${cursor.getInt(threadIndex)}m${cursor.getInt(idIndex)}"
        temp.textMessage = cursor.getString(bodyIndex)
        collectedSms.add(temp)
      }catch (e : Exception){
        e.printStackTrace()
      }
    }while (cursor.moveToNext())
    totalSMS.postValue(cursor.count.toFloat())
    cursor.close()
  }

  private var pers = 0
  private var money = 0f
  private var updates = 0f
  private var ads = 0f
  private var others = 0f

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
      val sms = SMS(t.id, "OTHERS", threadId, mId, phone, read, timestamp)
      try {
        when (t.cat) {
          "PROMOTIONAL", "PROMO", "ADS" -> {
            ads++
            adsSMS.postValue(ads)
            sms.cat = "ADS"
          }
          "PERSONAL", "URGENT" -> {
            pers++
            personalSMS.postValue(pers)
            sms.cat = "PERSONAL"
          }
          "MONEY", "OTP", "BANK" -> {
            money++
            moneySMS.postValue(money)
            sms.cat = "MONEY"
          }
          "UPDATES", "WALLET/APP", "ORDER" -> {
            updates++
            updateSMS.postValue(updates)
            sms.cat = "UPDATES"
          }
          else -> {
            others++
            otherSMS.postValue(others)
          }
        }
        database.userDao().insertAll(sms)
      }catch (e: Exception){
        log("SetupViewModel - error", e)
      }
    }
  }

  private fun processPersonalSms(){
    val c = context.contentResolver.query(Telephony.Sms.Outbox.CONTENT_URI,
      null, null, null, null)
    if(c==null || !c.moveToFirst()){
      return
    }
    val threadIndex = c.getColumnIndex(Telephony.Sms.Outbox.THREAD_ID)
    val addressIndex = c.getColumnIndex(Telephony.Sms.Outbox.ADDRESS)
    val idIndex = c.getColumnIndex(Telephony.Sms.Outbox._ID)
    val timeIndex = c.getColumnIndex(Telephony.Sms.Outbox.DATE_SENT)
    val sentIndex = c.getColumnIndex(Telephony.Sms.Outbox.STATUS)
    do {
      try {
        val threadId = c.getInt(threadIndex)
        val address = c.getString(addressIndex)
        val mid = c.getInt(idIndex)
        val timestamp = c.getLong(timeIndex)
        val sent = c.getInt(sentIndex)+10
        val sms = SMS("t${threadId}m$mid", "PERSONAL", threadId.toString(), mid.toString(),
          address, sent, timestamp)
        database.userDao().insertAll(sms)
        val cur = context.contentResolver.query(Telephony.Sms.Inbox.CONTENT_URI, null,
          "${Telephony.Sms.Inbox.THREAD_ID}=$threadId", null, null)
        if(cur==null || !cur.moveToFirst())
          continue
        val addressIndex2 = cur.getColumnIndex(Telephony.Sms.Inbox.ADDRESS)
        val idIndex2 = cur.getColumnIndex(Telephony.Sms.Inbox._ID)
        val timeIndex2 = cur.getColumnIndex(Telephony.Sms.Inbox.DATE)
        val readIndex = cur.getColumnIndex(Telephony.Sms.Inbox.READ)
        do {
          try {
            val mId = cur.getInt(idIndex2)
            if(database.userDao().getCat("t${threadId}m$mId")[0]=="PERSONAL"){
              continue
            }
            val address2 = cur.getString(addressIndex2)
            val timestamp2 = cur.getLong(timeIndex2)
            val read = cur.getInt(readIndex)
            val insms = SMS("t${threadId}m$mId", "PERSONAL", threadId.toString(), mId.toString(),
              address2, read, timestamp2)
            database.userDao().insertAll(insms)
          }catch (e: Exception){
            log("Personal Inbox SMS Error", e)
          }
        }while (cur.moveToNext())
        cur.close()
      }catch (e: Exception){
        log("Personal Outbox SMS Error", e)
      }
    }while (c.moveToNext())
    c.close()
  }

}