package com.msahil432.sms.setupActivity

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.msahil432.sms.common.BaseViewModel
import com.msahil432.sms.database.SMS
import com.msahil432.sms.database.SmsDatabase
import com.msahil432.sms.helpers.SmsHelper
import com.msahil432.sms.helpers.SmsHelper.*
import com.msahil432.sms.models.ServerMessage
import com.msahil432.sms.models.ServerModel
import java.lang.Exception

/**
 * Created by msahil432
 **/

class SetupViewModel : BaseViewModel() {

  private val totalSMS = MutableLiveData<Float>()
  private val personalSMS = MutableLiveData<Float>()
  private val moneySMS = MutableLiveData<Float>()
  private val promoSMS = MutableLiveData<Float>()
  private val updateSMS = MutableLiveData<Float>()
  private val otherSMS = MutableLiveData<Float>()

  private val collectedSms = ArrayList<ServerMessage>()

  private lateinit var context: Context
  private lateinit var database : SmsDatabase

  init {
    totalSMS.value = 100f
    personalSMS.value = 0f
    moneySMS.value = 0f
    promoSMS.value = 0f
    updateSMS.value = 0f
    otherSMS.value = 0f
  }

  fun getTotalSMS() : LiveData<Float> { return totalSMS}
  fun getPersonalSMS() : LiveData<Float> { return personalSMS}
  fun getMoneySMS() : LiveData<Float> { return moneySMS}
  fun getPromoSMS() : LiveData<Float> { return promoSMS}
  fun getUpdateSMS() : LiveData<Float> { return updateSMS}
  fun getOtherSMS() : LiveData<Float> { return otherSMS }

  fun startProcess(context: Context, database: SmsDatabase){
    this.context = context
    this.database = database
    WorkThread.execute(collectSms)
  }

  private val collectSms = Runnable {
    val uri = Uri.parse("content://sms/")
    val cursor = context.contentResolver.query(uri, null,
      "body IS NOT NULL", null, "date DESC")
    val bodyIndex = cursor!!.getColumnIndex(SmsHelper.COLUMN_BODY)
    val idIndex = cursor.getColumnIndex(SmsHelper.COLUMN_ID)
    val threadIndex = cursor.getColumnIndex(SmsHelper.COLUMN_THREAD_ID)

    if (!cursor.moveToFirst()) {
      return@Runnable
    }
    do{
      try {
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

    var pers = 0f
    var money = 0f
    var updates = 0f
    var promo = 0f
    var others = 0f

    for(i in 0 .. collectedSms.size/50){
      val tem2 = ServerModel()
      tem2.texts = ArrayList<ServerMessage>()

      for(a in 0 until 50){
        tem2.texts.add(collectedSms[(i*50)+a])
        if((i*50)+a+1 == collectedSms.size)
          break
      }

      val call = retrofit.categorizeSMS(tem2).execute()
      val v = call.body()!!
      Log.e("SetupViewModel", "classified "+v.texts.size)
      for(t in v.texts) {
        val threadId = t.id.substring(1, t.id.indexOf('m'))
        val mId = t.id.substring(t.id.indexOf('m') + 1)
        val phone = GetPhone(context, threadId)
        val read = GetMessageStatus(context, mId)
        val sms = SMS(t.id, "OTHERS", threadId, mId, phone, read)

        try {
          when (t.cat) {
            "PROMOTIONAL", "PROMO" -> {
              promo++
              promoSMS.postValue(promo)
              sms.cat = "PROMOTIONAL"
              database.userDao().insertAll(sms)
            }
            "PERSONAL", "URGENT" -> {
              pers++
              personalSMS.postValue(pers)
              sms.cat = "PERSONAL"
              database.userDao().insertAll(sms)
            }
            "MONEY", "OTP", "BANK" -> {
              money++
              moneySMS.postValue(money)
              sms.cat = "MONEY"
              database.userDao().insertAll(sms)
            }
            "UPDATES", "WALLET/APP", "ORDER" -> {
              updates++
              updateSMS.postValue(updates)
              sms.cat = "UPDATES"
              database.userDao().insertAll(sms)
            }
            else -> {
              others++
              otherSMS.postValue(others)
              database.userDao().insertAll(sms)
            }
          }
        }catch (e: Exception){
          log("SetupViewModel - error", e)
        }
      }

    }

  }

}