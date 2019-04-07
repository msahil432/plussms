package com.msahil432.sms.common

import android.util.Log
import androidx.lifecycle.ViewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue

/**
 * Created by msahil432
 **/

/**
 * Created by msahil432 on
 **/

public open class BaseViewModel : ViewModel(){

    companion object {
        var NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors()
        var workQueue = LinkedBlockingQueue<Runnable>()
        var WorkThread: Executor = Executors.newSingleThreadExecutor()
        var DownloadThread: Executor = Executors.newSingleThreadExecutor()
        val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(com.msahil432.sms.common.Retrofit.hostUrl)
                .build().create(com.msahil432.sms.common.Retrofit::class.java)!!
    }

    protected fun cancelTasks(){
        workQueue.clear()
    }

    override fun onCleared() {
        super.onCleared()
        cancelTasks()
    }


    protected fun log(what: String){
        Log.e("msahil432", what)
    }

    protected fun log(what: String, e: Exception){
        Log.e("msahil432", what, e)
    }

}