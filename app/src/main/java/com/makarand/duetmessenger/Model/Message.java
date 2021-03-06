package com.makarand.duetmessenger.Model;


import android.hardware.ConsumerIrManager;
import android.text.format.DateFormat;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;
import com.makarand.duetmessenger.Helper.Constants;
import com.makarand.duetmessenger.Helper.EndToEnd;

import java.util.ArrayList;
import java.util.Calendar;

import io.reactivex.internal.operators.observable.ObservableElementAt;

@IgnoreExtraProperties
public class Message {
    private String sender, receiver, message, image;
    private int messageStatus;
    private Object timestamp;
    private Object arrivalTime = null;
    private String messageId;
    private Object seenTime = null;
    private int animationTechnique = -1;
    @Exclude private boolean showMessageStatus = false;
    @Exclude private int messageType = Constants.NORMAL_MESSAGE;
    @Exclude private boolean animationShown = false;

    public Message(int messageType){
        this.messageType  = messageType;
    }


    public Message(String messageId, String sender, String receiver, String message, int messageStatus) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.messageStatus = messageStatus;
        this.timestamp = ServerValue.TIMESTAMP;
        this.messageId = messageId;
    }

    public Message(String messageId, String sender, String receiver, String message, int messageStatus, int animationTechnique) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.messageStatus = messageStatus;
        this.timestamp = ServerValue.TIMESTAMP;
        this.messageId = messageId;
        this.animationTechnique = animationTechnique;
    }

    public Message() {
    }


    public int getAnimationTechnique() {
        return animationTechnique;
    }

    @Exclude
    public void setAnimationTechnique(int animationTechnique) {
        this.animationTechnique = animationTechnique;
    }


    @Exclude
    public boolean isAnimationShown() {
        return animationShown;
    }


    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setAnimationShown(boolean animationShown) {
        this.animationShown = animationShown;
    }

    public Object getSeenTime() {
        return seenTime;
    }

    public void setSeenTime(Object seenTime) {
        this.seenTime = seenTime;
    }

    public Object getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(Object arrivalTime) {
        this.arrivalTime = arrivalTime;
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

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
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
    public boolean hasSameStatus(Message otherMessage){
        if(otherMessage.getSeenTime() != null && this.getSeenTime() != null){
            return true;
        } else {
            if(otherMessage.getSeenTime() == null && this.getSeenTime() != null){
                return false;
            } else if(otherMessage.getSeenTime() != null && this.getSeenTime() == null) {
                return false;
            }
        }

        if(otherMessage.getArrivalTime() != null && this.getArrivalTime() != null){
            return true;
        } else {
            if(otherMessage.getArrivalTime() == null && this.getArrivalTime() != null){
                return false;
            } else if(otherMessage.getArrivalTime() != null && this.getArrivalTime() == null) {
                return false;
            }
        }
        return (otherMessage.getMessageStatus() == this.messageStatus);
    }

    @Exclude
    public boolean isShowMessageStatus() {
        return showMessageStatus;
    }

    @Exclude
    public void setShowMessageStatus(boolean showMessageStatus) {
        this.showMessageStatus = showMessageStatus;
    }

    @Exclude
    public int getMessageType() {
        return messageType;
    }

    @Exclude
    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    @Exclude
    public boolean getShowMessageStatus(){
        return this.showMessageStatus;
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
    public String getFormattedDate(long timestamp) {
        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeInMillis(timestamp);
        String date, time;
        Calendar now = Calendar.getInstance();
        ArrayList<String> dateTime = new ArrayList<>();
        final String timeFormatString = "h:mm aa";
        final String monthFormatString = "MMMM d";
        final String dateHourString = "h:mm aa";
        final long HOURS = 60 * 60 * 60;
        if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE) ) {
            //date = "Today";
            date = "";
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


        return date + " " + time;
    }



    @Exclude
    public boolean isLateReply(Message prevMessage){
        try{
            long diff = (getMessageTimestamp() - prevMessage.getMessageTimestamp()) / (60 * 1000);
            return  (diff) >= Constants.LATE_REPLY_TIMEOUT;
        } catch (NullPointerException e){
            e.printStackTrace();
            return true;
        }

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



    @Exclude
    @Override
    public boolean equals(Object otherObject){
        if(!(otherObject instanceof Message)){
            return false;
        }
        Message otherMessage = (Message) otherObject;

//        if(!this.sender.equals(otherMessage.getSender())){
//            return false;
//        }
//        if(!this.receiver.equals(otherMessage.getReceiver())){
//            return false;
//        }
//        if(!this.message.equals(otherMessage.getMessage())){
//            return false;
//        }
//        if(!(this.messageStatus == otherMessage.getMessageStatus())){
//            return false;
//        }
//        if(!(this.timestamp  == otherMessage.getTimestamp())){
//            return false;
//        }
//        if(!(this.arrivalTime  == otherMessage.getArrivalTime())){
//            return false;
//        }
//        if(!(this.seenTime  == otherMessage.getSeenTime())){
//            return false;
//        }

        return this.getMessageId().equals(otherMessage.getMessageId());
            /*Same Message*/
    }



}

