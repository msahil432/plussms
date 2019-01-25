package com.msahil432.sms.common;

import com.msahil432.sms.models.ServerMessage;
import com.msahil432.sms.models.ServerModel;
import com.msahil432.sms.models.ServerModel2;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.POST;
import retrofit2.http.Part;

import java.util.List;

public interface RetroFit {

  @POST("/mobileapp/")
  public Call<ServerModel2> categorizeSMS(@Body ServerModel messages);

}
