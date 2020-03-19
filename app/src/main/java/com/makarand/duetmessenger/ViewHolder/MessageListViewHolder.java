package com.makarand.duetmessenger.ViewHolder;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.annotations.NotNull;
import com.makarand.duetmessenger.R;
import com.vanniktech.emoji.EmojiTextView;

public class MessageListViewHolder extends RecyclerView.ViewHolder {
    private EmojiTextView messageText;
    private TextView timeText;
    private RelativeLayout messageBubble;
    public MessageListViewHolder(@NotNull View view){
        super(view);
        messageText = view.findViewById(R.id.message_text);
        timeText = view.findViewById(R.id.message_time);
        messageBubble = view.findViewById(R.id.bubble);
    }

    public void setMessageText(String message){
        messageText.setText(message);
    }

    public void setTimeText(String time){
        timeText.setText(time);
        timeText.setVisibility(View.VISIBLE);
    }

    public void setListener(String time){
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
    }

    public void removeTimeText(){
        timeText.setVisibility(View.GONE);
    }

}
