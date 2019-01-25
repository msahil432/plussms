package com.msahil432.sms.common

import androidx.lifecycle.ViewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.*

/**
 * Created by msahil432 on
 **/

public open class BaseViewModel : ViewModel(){

  companion object {
    var NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors()
    var workQueue = LinkedBlockingQueue<Runnable>()
    var WorkThread: Executor = Executors.newSingleThreadExecutor()
    var DownloadThread: Executor = Executors.newSingleThreadExecutor()
    var DbThread: Executor = Executors.newSingleThreadExecutor()
    val retrofit = Retrofit.Builder()
      .addConverterFactory(GsonConverterFactory.create())
      .baseUrl("https://glacial-hamlet-87000.herokuapp.com")
      .build().create(RetroFit::class.java)
  }

  protected fun cancelTasks(){
    workQueue.clear()
  }

  override fun onCleared() {
    super.onCleared()
    cancelTasks()
  }
}