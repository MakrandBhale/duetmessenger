package com.makarand.duetmessenger.ViewHolder;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.makarand.duetmessenger.R;
import com.vanniktech.emoji.EmojiTextView;

public class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
    public EmojiTextView messageText;
    public TextView timeText;
    public TextView dateText;
    //public LinearLayout messageBubble;
    public LinearLayout timeDateContainer;

    public ReceivedMessageViewHolder(@NonNull View view) {
        super(view);
        messageText = view.findViewById(R.id.message_text);
        timeText = view.findViewById(R.id.message_time);
        dateText = view.findViewById(R.id.message_date);
        //messageBubble = view.findViewById(R.id.bubble);
        timeDateContainer = view.findViewById(R.id.date_time_container);
    }
}
