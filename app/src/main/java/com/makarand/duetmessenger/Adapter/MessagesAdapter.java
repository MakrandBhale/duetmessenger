package com.makarand.duetmessenger.Adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.makarand.duetmessenger.Helper.Constants;
import com.makarand.duetmessenger.Model.Message;
import com.makarand.duetmessenger.R;
import com.makarand.duetmessenger.ViewHolder.ReceivedMessageViewHolder;
import com.makarand.duetmessenger.ViewHolder.SentMessageViewHolder;
import com.makarand.duetmessenger.ViewHolder.TypingIndicatorViewHolder;

import java.util.ArrayList;

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<Message> messageArrayList = new ArrayList<>();
    private String myUid;

    public MessagesAdapter(String myUid){
        this.myUid = myUid;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        switch (viewType){
            case R.layout.item_message_bubble_sent:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_bubble_sent,parent, false);
                return new SentMessageViewHolder(view);
            case R.layout.item_message_bubble_received:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_bubble_received,parent, false);
                return new ReceivedMessageViewHolder(view);

            case R.layout.typing_indicator_item:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.typing_indicator_item,parent, false);
                return new TypingIndicatorViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message currentMessage = messageArrayList.get(position);

        if(currentMessage == null) return;

        switch (currentMessage.getMessageType()){
            case Constants.TYPING:
                //((TypingIndicatorViewHolder) holder).startAnimation();
                break;
            case Constants.NORMAL_MESSAGE:
                if(currentMessage.getSender().equals(myUid)){
                    setupMyMessages(holder, currentMessage, position);
                } else {
                    ((ReceivedMessageViewHolder) holder).messageText.setText(currentMessage.getMessage());
                }
                break;
        }

    }

    private void setupMyMessages(RecyclerView.ViewHolder holder, Message currentMessage, int pos) {
        ((SentMessageViewHolder) holder).messageText.setText(currentMessage.getMessage());
        Message prevMessage = messageArrayList.get(pos);
        if(currentMessage.isLateReply(prevMessage)){
            //holder.
        }
    }

    @Override
    public int getItemCount() {
        return messageArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message currentMessage = messageArrayList.get(position);
        if(currentMessage.getMessageType() == Constants.TYPING_MESSAGE){
            return R.layout.typing_indicator_item;
        }

        if(currentMessage.getSender().equals(myUid)){
            return R.layout.item_message_bubble_sent;
        }

        return R.layout.item_message_bubble_received;
    }

    public void addNewMessage(Message message){
        int index = messageArrayList.size();
        messageArrayList.add(index, message);
        this.notifyItemInserted(index);
    }

    public void updateMessage(Message message){
        int index = messageArrayList.indexOf(message);

        messageArrayList.set(index, message);
        this.notifyItemChanged(index);
    }


    public void showTypingIndicator() {
        Message typingMessage = new Message();
        typingMessage.setMessageType(Constants.TYPING_MESSAGE);
        this.addNewMessage(typingMessage);
    }

    public void hideTypingIndicator() {
        int lastIndex = messageArrayList.size() - 1;
        messageArrayList.remove(lastIndex);
        this.notifyItemRemoved(lastIndex);
    }
}
