package com.makarand.duetmessenger.ViewHolder;

import android.graphics.LinearGradient;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.annotations.NotNull;
import com.makarand.duetmessenger.R;
import com.vanniktech.emoji.EmojiTextView;

import java.util.ArrayList;

public class MessageListViewHolder extends RecyclerView.ViewHolder {
    private EmojiTextView messageText;
    private TextView timeText;
    private TextView dateText;
    private RelativeLayout messageBubble;
    private LinearLayout timeDateContainer;
    public MessageListViewHolder(@NotNull View view){
        super(view);
        messageText = view.findViewById(R.id.message_text);
        timeText = view.findViewById(R.id.message_time);
        dateText = view.findViewById(R.id.message_date);
        messageBubble = view.findViewById(R.id.bubble);
        timeDateContainer = view.findViewById(R.id.date_time_container);
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

}
