package com.makarand.duetmessenger.Model;


import android.text.format.DateFormat;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;
import com.makarand.duetmessenger.Helper.Constants;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

@IgnoreExtraProperties
public class Message {
    private String sender, receiver, message;
    private int messageStatus;
    private Object timestamp;

    public Message(String sender, String receiver, String message, int messageStatus) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.messageStatus = messageStatus;
        this.timestamp = ServerValue.TIMESTAMP;
    }

    public Message() {
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(int messageStatus) {
        this.messageStatus = messageStatus;
    }

    @Exclude
    private long getMessageTimestamp(){
        return (Long) timestamp;
    }

    @Exclude
    public ArrayList<String> getFormattedDate() {
        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeInMillis(getMessageTimestamp());
        String date, time;
        Calendar now = Calendar.getInstance();
        ArrayList<String> dateTime = new ArrayList<>();
        final String timeFormatString = "h:mm aa";
        final String monthFormatString = "MMMM d";
        final String dateHourString = "h:mm aa";
        final long HOURS = 60 * 60 * 60;
        if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE) ) {
            date = "Today";
            time = DateFormat.format(timeFormatString, smsTime).toString();
        } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1  ){
            date = "Yesterday";
            time = DateFormat.format(timeFormatString, smsTime).toString();
        } else if (now.get(Calendar.YEAR) == smsTime.get(Calendar.YEAR)) {
            date = DateFormat.format(monthFormatString, smsTime).toString();
            time =  DateFormat.format(dateHourString, smsTime).toString();
        } else {
            date = DateFormat.format("MMMM dd yyyy", smsTime).toString();
            time = DateFormat.format("h:mm aa", smsTime).toString();
        }

        dateTime.add(date);
        dateTime.add(time);
        return dateTime;
    }



    @Exclude
    public boolean isLateReply(Message prevMessage){
        long diff = (getMessageTimestamp() - prevMessage.getMessageTimestamp()) / (60 * 1000);
        return  (diff) >= Constants.LATE_REPLY_TIMEOUT;

        /*
        Timestamp currentTimestamp = new Timestamp(getMessageTimestamp());
        Timestamp prevMsgTimestamp = new Timestamp(prevMessage.getMessageTimestamp());
        *//*The previous message is more than 3 minutes old, show time banner now*//*
        return compareTwoTimeStamps(currentTimestamp, prevMsgTimestamp) > Constants.LATE_REPLY_TIMEOUT;*/
    }

/*    @Exclude
    private long compareTwoTimeStamps(Timestamp currentTime, java.sql.Timestamp oldTime)
    {
        long milliseconds1 = oldTime.getTime();
        long milliseconds2 = currentTime.getTime();
        long diff = milliseconds2 - milliseconds1;
        //long diffSeconds = diff / 1000;

        *//*Diff in minutes*//*
        return diff / (60 * 1000);


        //long diffHours = diff / (60 * 60 * 1000);
        //long diffDays = diff / (24 * 60 * 60 * 1000);
        //return diffMinutes;
    }*/

    /*
    @Exclude
    public String getTimeStringFromTimestamp(){
        String timeText;

        SimpleDateFormat sfd = new SimpleDateFormat("hh:mma", Locale.ENGLISH);
        timeText = sfd.format(new Date(getMessageTimestamp()));
        return timeText;
    }

    @Exclude
    public boolean isSameAsPrev(Message prevMessage){
        return getTimeStringFromTimestamp().equals(prevMessage.getTimeStringFromTimestamp());
    }
*/

}
