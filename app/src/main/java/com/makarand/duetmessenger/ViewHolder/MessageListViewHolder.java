package com.makarand.duetmessenger.ViewHolder;

import android.animation.Animator;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.firebase.database.annotations.NotNull;
import com.makarand.duetmessenger.Helper.Constants;
import com.makarand.duetmessenger.R;
import com.vanniktech.emoji.EmojiTextView;

import java.util.ArrayList;

public class MessageListViewHolder extends RecyclerView.ViewHolder {
    private EmojiTextView messageText;
    private TextView timeText;
    private TextView dateText;
    private TextView statusTextTextView;
    public View messageBubble;
    public LinearLayout timeDateContainer;
    public LinearLayout messageStatusContainer;

    public MessageListViewHolder(@NotNull View view){
        super(view);
        messageText = view.findViewById(R.id.message_text);
        timeText = view.findViewById(R.id.message_time);
        dateText = view.findViewById(R.id.message_date);
        messageBubble = view.findViewById(R.id.bubble);
        timeDateContainer = view.findViewById(R.id.date_time_container);
        messageStatusContainer = view.findViewById(R.id.message_status_container);
        statusTextTextView = view.findViewById(R.id.message_status_text);
    }

    public void setMessageText(String message){
        messageText.setText(message);
    }

    public void startAnimation(int tech){
        Techniques techniques;
        switch (tech){
            case Constants.HEART_BEAT:
                techniques = Techniques.Pulse;
                break;
            case Constants.SOFT:
                techniques = Techniques.FadeIn;
                break;
            case Constants.ANGRY:
                techniques = Techniques.Shake;
                break;
            case Constants.EXCITED:
                techniques = Techniques.Tada;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + tech);
        }

        YoYo.with(techniques)
                .delay(50)
                .playOn(messageBubble);
    }




    public void setListener(ArrayList<String> time){
        messageBubble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(timeText.getVisibility() == View.GONE){
                    showTimeText(time);
                } else {
                    hideTimeText();
                }


                if(messageStatusContainer.getVisibility() == View.VISIBLE){
                    messageStatusContainer.setVisibility(View.GONE);
                } else {
                    messageStatusContainer.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void showTimeText(ArrayList<String> time){
        dateText.setText(time.get(0));
        timeText.setText(time.get(1));
        timeDateContainer.setVisibility(View.VISIBLE);
        time.clear();
    }

    public void hideTimeText(){
        /*
        * IMP DOnt forgetttt
        * */

        timeDateContainer.setVisibility(View.GONE);
    }

    private void showMessageStatus(){
        messageStatusContainer.setVisibility(View.VISIBLE);
    }


    public void setMessageStatus(String status){
        statusTextTextView.setText(status);
        showMessageStatus();
    }
    public void setMessageStatus(int status) {
        String statusText="";
        switch (status){
            case Constants.MESSAGE_STATUS_SENDING:
                /*YoYo.with(Techniques.SlideInDown)
                        .delay(1000)
                        .duration(300)
                        .onStart(new YoYo.AnimatorCallback() {
                            @Override
                            public void call(Animator animator) {
                                statusTextTextView.setText("Sending");
                                messageStatusContainer.setVisibility(View.VISIBLE);
                            }
                        })
                        .playOn(messageStatusContainer);*/
                break;
            case Constants.MESSAGE_STATUS_SENT:
                statusTextTextView.setText("Sent");
                showMessageStatus();
                //messageStatusContainer.setVisibility(View.VISIBLE);

                break;
        }
    }
    public void hideMessageStatus() {
        messageStatusContainer.setVisibility(View.GONE);
    }

}
