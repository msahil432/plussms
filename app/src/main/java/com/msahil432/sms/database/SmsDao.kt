package com.msahil432.sms.database

import androidx.paging.DataSource
import androidx.room.*

/**
 * Created by msahil432
 **/

@Dao
interface UserDao {
  @Query("SELECT * FROM sms")
  fun getAll(): List<SMS>

  @Query("select distinct(threadId), id, mId, phone, status, cat from sms where cat =:cat order by mId")
  fun getForCat(cat: String): DataSource.Factory<Int, SMS>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertAll(vararg users: SMS)

  @Delete
  fun delete(user: SMS)
}
