package com.msahil432.sms.database

import androidx.room.*

/**
 * Created by msahil432
 **/

@Dao
interface UserDao {
  @Query("SELECT * FROM sms")
  fun getAll(): List<SMS>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertAll(vararg users: SMS)

  @Delete
  fun delete(user: SMS)
}
