package com.msahil432.sms

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.moez.QKSMS.model.Conversation
import com.moez.QKSMS.model.Message
import com.msahil432.sms.common.BaseViewModel
import com.msahil432.sms.common.JavaHelper
import com.msahil432.sms.models.ServerMessage
import com.msahil432.sms.models.ServerModel
import io.realm.Realm
import io.realm.Sort
import java.lang.Exception
import java.util.concurrent.Executors

/**
 * Created by msahil432
 **/

class CatViewModel : BaseViewModel() {

    val total = MutableLiveData<Int>()
    val personal = MutableLiveData<Int>()
    val updates = MutableLiveData<Int>()
    val ads = MutableLiveData<Int>()
    val money = MutableLiveData<Int>()
    val others = MutableLiveData<Int>()

    val status = MutableLiveData<Int>()

    private val personalMessages = ArrayList<Message>()
    private val toProcessMessages = ArrayList<ServerMessage>()

    fun startProcess(context: Context){
        Executors.newSingleThreadExecutor().execute {
            JavaHelper.pingServer()
        }

        WorkThread.execute {
            val messages = Realm.getDefaultInstance().where(Message::class.java)
                    .equalTo("type", "sms").findAll()
            total.postValue(messages.size)

            val pm = Realm.getDefaultInstance().where(Message::class.java)
                    .equalTo("type", "sms")
                    .equalTo("category", "PERSONAL")
                    .findAll()
            personalMessages.addAll(pm)
            pers = pm.size
            personal.postValue(pers)

            val nonMessages = Realm.getDefaultInstance().where(Message::class.java)
                    .equalTo("type", "sms")
                    .equalTo("category", "NONE")
                    .findAll()

            for (t in nonMessages) {
                if(JavaHelper.getContactName(t.address, context)!=t.address){
                    personalMessages.add(t)
                    pers++
                    personal.postValue(pers)
                    continue
                }

                val temp = ServerMessage(t.id.toString(),
                        JavaHelper.cleanPrivacy(t.getText()), "")
                toProcessMessages.add(temp)
                if(toProcessMessages.size==50){
                    sendToServer(toProcessMessages)
                    toProcessMessages.clear()
                }
            }
            sendToServer(toProcessMessages)
            toProcessMessages.clear()

            processPersonalMessages()

            status.postValue(1)
        }
    }

    private fun processPersonalMessages(){
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        for(t in personalMessages){
            val ts = realm.where(Message::class.java).equalTo("threadId", t.threadId)
                    .findAll()
            l1@ for( t2 in ts){
                when(t2.category){
                    "PERSONAL"->{
                        continue@l1
                    }
                    "UPDATES" ->{
                        up--
                        updates.postValue(up)
                    }
                    "OTHERS" ->{
                        oth--
                        others.postValue(oth)
                    }
                    "ADS" ->{
                        ad--
                        ads.postValue(ad)
                    }
                    "FINANCE" ->{
                        mo--
                        money.postValue(mo)
                    }
                }
                t2.category = "PERSONAL"
                pers++
                personal.postValue(pers)
                saveToConversation(t2, realm)
            }
            realm.insertOrUpdate(ts)
        }
        realm.commitTransaction()
        realm.refresh()
        realm.close()
    }

    private fun sendToServer(collected: ArrayList<ServerMessage>){
        if(collected.size==0)
            return

        val tem2 = ServerModel()
        tem2.texts = collected

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
                status.postValue(0)
            }catch (e: Exception){
                net = false
                status.postValue(-1)
            }
        }
        saveMessage(v)
    }

    private var pers =0
    private var ad = 0
    private var mo =0
    private var oth = 0
    private var up =0

    private fun saveMessage(v: ServerModel){
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        for (t in v.texts) {
            try {
                val message = realm.where(Message::class.java).equalTo("id", t.id.toLong())
                        .findFirst()!!
                when (t.cat) {
                    "PROMOTIONAL", "PROMO", "ADS", "ads" -> {
                        message.category = "ADS"
                        ad++
                        ads.postValue(ad)
                    }
                    "PERSONAL", "URGENT", "personal" -> {
                        message.category = "PERSONAL"
                        pers++
                        personal.postValue(pers)
                        personalMessages.add(message)
                    }
                    "FINANCE", "OTP", "BANK", "money" -> {
                        message.category = "FINANCE"
                        mo++
                        money.postValue(mo)
                    }
                    "UPDATES", "WALLET/APP", "ORDER", "update" -> {
                        message.category = "UPDATES"
                        up++
                        updates.postValue(up)
                    }
                    else -> {
                        message.category = "OTHERS"
                        oth++
                        others.postValue(oth)
                    }
                }
                realm.insertOrUpdate(message)
                saveToConversation(message, realm)
            } catch (e: Exception) {
                log("SetupViewModel - error", e)
            }
        }
        realm.commitTransaction()
        realm.close()
        realm.refresh()
    }

    private fun saveToConversation(message: Message, realm: Realm){
        val existing = realm.where(Conversation::class.java)
                .equalTo("id", message.threadId)
                .equalTo("category", message.category)
                .sort("date", Sort.DESCENDING).findAll()
        if(existing.size>0){
            val conv = existing[0]!!
            if(conv.date<message.date){
                conv.snippet = message.body
                conv.date = message.date
                conv.me = message.isMe()
                conv.read = message.read
            }
            conv.count++
            realm.insertOrUpdate(conv)
            return
        }else{
            val e = realm.where(Conversation::class.java)
                    .equalTo("id", message.threadId)
                    .sort("date", Sort.DESCENDING)
                    .findAll()[0]!!
            val exists = realm.copyFromRealm(e)
            if(exists.category=="NONE") {
                exists.category = message.category
                exists.count = 1
                realm.insertOrUpdate(exists)
                return
            }
            val currentMax = realm.where(Conversation::class.java).max("vid")
            exists.vid = if(currentMax == null || currentMax==0 ) 1L else currentMax.toInt()+1L
            exists.category = message.category
            exists.snippet = message.body
            exists.date = message.date
            exists.me = message.isMe()
            exists.read = message.read
            exists.count = 1
            realm.insertOrUpdate(exists)
        }
    }
}