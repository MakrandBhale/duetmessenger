package com.makarand.duetmessenger.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.makarand.duetmessenger.Helper.Constants;
import com.makarand.duetmessenger.Model.Message;
import com.makarand.duetmessenger.R;
import com.makarand.duetmessenger.ViewHolder.MessageListViewHolder;
//import com.makarand.duetmessenger.ViewHolder.TypingIndicatorViewHolder;
import java.util.ArrayList;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListViewHolder> {
    private ArrayList<Message> messageArrayList;
    private String myUid;
    private Context context;
    private int typingIndicatorIndex = -1;
    private int myLastMessageIndex = -1;
    private int statusDateShowingAtIndex = -1;
    private boolean isTypingIndicatorShowing;
    public MessageListAdapter(String myUid, Context context) {
        this.myUid = myUid;
        this.messageArrayList = new ArrayList<>();
        this.context = context;
    }

    @NonNull
    @Override
    public MessageListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        switch (viewType) {
            case R.layout.item_message_bubble_sent:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_bubble_sent, parent, false);
                break;
            case R.layout.item_message_bubble_received:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_bubble_received, parent, false);
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + viewType);
        }

        return new MessageListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageListViewHolder holder, int position) {
        Message currentMessage = messageArrayList.get(position);

        ArrayList<String> formattedDate = currentMessage.getFormattedDate();

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
        holder.setMessageText(currentMessage.getMessage());

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
            return R.layout.typing_indicator_item;
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
        messageArrayList.add(message);
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
            if(message.getSender().equals(myUid)){
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
            if(lastMessage.getSender().equals(myUid))
                return index;
            index--;
        }
        return -1;
    }

    public void hideTypingIndicator() {

    }

    public void addOldMessage(Message message) {
        this.messageArrayList.add(0, message);
        this.notifyItemInserted(0);
    }
}
