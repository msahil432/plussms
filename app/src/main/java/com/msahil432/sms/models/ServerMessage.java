package com.msahil432.sms.models;

public class ServerMessage {

  String id;
  String textMessage;
  String cat;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTextMessage() {
    return textMessage;
  }

  public void setTextMessage(String textMessage) {
    this.textMessage = textMessage;
  }

  public String getCat() {
    return cat;
  }

  public void setCat(String cat) {
    this.cat = cat;
  }
}
