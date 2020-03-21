package com.makarand.duetmessenger.Threads;

import android.provider.ContactsContract;

import com.google.firebase.database.DatabaseReference;
import com.makarand.duetmessenger.Helper.Constants;

import android.os.Handler;
import android.text.Editable;

public class TypingThread implements Runnable{
    private DatabaseReference myRef;
    private long typingTime;
    private final int DELAY = 1000;
    private Handler handler;
    public TypingThread(DatabaseReference myRef){
        this.myRef = myRef;
        this.handler = new android.os.Handler();
    }

    public void typingStarted(){
        myRef.child("online").setValue(Constants.TYPING);
        handler.removeCallbacks(this);
    }

    public void typingStopped(){
        typingTime = System.currentTimeMillis();
        handler.postDelayed(this, DELAY);
    }

    @Override
    public void run() {
        if (System.currentTimeMillis() > (typingTime + DELAY - 500)) {
            myRef.child("online").setValue(Constants.ONLINE);
        }

    }
}
