package com.msahil432.sms.setupActivity

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.msahil432.sms.common.BaseViewModel
import com.msahil432.sms.database.SMS
import com.msahil432.sms.database.SmsDatabase
import com.msahil432.sms.helpers.JsonHelper
import com.msahil432.sms.helpers.SmsHelper
import com.msahil432.sms.models.ServerMessage
import com.msahil432.sms.models.ServerModel
import com.msahil432.sms.models.ServerModel2
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

/**
 * Created by msahil432 on
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

    for (i in 0 until collectedSms.size) {
      val tem2 = ServerModel()
      tem2.texts = ArrayList<ServerMessage>()
      tem2.texts.add(collectedSms[i])

      val call = retrofit.categorizeSMS(tem2).execute()
      Log.e("success", call.body().toString())
      Log.e("success", call.body()!!.text.cat)
      val t = call.body()!!.text
      when (t.cat){
        "PROMOTIONAL", "PROMO" -> {
          database.userDao().insertAll(SMS(t.id, "PROMOTIONAL"))
          promoSMS.postValue(promoSMS.value!! + 1)
        }
        "PERSONAL", "URGENT" -> {
          database.userDao().insertAll(SMS(t.id, "PERSONAL"))
          personalSMS.postValue(personalSMS.value!! + 1)
        }
        "MONEY", "OTP", "BANK" -> {
          database.userDao().insertAll(SMS(t.id, "MONEY"))
          moneySMS.postValue(moneySMS.value!! + 1)
        }
        "UPDATES", "WALLET/APP", "ORDER" -> {
          database.userDao().insertAll(SMS(t.id, "UPDATES"))
          updateSMS.postValue(updateSMS.value!! + 1)
        }
        else -> {
          database.userDao().insertAll(SMS(t.id, "OTHERS"))
          otherSMS.postValue(otherSMS.value!! + 1)
        }
      }
    }
  }

}