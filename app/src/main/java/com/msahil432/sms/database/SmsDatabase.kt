package com.msahil432.sms.database

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Created by msahil432
 **/

@Database(entities = arrayOf(SMS::class), version = 2)
abstract class SmsDatabase : RoomDatabase() {
  abstract fun userDao(): UserDao
}