package com.msahil432.sms.helpers

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by msahil432
 **/

class TimeHelper{
  companion object {

    fun TimeAgo(millis : Long): String{
      return when(millis/1000){
        in 0 .. 1 ->
          "Just Now"

        in 1 .. 59 ->
          "${(millis/1000).toInt()} Seconds Ago"

        in 1*60 until 2*60 ->
          "1 Minute Ago"

        in 2*60 .. 59*60 ->
          "${(millis/(1000*60)).toInt()} Minutes Ago"

        in 1*60*60 until 2*60*60 ->
          "1 Hour Ago"

        in 2*60*60 .. 23*60*60 ->
          "${(millis/(1000*60*60)).toInt()} Hours Ago"

        in 24*60*60 until 2*24*60*60 ->
          "Yesterday ${SimpleDateFormat("hh:mm a").format(System.currentTimeMillis()-millis)}"

        in 2*24*60*60 .. 365*24*60*60 ->
          SimpleDateFormat("dd MMM hh:mm a").format(System.currentTimeMillis()-millis)

        else ->
          SimpleDateFormat("dd-mm-yy hh:mm a").format(System.currentTimeMillis()-millis)
      }
    }


  }
}
