package com.msahil432.sms.common;

import com.msahil432.sms.models.ServerModel;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RetroFit {

  public final String hostUrl = "https://glacial-hamlet-87000.herokuapp.com";

  @POST("/mobileapps/")
  public Call<ServerModel> categorizeSMS(@Body ServerModel messages);

}
