/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.moez.QKSMS.manager

import com.msahil432.sms.Retrofit
import com.msahil432.sms.common.JavaHelper
import com.msahil432.sms.models.ServerMessage
import com.msahil432.sms.models.ServerModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActiveConversationManagerImpl @Inject constructor() : ActiveConversationManager {

    private var threadId: Long? = null

    override fun setActiveConversation(threadId: Long?) {
        this.threadId = threadId
    }

    override fun getActiveConversation(): Long? {
        return threadId
    }

    private val map = HashMap<String, String>()
    private val retrofit = retrofit2.Retrofit.Builder()
            .baseUrl(Retrofit.hostUrl).addConverterFactory(GsonConverterFactory.create()).build()
            .create(Retrofit::class.java)
    override fun getCategoryForSms(body: String): String {
        val text = JavaHelper.cleanPrivacy(body)
        val cat = map[text]
        if(cat==null){
            val t = ServerModel(listOf(ServerMessage("hey-there", text, "")))
            retrofit.categorizeSMS(t).enqueue(object: Callback<ServerModel> {
                override fun onFailure(call: Call<ServerModel>, t: Throwable) {}
                override fun onResponse(call: Call<ServerModel>, response: Response<ServerModel>) {
                    try {
                        val t2 = response.body()!!.texts
                        if (t2 != null)
                            map[t2[0].textMessage] = t2[0].cat
                    }catch (e: Exception){
                        e.printStackTrace()
                    }
                }
            })
            return "NONE"
        }
        return cat
    }
}