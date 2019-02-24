package com.msahil432.sms.database

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Created by msahil432
 **/

@Database(entities = [SMS::class], version = 4)
abstract class SmsDatabase : RoomDatabase() {
  abstract fun userDao(): UserDao
}