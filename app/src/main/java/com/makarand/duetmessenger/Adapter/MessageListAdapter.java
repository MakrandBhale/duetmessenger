package com.makarand.duetmessenger.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.makarand.duetmessenger.Helper.Constants;
import com.makarand.duetmessenger.Helper.EndToEnd;
import com.makarand.duetmessenger.Model.Message;
import com.makarand.duetmessenger.R;
import com.makarand.duetmessenger.ViewHolder.MessageListViewHolder;
import com.makarand.duetmessenger.ViewHolder.TypingIndicatorViewHolder;
//import com.makarand.duetmessenger.ViewHolder.TypingIndicatorViewHolder;
import java.util.ArrayList;

public class MessageListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<Message> messageArrayList;
    private String myUid, key;
    private Context context;
    private int typingIndicatorIndex = -1;
    private int myLastMessageIndex = -1;
    private int statusDateShowingAtIndex = -1;
    private boolean isTypingIndicatorShowing;
    public MessageListAdapter(String myUid, Context context, String key) {
        this.myUid = myUid;
        this.messageArrayList = new ArrayList<>();
        this.context = context;
        this.key = key;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        switch (viewType) {
            case R.layout.item_message_bubble_sent:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_bubble_sent, parent, false);
                break;
            case R.layout.item_message_bubble_received:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_bubble_received, parent, false);
                break;
            case R.layout.item_bubble_typing_indicator:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bubble_typing_indicator, parent, false);
                return new TypingIndicatorViewHolder(view);
            default:
                throw new IllegalStateException("Unexpected value: " + viewType);
        }

        return new MessageListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        Message currentMessage = messageArrayList.get(position);

        if(currentMessage.getMessageType() == Constants.TYPING_MESSAGE){
            TypingIndicatorViewHolder holder1 = (TypingIndicatorViewHolder) viewHolder;
            holder1.startAnimation();
            return;
        }
        ArrayList<String> formattedDate = currentMessage.getFormattedDate();
        MessageListViewHolder holder = (MessageListViewHolder) viewHolder;
        if (currentMessage.getSender().equals(myUid)) {
            myLastMessageIndex = position;
            setMessageStatus(currentMessage, holder);
        }

        if (position > 0) {
            Message prevMessage = messageArrayList.get(position - 1);
            if (prevMessage != null) {
                if (currentMessage.isLateReply(prevMessage)) {
                    holder.showTimeText(formattedDate);
                } else {
                    holder.hideTimeText();
                }
            } else {
                holder.showTimeText(formattedDate);
            }
        } else {
            holder.showTimeText(formattedDate);
        }

        if(currentMessage.getImage() != null){
            holder.setImage(currentMessage.getImage(), context);
        } else {
            //Glide.clear(holder.messageImage);
            Glide.with(context).clear(holder.messageImage);
            holder.hideImage();
            holder.messageImage.setImageDrawable(null);
        }

        if(currentMessage.getMessage() == null || currentMessage.getMessage().isEmpty()){
            holder.hideMessageText();
        } else {
            String message = currentMessage.getMessage();
            try {
                EndToEnd endToEnd = new EndToEnd(this.key);
                message = endToEnd.decrypt(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
            holder.setMessageText(message);
        }


        if(currentMessage.getAnimationTechnique() != -1 && !currentMessage.isAnimationShown()){
            holder.startAnimation(currentMessage.getAnimationTechnique());
            currentMessage.setAnimationShown(true);
        }
    }

    private void setMessageStatus(Message currentMessage, MessageListViewHolder holder) {
        Long arrivalTime = (Long) currentMessage.getArrivalTime();
        Long seenTime = (Long) currentMessage.getSeenTime();
        if (currentMessage.getShowMessageStatus()) {
            if (seenTime != null) {
                /*Seen time exists means user saw the message, ignore all other times*/
                String messageSeenTime = currentMessage.getFormattedDate(seenTime);
                //holder.setMessageStatus("Seen • " + messageSeenTime);
                holder.setMessageStatus("Seen");
            } else if (arrivalTime != null) {
                /* user has not really seen the message, but message has been delivered.*/
                String deliveryTime = currentMessage.getFormattedDate(arrivalTime);
                //holder.setMessageStatus("Delivered • " + deliveryTime);
                holder.setMessageStatus("Delivered");
            } else {
                /*message has not yet been delivered. but sent to database.*/
                holder.setMessageStatus(currentMessage.getMessageStatus());
            }
        } else {
            holder.hideMessageStatus();
        }
    }


    @Override
    public int getItemCount() {
        return messageArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageArrayList.get(position);
        if (message.getMessageType() == Constants.TYPING_MESSAGE) {
            return R.layout.item_bubble_typing_indicator;
        } else {
            if (message.getSender().equals(myUid)) {
                return R.layout.item_message_bubble_sent;
            }
            return R.layout.item_message_bubble_received;
        }
    }

    private Message getLastMessage() {
        if (messageArrayList.size() > 0) {
            return messageArrayList.get(messageArrayList.size() - 1);
        }
        return null;
    }

    public void addNewMessage(Message message) {
        //hideTypingIndicator();
        int index = getInsertionIndex();
        messageArrayList.add(index, message);
        int prevMessageIndex = getMyPreviousMessage(index);
        if(message.getSender().equals(myUid) && prevMessageIndex != -1){
            Message prevMessage = messageArrayList.get(prevMessageIndex);
            if(prevMessage.hasSameStatus(message)){
                prevMessage.setShowMessageStatus(false);
            } else {
                prevMessage.setShowMessageStatus(true);
            }
            notifyItemChanged(prevMessageIndex);
        }

        this.notifyItemInserted(index);
    }

    private int getInsertionIndex(){
        if(isTypingIndicatorShowing) {
            return messageArrayList.size() - 1;
        } else {
            return messageArrayList.size();
        }
    }

    public void updateMessage(Message messageToBeUpdated) {
        int index = messageArrayList.indexOf(messageToBeUpdated);
        this.messageArrayList.set(index, messageToBeUpdated);
        int prevMessageIndex = getMyPreviousMessage(index);
        if(messageToBeUpdated.getSender().equals(myUid) && prevMessageIndex != -1){

            Message prevMessage = messageArrayList.get(prevMessageIndex);
            if(prevMessage.hasSameStatus(messageToBeUpdated)){
                prevMessage.setShowMessageStatus(false);
                notifyItemChanged(prevMessageIndex);
            } else {
                prevMessage.setShowMessageStatus(true);
            }

        }

        int nextMessageIndex = getMyNextMessage(index);

        if(messageToBeUpdated.getSender().equals(myUid) && nextMessageIndex != -1){
            Message nextMessage = messageArrayList.get(nextMessageIndex);
            if(nextMessage.hasSameStatus(messageToBeUpdated)){
                nextMessage.setShowMessageStatus(false);
                this.notifyItemChanged(nextMessageIndex);
            }
        } else {
            /*last item*/
            messageToBeUpdated.setShowMessageStatus(true);
        }
        notifyItemChanged(index);
    }

    private int getMyNextMessage(int index){
        index = index + 1;
        while(index < messageArrayList.size() && index >= 0){
            Message message = messageArrayList.get(index);
            if(message.getMessageType() != Constants.TYPING_MESSAGE && message.getSender().equals(myUid)){
                return index;
            }
            index++;
        }
        return -1;
    }

    private int getMyPreviousMessage(int index){
        index = index - 1;
        while(index >= 0){
            Message lastMessage = messageArrayList.get(index);
            if(lastMessage.getMessageType()!= Constants.TYPING_MESSAGE && lastMessage.getSender().equals(myUid))
                return index;
            index--;
        }
        return -1;
    }


    public void addOldMessage(Message message) {
        this.messageArrayList.add(0, message);
        this.notifyItemInserted(0);
    }

    public void hideTypingIndicator() {
        if(isTypingIndicatorShowing){
            int index = messageArrayList.size() - 1;
            messageArrayList.remove(index);
            notifyItemRemoved(index);
        }
        isTypingIndicatorShowing = false;
    }


    public void showTypingIndicator() {
        isTypingIndicatorShowing = true;
        Message message = new Message(Constants.TYPING_MESSAGE);
        messageArrayList.add(message);
        notifyItemInserted(messageArrayList.size() - 1);
    }
}
