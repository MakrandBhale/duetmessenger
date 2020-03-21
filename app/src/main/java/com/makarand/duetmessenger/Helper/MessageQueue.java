package com.makarand.duetmessenger.Helper;

import com.makarand.duetmessenger.Model.Message;

import java.util.ArrayList;

public class MessageQueue {
    private ArrayList<Message> q = new ArrayList<>();
    private final int FIRST = 0;
    private final int SECOND = 1;


    public void add(Message newMessage) {
        if (q.isEmpty()) {
            /*if empty then add to first location*/
            q.add(FIRST, newMessage);
        } else {
            /*if 1 message then move first to second and add new message to first location*/
            q.add(SECOND, q.get(FIRST));
            q.set(FIRST, newMessage);
        }
    }

    public Message getFirst() {
        return q.get(FIRST);
    }

    public Message getSecond(){
        if(q.size() < 2)
            return null;
        return q.get(SECOND);
    }
}
