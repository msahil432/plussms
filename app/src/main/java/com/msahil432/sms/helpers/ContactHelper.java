package com.msahil432.sms.helpers;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Telephony;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContactHelper {

  public static final String TAG = "ContactHelper";

  public static String GetPhone(Context context, String threadId){
    if(threadId == null || threadId.isEmpty())
      return null;
    try {
      Cursor cursor = context.getContentResolver().query(Telephony.Sms.CONTENT_URI, null,
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

  public static String getName(Context context, String address) {
    if (address == null || address.isEmpty() || validateEmail(address))
      return address;

    while(address.contains(" ")){
      address = address.replace(" ", "");
    }

    Cursor cursor;
    Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address));
    ContentResolver contentResolver = context.getContentResolver();
    try {
      cursor = contentResolver.query(uri, new String[]{BaseColumns._ID,
          ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
      assert cursor != null;
      if (cursor.moveToNext())
        address = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
      cursor.close();
    } catch (Exception e) {
      Log.e(TAG, "Failed to find name for address " + address, e);
    }
    return address;
  }

  public static boolean validateEmail(String email) {
    Pattern pattern;
    Matcher matcher;
    String EMAIL_PATTERN = "\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}\\b";
    pattern = Pattern.compile(EMAIL_PATTERN);
    matcher = pattern.matcher(email);
    return matcher.matches();
  }

  public static long getId(Context context, String address) {

    if (address == null || address.isEmpty() || validateEmail(address))
      return 0;

    Cursor cursor;
    Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address));
    ContentResolver contentResolver = context.getContentResolver();
    long id = 0;
    try {
      cursor = contentResolver.query(uri, new String[]{BaseColumns._ID,
          ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
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

  private static int uriIndex = -1;
  public static Bitmap GetThumbnail(Context context, String address, String name){
    try {
      Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
          Uri.encode(address));
      Cursor cursor = context.getContentResolver().query(uri,
          new String[] { ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID },
          null, null, null);
      if (cursor == null || !cursor.moveToFirst()) {
        throw new Exception("Null or Empty Contact ID Cursor!");
      }
      long contactId = cursor.getLong(cursor
          .getColumnIndex(ContactsContract.PhoneLookup._ID));
      cursor.close();
      cursor = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI,
              null,
              ContactsContract.Data.CONTACT_ID + "=" + contactId + " AND "
                  + ContactsContract.Data.MIMETYPE + "='"
                  + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
                  + "'", null, null);
      if (cursor == null || !cursor.moveToFirst()) {
        throw new Exception("Null or Empty Photo Cursor!");
      }
      Uri person = ContentUris.withAppendedId(
          ContactsContract.Contacts.CONTENT_URI, contactId);
      Uri image_uri = Uri.withAppendedPath(person,
          ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
      cursor.close();
      return ImageHelper.getRoundedCornerBitmap(
                  MediaStore.Images.Media.getBitmap(
                      context.getContentResolver(), image_uri ) );
    } catch (Exception e) {
      return ImageHelper.getBitmapFromDrawable(
          ImageHelper.getAlphabet(name.charAt(0), colorGenerator.getColor(name)  )   );
    }
  }

  public static ColorGenerator colorGenerator = ColorGenerator.MATERIAL;
  public static  Bitmap getContactsPhoto(Context context, String address, String name) {
    String selection = ContactsContract.CommonDataKinds.Phone.NUMBER + " = '" + address + "'";
    Cursor phones = context.getContentResolver().query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, selection,
        null, null);
    while (phones != null && phones.moveToNext()) {
      String image_uri = phones.getString(phones.getColumnIndex(
          ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
      if (image_uri != null) {
        try {
          Bitmap bp = MediaStore.Images.Media
              .getBitmap(context.getContentResolver(),
                  Uri.parse(image_uri));
          phones.close();
          return bp;
        } catch (Exception e) {
          Log.e(TAG, address+" while getting photo"+e.getMessage());
        }
      }
    }
    if (phones != null) {
      phones.close();
    }
    int color = colorGenerator.getColor(address);
    if(address.equalsIgnoreCase(name) && address.indexOf("-")==2){
      return ImageHelper.getBitmapFromDrawable(ImageHelper.getAlphabet(address.charAt(3), color));
    }else{
      return ImageHelper.getBitmapFromDrawable(ImageHelper.getAlphabet(name.charAt(0), color));
    }
  }

  public static boolean viewContact(Context context, String phone){
    try {
      if(!isPhoneNumber(phone)) {
        Toast.makeText(context, "Not A Phone Number", Toast.LENGTH_SHORT).show();
        return false;
      }
      long contactID = getId(context, phone);
      Intent intent = new Intent(Intent.ACTION_VIEW);
      Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(contactID));
      intent.setData(uri);
      context.startActivity(intent);
      return true;
    }catch (Exception e){
      Toast.makeText(context, "Contact Doesn't Exist", Toast.LENGTH_SHORT).show();
      return false;
    }
  }

  public static ArrayAdapter<String> getContacts(Context context){
    ArrayList<String> arrayList = new ArrayList<>();

    Cursor cursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null,  null);
    if(!(cursor != null && cursor.moveToFirst()))
      return null;
    int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
    int idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
    int phonesIndex = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
    while(cursor.moveToNext()){
      try {
        String id = cursor.getString(idIndex);

        if(cursor.getInt(phonesIndex) > 0) {
          Cursor numberCursor = context.getContentResolver()
              .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                  null,
                  ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                  new String[]{id}, null);
          String phones;
          while(numberCursor != null && numberCursor.moveToNext()){
            phones = numberCursor.getString(
                numberCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            phones = phones + " C: "+cursor.getString(nameIndex);
            arrayList.add(phones);
          }
          if (numberCursor != null) {
            numberCursor.close();
          }
        }
      }catch (Exception e){
        e.printStackTrace();
      }
    }
    cursor.close();

    String[] phones = new String[arrayList.size()];
    arrayList.toArray(phones);

    return new ArrayAdapter<String>(context,
        android.R.layout.simple_dropdown_item_1line, phones);
  }

  public static boolean isEmailId(String mail){
    return  android.util.Patterns.EMAIL_ADDRESS.matcher(mail).matches();
  }

}
