package com.msahil432.sms.database

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Created by msahil432
 **/

@Database(entities = [SMS::class], version = 3)
abstract class SmsDatabase : RoomDatabase() {
  abstract fun userDao(): UserDao
}