package com.msahil432.sms.database;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import com.msahil432.sms.helpers.TimeHelper;

/**
 * Created by msahil432
 **/

@Entity
public class SMS {
  @PrimaryKey @NonNull public String id;
  public String cat;
  public String threadId;
  public String mId;
  public String phone;
  public int status;
  public long timestamp;

  @Ignore public String timeAgo;
  @Ignore public String body;
  @Ignore public Bitmap thumbnail;
  @Ignore public String name;

  public SMS(@NonNull String id, String cat, String threadId,
             String mId, String phone, int status, long timestamp) {
    this.id = id;
    this.cat = cat;
    this.threadId = threadId;
    this.mId = mId;
    this.phone = phone;
    this.status = status;
    this.timestamp = timestamp;
  }

  @Ignore
  public SMS(@NonNull String id, String cat, String phone, int status) {
    this.id = id;
    this.cat = cat;
    this.threadId = id.substring(1, id.indexOf('m'));
    this.mId = id.substring(id.indexOf('m')+1);
    this.phone = phone;
    this.status = status;
  }
}
