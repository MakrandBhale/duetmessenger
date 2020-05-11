package com.makarand.duetmessenger.Helper;

import com.makarand.duetmessenger.Model.Message;

import java.util.ArrayList;

public class MessageStatusList {
    private ArrayList<Message> sentMessage, deliveredMessage;

    MessageStatusList(){
        sentMessage = new ArrayList<>();
        deliveredMessage = new ArrayList<>();
    }

    public void add(Message message){
        switch (message.getMessageStatus()){
            case Constants.MESSAGE_STATUS_SENT:
                sentMessage.add(message);

                break;
            case Constants.MESSAGE_STATUS_DELIVERED:
                deliveredMessage.add(message);
                break;
            default:
        }
    }
}
