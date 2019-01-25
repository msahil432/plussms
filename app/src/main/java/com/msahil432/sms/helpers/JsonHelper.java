package com.msahil432.sms.helpers;

import android.content.Context;
import org.json.JSONObject;

public class JsonHelper {

  public static JSONObject makeText(String id, String body, String threadid) throws Exception{
    JSONObject objs = new JSONObject();
    objs.accumulate("id", id);
    objs.accumulate("textMessage", body);
    objs.accumulate("threadid", threadid);
    return objs;
  }


}
