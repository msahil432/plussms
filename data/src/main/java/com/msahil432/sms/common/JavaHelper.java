package com.msahil432.sms.common;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.msahil432.sms.SmsClassifier.CATEGORY_ADS;
import static com.msahil432.sms.SmsClassifier.CATEGORY_FINANCE;
import static com.msahil432.sms.SmsClassifier.CATEGORY_OTHERS;
import static com.msahil432.sms.SmsClassifier.CATEGORY_PERSONAL;
import static com.msahil432.sms.SmsClassifier.CATEGORY_UPDATES;

public class JavaHelper {

    public static void pingServer(){
        try{
            URL url = new URL("https://glacial-hamlet-87000.herokuapp.com");
            HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
            urlc.setRequestProperty("Connection", "close");
            urlc.setConnectTimeout(1000 * 30);
            urlc.connect();

            if (urlc.getResponseCode() == 200) {
                Log.i("JavaHelper", "Ping Successful to Server");
            }
        }catch (Exception e){
            Log.i("JavaHelper", "Ping unsuccessful to Server");
        }
    }

    public static String cleanPrivacy(String text){
        return text.replaceAll("\\d", "");
    }

    public static String getContactName(String address, Context context){
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
            Log.e("JavaHelper", "Failed to find name for address " + address, e);
        }
        return address;
    }

    public static boolean validateEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static long resolveVid(long id, String category){
        switch (category){
            case CATEGORY_PERSONAL : return id + 1000000;
            case CATEGORY_OTHERS : return id + 2000000;
            case CATEGORY_ADS : return id + 3000000;
            case CATEGORY_UPDATES : return id + 4000000;
            case CATEGORY_FINANCE : return id + 5000000;
        }
        return id;
    }

    public static boolean validPhoneNumber(String phone){
        return android.util.Patterns.PHONE.matcher(phone).matches();
    }

    public static boolean canSendToThis(String address){
        return validateEmail(address) || validPhoneNumber(address);
    }

}
