package com.msahil432.sms.receivers;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionManager;
import android.util.Log;
import com.msahil432.sms.SmsApplication;
import com.msahil432.sms.helpers.ContactHelper;
import com.msahil432.sms.notifications.NotificationHelper;
import com.msahil432.sms.services.BackgroundCategorizationService;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsReceiver extends BroadcastReceiver {

  private static final String TAG = "SmsReceiver";

  @Override
  public void onReceive(Context context, Intent intent) {
    if(intent.getAction().equals("android.provider.Telephony.SMS_DELIVER")) {
      Log.e(TAG, "SMS Delivered");
      return;
    }
    Log.e(TAG, "SMS Received");
    try {
      SmsMessage[] smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
      if (smsMessages == null || smsMessages.length < 1 || smsMessages[0].getMessageBody() == null) {
        Log.e(TAG, "No SMSs");
        return;
      }

      if (smsMessages[0].getMessageClass() == android.telephony.SmsMessage.MessageClass.CLASS_0) {
        Log.e(TAG, "Class 0 Message: " + smsMessages[0].getMessageBody());
        return;
      }

      final int errorCode = intent.getIntExtra("errorCode", 0);
      ContentValues values = parseReceivedSmsMessage(context, smsMessages, errorCode);
      if (SmsApplication.AmIDefaultApp(context)) {
        doDefaultAppStuff(context, intent, values);
      }

      NotificationHelper.Companion.createInitialNotification(context, values);

      Intent i = new Intent(context, BackgroundCategorizationService.class);
      if (smsMessages[0].getTimestampMillis() != 0) {
        i.putExtra(BackgroundCategorizationService.PARAM_TIMESTAMP,
            smsMessages[0].getTimestampMillis());
      } else {
        i.putExtra(BackgroundCategorizationService.PARAM_TIMESTAMP, -1L);
      }
      context.startService(i);
    }catch (Exception e){
      Log.e(TAG, e.getMessage(), e);
    }
  }

  private void doDefaultAppStuff(Context context, Intent intent, ContentValues values){
    int subId = intent.getIntExtra("subscription", -1);
    if(subId<0){
      subId = SubscriptionManager.getDefaultSmsSubscriptionId();
    }
    values.put(Telephony.Sms.SUBSCRIPTION_ID, subId);

    final Uri messageUri = context.getContentResolver().insert(Telephony.Sms.Inbox.CONTENT_URI,
        values);
    if (messageUri != null) {
      Log.e(TAG, "Inserted SMS message into telephony " + "uri = " + messageUri);
    } else {
      Log.e(TAG, "ReceiveSmsMessageAction: Failed to insert SMS into telephony!");
    }
    this.abortBroadcast();
  }

  public static ContentValues parseReceivedSmsMessage(
      final Context context, final SmsMessage[] msgs, final int error) {
    final SmsMessage sms = msgs[0];
    final ContentValues values = new ContentValues();

    final String address =sms.getDisplayOriginatingAddress();
    if(address==null){
      // If address is null, Use Unknown Sender Notation
      values.put(Telephony.Sms.ADDRESS, "\u02BCUNKNOWN_SENDER!\u02BC");
    }else{
      values.put(Telephony.Sms.ADDRESS, address);
    }
    values.put(Telephony.Sms.BODY, buildMessageBodyFromPdus(msgs));
    if (hasSmsDateSentColumn(context)) {
      values.put(Telephony.Sms.DATE_SENT, sms.getTimestampMillis());
    }
    ArrayList<String> set = new ArrayList<>();
    set.add(address);
    long threadId = getOrCreateThreadId(context, set);
    values.put(Telephony.Sms.THREAD_ID, threadId);
    values.put(Telephony.Sms.Inbox.READ, 0);
    values.put(Telephony.Sms.Inbox.SEEN, 0);
    values.put(Telephony.Sms.PROTOCOL, sms.getProtocolIdentifier());
    if (sms.getPseudoSubject().length() > 0) {
      values.put(Telephony.Sms.SUBJECT, sms.getPseudoSubject());
    }
    values.put(Telephony.Sms.REPLY_PATH_PRESENT, sms.isReplyPathPresent() ? 1 : 0);
    values.put(Telephony.Sms.SERVICE_CENTER, sms.getServiceCenterAddress());
    // Error code
    values.put(Telephony.Sms.ERROR_CODE, error);
    return values;
  }

  private static Boolean sHasSmsDateSentColumn = null;
  public static boolean hasSmsDateSentColumn(Context context) {
    if (sHasSmsDateSentColumn == null) {
      Cursor cursor = null;
      try {
        final ContentResolver resolver = context.getContentResolver();
        cursor = resolver.query( Telephony.Sms.CONTENT_URI,
            new String[]{Telephony.Sms.DATE_SENT}, null,
            null, Telephony.Sms.DATE_SENT + " ASC LIMIT 1");
        sHasSmsDateSentColumn = true;
      } catch (final SQLiteException e) {
        Log.e(TAG, "date_sent in sms table does not exist", e);
        sHasSmsDateSentColumn = false;
      } finally {
        if (cursor != null) {
          cursor.close();
        }
      }
    }
    return sHasSmsDateSentColumn;
  }



  private static String replaceFormFeeds(final String s) {
    return s == null ? "" : s.replace('\f', '\n');
  }
  // Parse the message body from message PDUs
  private static String buildMessageBodyFromPdus(final SmsMessage[] msgs) {
    if (msgs.length == 1) {
      // There is only one part, so grab the body directly.
      return replaceFormFeeds(msgs[0].getDisplayMessageBody());
    } else {
      // Build up the body from the parts.
      final StringBuilder body = new StringBuilder();
      for (final SmsMessage msg : msgs) {
        try {
          // getDisplayMessageBody() can NPE if mWrappedMessage inside is null.
          body.append(msg.getDisplayMessageBody());
        } catch (final NullPointerException e) {
          // Nothing to do
        }
      }
      return replaceFormFeeds(body.toString());
    }
  }

  public static long getOrCreateThreadId(final Context context, final ArrayList<String> recipients) {
    final Uri uri = Uri.parse("content://mms-sms/threadID");
    final Uri.Builder uriBuilder = uri.buildUpon();
    for (String recipient : recipients) {
      if (ContactHelper.isEmailId(recipient)) {
        recipient = extractAddrSpec(recipient);
      }
      uriBuilder.appendQueryParameter("recipient", recipient);
    }
    //if (DEBUG) Rlog.v(TAG, "getOrCreateThreadId uri: " + uri);
    final Cursor cursor = context.getContentResolver().query(uri,
        new String[]{ BaseColumns._ID }, null, null, null);
    if (cursor != null) {
      try {
        if (cursor.moveToFirst()) {
          return cursor.getLong(0);
        } else {
          Log.e(TAG, "getOrCreateThreadId returned no rows!");
        }
      } finally {
        cursor.close();
      }
    }

    Cursor c = context.getContentResolver().query(Telephony.Sms.CONTENT_URI, null,
        Telephony.Sms.ADDRESS+"="+recipients.get(0), null, null);
    if(c!=null && c.moveToFirst()){
      long t =  c.getLong(c.getColumnIndex(Telephony.Sms.THREAD_ID));
      c.close();
      return t;
    }
    c = context.getContentResolver().query(Telephony.Sms.CONTENT_URI,
        new String[]{Telephony.Sms.THREAD_ID},
        Telephony.Sms.ADDRESS+"="+recipients.get(0), null,
        Telephony.Sms.THREAD_ID+" DESC");
    if(c!=null && c.moveToFirst()) {
      long t = c.getLong(0);
      c.close();
      return t;
    }
    c = context.getContentResolver().query(Telephony.Sms.CONTENT_URI,
        new String[]{Telephony.Sms.THREAD_ID}, null, null,
        Telephony.Sms.THREAD_ID+" DESC");
    if(c==null || !c.moveToFirst())
      throw new RuntimeException("Can't create ThreadId");
    long t = c.getLong(0)+1;
    c.close();
    return t;
  }
  /**
   * mailbox         =       name-addr
   * name-addr       =       [display-name] angle-addr
   * angle-addr      =       [CFWS] "<" addr-spec ">" [CFWS]
   */
  public static final Pattern NAME_ADDR_EMAIL_PATTERN =
      Pattern.compile("\\s*(\"[^\"]*\"|[^<>\"]+)\\s*<([^<>]+)>\\s*");
  public static String extractAddrSpec(final String address) {
    final Matcher match = NAME_ADDR_EMAIL_PATTERN.matcher(address);
    if (match.matches()) {
      return match.group(2);
    }
    return address;
  }


}
