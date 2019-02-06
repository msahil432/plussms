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

  @Query("select * from sms where cat = :cat group by threadId order by max (mId)")
  fun getForCat(cat: String): DataSource.Factory<Int, SMS>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertAll(vararg users: SMS)

  @Delete
  fun delete(user: SMS)
}
