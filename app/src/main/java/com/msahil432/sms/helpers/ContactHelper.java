package com.msahil432.sms.helpers;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContactHelper {

  public static final String TAG = "ContactHelper";

  //No modifications
  public static String getName(Context context, String address) {

    if (address == null || address.isEmpty() || validateEmail(address))
      return address;

    while(address.contains(" ")){
      address = address.replace(" ", "");
    }

    Cursor cursor;

    Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address));
    ContentResolver contentResolver = context.getContentResolver();

    String name = address;

    try {
      cursor = contentResolver.query(uri, new String[]{BaseColumns._ID, ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
      assert cursor != null;
      if (cursor.moveToNext())
        name = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
      cursor.close();
    } catch (Exception e) {
      Log.d(TAG, "Failed to find name for address " + address);
      e.printStackTrace();
    }
    return name;
  }

  //No modifications
  public static boolean validateEmail(String email) {
    Pattern pattern;
    Matcher matcher;
    String EMAIL_PATTERN = "\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}\\b";
    pattern = Pattern.compile(EMAIL_PATTERN);
    matcher = pattern.matcher(email);
    return matcher.matches();
  }

  //No modifications
  public static long getId(Context context, String address) {

    if (address == null || address.isEmpty() || validateEmail(address))
      return 0;

    Cursor cursor;

    Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address));
    ContentResolver contentResolver = context.getContentResolver();

    long id = 0;

    try {
      cursor = contentResolver.query(uri, new String[]{BaseColumns._ID, ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
      assert cursor != null;
      if (cursor.moveToNext())
        id = cursor.getLong(cursor.getColumnIndex(ContactsContract.Data._ID));
      cursor.close();
    } catch (Exception e) {
      Log.d(TAG, "Failed to find ID for address " + address);
      e.printStackTrace();
    }

    return id;
  }

  public static boolean isPhoneNumber(String number){
    while(number.contains(" ")){
      number = number.replace(" ", "");
    }
    return PhoneNumberUtils.isGlobalPhoneNumber(number);
  }

}
