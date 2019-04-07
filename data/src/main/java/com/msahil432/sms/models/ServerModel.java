package com.msahil432.sms.models;

import java.util.List;

public class ServerModel {

    List<ServerMessage> texts;

    public ServerModel() {    }

    public ServerModel(List<ServerMessage> texts) {
        this.texts = texts;
    }

    public List<ServerMessage> getTexts() {
        return texts;
    }

    public void setTexts(List<ServerMessage> texts) {
        this.texts = texts;
    }
}