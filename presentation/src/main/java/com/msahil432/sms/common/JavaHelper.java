package com.msahil432.sms.common;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.util.Log;

import com.msahil432.sms.Retrofit;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaHelper {

    public static void pingServer(){
        try{
            URL url = new URL(Retrofit.hostUrl);
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
        Pattern pattern;
        Matcher matcher;
        String EMAIL_PATTERN = "\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}\\b";
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();
    }

}