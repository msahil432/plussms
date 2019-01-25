package com.msahil432.sms.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Created by msahil432
 **/

@Entity
public class SMS {
  @PrimaryKey @NonNull public String id;
  public String cat;
  public String threadId;
  public String mId;

  public SMS(@NonNull String id, String cat, String threadId, String mId) {
    this.id = id;
    this.cat = cat;
    this.threadId = threadId;
    this.mId = mId;
  }

  @Ignore
  public SMS(@NonNull String id, String cat) {
    this.id = id;
    this.cat = cat;
    this.threadId = id.substring(1, id.indexOf('m'));
    this.mId = id.substring(id.indexOf('m')+1);
  }
}
