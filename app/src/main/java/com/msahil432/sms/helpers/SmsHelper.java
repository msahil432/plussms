package com.msahil432.sms.helpers;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SmsHelper {

  //DB tags, All thanks to QKSMS
  public static final Uri SMS_CONTENT_PROVIDER = Uri.parse("content://sms/");
  public static final Uri MMS_CONTENT_PROVIDER = Uri.parse("content://mms/");
  public static final Uri MMS_SMS_CONTENT_PROVIDER = Uri.parse("content://mms-sms/conversations/");
  public static final Uri SENT_MESSAGE_CONTENT_PROVIDER = Uri.parse("content://sms/sent");
  public static final Uri DRAFTS_CONTENT_PROVIDER = Uri.parse("content://sms/draft");
  public static final Uri PENDING_MESSAGE_CONTENT_PROVIDER = Uri.parse("content://sms/outbox");
  public static final Uri RECEIVED_MESSAGE_CONTENT_PROVIDER = Uri.parse("content://sms/inbox");
  public static final Uri CONVERSATIONS_CONTENT_PROVIDER = Uri.parse("content://mms-sms/conversations?simple=true");
  public static final Uri ADDRESSES_CONTENT_PROVIDER = Uri.parse("content://mms-sms/canonical-addresses");

  public static final String MAX_MMS_ATTACHMENT_SIZE_UNLIMITED = "unlimited";
  public static final String MAX_MMS_ATTACHMENT_SIZE_300KB = "300kb";
  public static final String MAX_MMS_ATTACHMENT_SIZE_600KB = "600kb";
  public static final String MAX_MMS_ATTACHMENT_SIZE_1MB = "1mb";

  public static final String sortDateDesc = "date DESC";
  public static final String sortDateAsc = "date ASC";

  public static final byte UNREAD = 0;
  public static final byte READ = 1;

  // Attachment types
  public static final int TEXT = 0;
  public static final int IMAGE = 1;
  public static final int VIDEO = 2;
  public static final int AUDIO = 3;
  public static final int SLIDESHOW = 4;

  // Columns for SMS content providers
  public static final String COLUMN_ID = "_id";
  public static final String COLUMN_THREAD_ID = "thread_id";
  public static final String COLUMN_ADDRESS = "address";
  public static final String COLUMN_RECIPIENT = "recipient_ids";
  public static final String COLUMN_PERSON = "person";
  public static final String COLUMN_SNIPPET = "snippet";
  public static final String COLUMN_DATE = "date";
  public static final String COLUMN_DATE_NORMALIZED = "normalized_date";
  public static final String COLUMN_DATE_SENT = "date_sent";
  public static final String COLUMN_STATUS = "status";
  public static final String COLUMN_ERROR = "error";
  public static final String COLUMN_READ = "read";
  public static final String COLUMN_TYPE = "type";
  public static final String COLUMN_MMS = "ct_t";
  public static final String COLUMN_TEXT = "text";
  public static final String COLUMN_SUB = "sub";
  public static final String COLUMN_MSG_BOX = "msg_box";
  public static final String COLUMN_SUBJECT = "subject";
  public static final String COLUMN_BODY = "body";
  public static final String COLUMN_SEEN = "seen";

  public static final String READ_SELECTION = COLUMN_READ + " = " + READ;
  public static final String UNREAD_SELECTION = COLUMN_READ + " = " + UNREAD;
  public static final String UNSEEN_SELECTION = COLUMN_SEEN + " = " + UNREAD;
  public static final String FAILED_SELECTION = COLUMN_TYPE + " = " + SmsHelper.FAILED;



  private final String TAG = "Message";

  public static final int RECEIVED = 1;
  public static final int SENT = 2;
  public static final int DRAFT = 3;
  public static final int SENDING = 4;
  public static final int FAILED = 5;

  // ContentResolver columns
  private Context context;
  private Uri uri;
  private long id;
  private long threadId;
  private String body;
  private String address;
  private String name;
  private long contactId;

  //Ours :P
  private String date;

  //Deprecated to caution the usage
  @Deprecated
  public SmsHelper(Context context){

    this.context = context;
  }

  public SmsHelper(Context context, Uri uri){

    this.context = context;
    this.uri = uri;

    Cursor cursor = context.getContentResolver().query(uri, new String[]{COLUMN_ID}, null, null, null);
    cursor.moveToFirst();
    id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
    cursor.close();

  }

  public SmsHelper(Context context, long id){
    this.context = context;
    this.id = id;
  }

  public long getId() {
    return id;
  }

  public long getThreadId() {
    if (threadId == 0) {
      Cursor cursor = null;
      try {
        cursor = context.getContentResolver().query(SMS_CONTENT_PROVIDER, new String[]{COLUMN_THREAD_ID}, "_id=" + id, null, null);
        cursor.moveToFirst();
        threadId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_THREAD_ID));
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        if (cursor != null) {
          cursor.close();
        }
      }
    }
    return threadId;
  }

  public String getBody() {
    if (body == null) {
      Cursor cursor = null;
      try {
        cursor = context.getContentResolver().query(SMS_CONTENT_PROVIDER, new String[]{COLUMN_BODY}, "_id=" + id, null, null);
        cursor.moveToFirst();
        body = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BODY));
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        if (cursor != null) {
          cursor.close();
        }
      }
    }

    return body;
  }

  public String getAddress() {
    if (address == null) {
      Cursor cursor = null;
      try {
        cursor = context.getContentResolver().query(SMS_CONTENT_PROVIDER, new String[]{COLUMN_ADDRESS}, "_id=" + id, null, null);
        cursor.moveToFirst();
        address = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS));
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        if (cursor != null) {
          cursor.close();
        }
      }
    }

    return address;
  }

  public String getName() {

    if (name == null)
      name = ContactHelper.getName(context, getAddress());

    return name;
  }

  public long getContactId() {
    if (contactId == 0)
      contactId = ContactHelper.getId(context, getAddress());
    return contactId;
  }

  public String getTime(){
    if (date == null) {
      Cursor cursor = null;
      try {
        cursor = context.getContentResolver().query(SMS_CONTENT_PROVIDER, new String[]{COLUMN_DATE}, "_id=" + id, null, null);
        cursor.moveToFirst();
        long l = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_DATE));
        Date d = new Date(l);
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy");
        date = format.format(d);

      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        if (cursor != null) {
          cursor.close();
        }
      }
    }

    return date;
  }

  public static String getSnippet(String data){
    String snippet = data;

    snippet = snippet.replaceAll("\n", " ");

    snippet = snippet.substring(1, 20);

    Log.v("Snippet", snippet);

    return snippet;
  }

  public static String GetPhone(Context context, String threadId){
    if(threadId == null || threadId.isEmpty())
      return null;
    try {
      Cursor cursor = context.getContentResolver().query(Uri.parse("content://sms/"), null,
          Telephony.Sms.THREAD_ID +"="+threadId, null, null);
      assert cursor != null;
      cursor.moveToFirst();
      String t = cursor.getString(cursor.getColumnIndex(Telephony.Sms.ADDRESS));
      cursor.close();
      return t;
    }catch (Exception e){
      e.printStackTrace();
      return null;
    }
  }

  public static int GetMessageStatus(Context context, String messageId){
    if(messageId == null || messageId.isEmpty())
      return SmsHelper.READ;
    try {
      Uri uri = Uri.parse("content://sms/");
      Cursor cursor = context.getContentResolver().query(uri, new String[]{SmsHelper.COLUMN_STATUS},
          SmsHelper.COLUMN_ID, new String[]{messageId}, "date DESC");
      int t = cursor.getInt(0);
      cursor.close();
      return t;
    }catch (Exception e){
      return SmsHelper.READ;
    }
  }

}