package com.makarand.duetmessenger.ViewHolder;

import android.graphics.LinearGradient;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.view.menu.MenuView;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.firebase.database.annotations.NotNull;
import com.makarand.duetmessenger.Helper.Constants;
import com.makarand.duetmessenger.R;
import com.vanniktech.emoji.EmojiTextView;

import java.util.ArrayList;

import butterknife.BindView;

public class MessageListViewHolder extends RecyclerView.ViewHolder {
    private EmojiTextView messageText;
    private TextView timeText;
    private TextView dateText;
    private TextView statusTextTextView;
    //private LinearLayout messageBubble;
    private LinearLayout timeDateContainer;
    private LinearLayout messageStatusContainer;
    private View view;

    public MessageListViewHolder(@NotNull View view){
        super(view);
        messageText = view.findViewById(R.id.message_text);
        timeText = view.findViewById(R.id.message_time);
        dateText = view.findViewById(R.id.message_date);
        //messageBubble = view.findViewById(R.id.bubble);
        timeDateContainer = view.findViewById(R.id.date_time_container);
        messageStatusContainer = view.findViewById(R.id.message_status_container);
        statusTextTextView = view.findViewById(R.id.message_status_text);
    }

    public void setMessageText(String message){
        messageText.setText(message);
    }

    public void setTimeText(ArrayList<String> time){
        dateText.setText(time.get(0));
        timeText.setText(time.get(1));
        timeDateContainer.setVisibility(View.VISIBLE);
        time.clear();
    }

/*    public void setListener(String time){
        messageBubble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(timeText.getVisibility() == View.GONE){
                    setTimeText(time);
                } else {
                    removeTimeText();
                }
            }
        });
    }*/

    public void removeTimeText(){
        /*
        * IMP DOnt forgetttt
        * */

        timeDateContainer.setVisibility(View.GONE);
    }

    public void showMessageStatus(String status){
        messageStatusContainer.setVisibility(View.VISIBLE);

        statusTextTextView.setText("Delivered • " + status);
    }

    public void showMessageStatus(int status) {
        messageStatusContainer.setVisibility(View.VISIBLE);
        String statusText="";
        switch (status){
            case Constants.MESSAGE_STATUS_SENDING:
                statusTextTextView.setText("Sending");
                break;
            case Constants.MESSAGE_STATUS_SENT:
                statusTextTextView.setText("Sent");
                break;
        }
    }

    public void hideMessageStatus() {
        messageStatusContainer.setVisibility(View.GONE);
    }


}
