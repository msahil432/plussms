package com.msahil432.sms.database

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*

/**
 * Created by msahil432
 **/

@Dao
interface UserDao {

  @Query("SELECT * FROM sms")
  fun getAll(): List<SMS>

  @Query("select count(id) from sms")
  fun getCount(): LiveData<Int>

  @Query("select count(id) from sms where cat = :cat")
  fun getCount(cat: String): LiveData<Int>

  @Query("select mId from sms where status =0")
  fun getUnreadCount(): List<String>

  @Query("select mId from sms where cat = :cat and status = 0")
  fun getUnreadCount(cat: String):List<String>

  @Query("select mId from sms where status =0")
  fun getLiveUnreadCount(): LiveData<List<String>>

  @Query("select mId from sms where cat = :cat and status = 0")
  fun getLiveUnreadCount(cat: String): LiveData<List<String>>

  @Query("select * from sms where cat = :cat group by threadId order by max (timestamp) desc")
  fun getForCat(cat: String): DataSource.Factory<Int, SMS>

  @Query("select cat from sms where timestamp = :timestamp")
  fun getCat(timestamp: Long) : List<String>

  @Query("select cat from sms where id = :uid")
  fun getCat(uid: String) : List<String>

  @Query("select mId from sms where threadId= :tId order by mId desc")
  fun getMessagesForThread(tId: String) : List<String>

  @Query("select mId from sms where threadId= :tId and cat= :cat order by mId desc")
  fun getMessagesForThread(tId: String, cat: String) : List<String>

  @Query("select * from sms where threadId= :tId order by mId desc")
  fun getSmsForThread(tId: String) : DataSource.Factory<Int, SMS>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertAll(vararg users: SMS)

  @Delete
  fun delete(user: SMS)
}
