package com.makarand.duetmessenger.Model;

import com.google.firebase.database.Exclude;
import com.makarand.duetmessenger.Helper.Constants;

public class TypingMessage extends Message{
    @Exclude
    private int messageType = Constants.NORMAL_MESSAGE;
    @Exclude private String avtarLink;

    public TypingMessage(){
        this.messageType = Constants.TYPING_MESSAGE;
    }
    public TypingMessage(String avtarLink){
        this.messageType = Constants.TYPING_MESSAGE;
        this.avtarLink = avtarLink;

    }

    @Override
    public int getMessageType() {
        return messageType;
    }

    @Override
    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public String getAvtarLink() {
        return avtarLink;
    }

    public void setAvtarLink(String avtarLink) {
        this.avtarLink = avtarLink;
    }
}
