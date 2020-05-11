package com.makarand.duetmessenger.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.makarand.duetmessenger.Helper.Constants;
import com.makarand.duetmessenger.Model.Message;
import com.makarand.duetmessenger.Model.TypingMessage;
import com.makarand.duetmessenger.R;
import com.makarand.duetmessenger.ViewHolder.MessageListViewHolder;
import com.makarand.duetmessenger.ViewHolder.TypingIndicatorViewHolder;
import java.util.ArrayList;

public class MessageListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<Message> messageArrayList;
    private String myUid;
    private Context context;
    private int typingIndicatorIndex = -1;
    private int myLastMessageIndex = -1;
    private boolean isIndicatorShowing = false;
    public MessageListAdapter(String myUid, Context context) {
        this.myUid = myUid;
        this.messageArrayList = new ArrayList<>();
        this.context = context;
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
            case R.layout.typing_indicator_item:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.typing_indicator_item, parent, false);
                return new TypingIndicatorViewHolder(view);

            default:
                throw new IllegalStateException("Unexpected value: " + viewType);
        }

        return new MessageListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        Message currentMessage = messageArrayList.get(position);
        if (currentMessage.getMessageType() == Constants.TYPING_MESSAGE) {
            TypingMessage typingMessage = (TypingMessage) currentMessage;
            ((TypingIndicatorViewHolder) viewHolder).startAnimation(typingMessage.getAvtarLink(), context);
            return;
        }

        MessageListViewHolder holder = (MessageListViewHolder) viewHolder;

        ArrayList<String> formattedDate = currentMessage.getFormattedDate();

        if (currentMessage.getSender().equals(myUid)) {
            myLastMessageIndex = position;
            showMessageStatus(currentMessage, holder);
        }

        if (position > 0) {
            Message prevMessage = messageArrayList.get(position - 1);
            if (prevMessage != null && prevMessage.getMessageType() != Constants.TYPING_MESSAGE) {
                if (currentMessage.isLateReply(prevMessage)) {
                    holder.setTimeText(formattedDate);
                } else {
                    holder.removeTimeText();
                }
            } else {
                holder.setTimeText(formattedDate);
            }
        } else {
            holder.setTimeText(formattedDate);
        }
        holder.setMessageText(currentMessage.getMessage());

        //myPrevMessage = currentMessage;
        //holder.setListener(formattedDate);
    }

    private void showMessageStatus(Message currentMessage, MessageListViewHolder holder) {
        if (currentMessage.isShowMessageStatus()) {
            Long arrivalTime = (Long) currentMessage.getArrivalTime();
            Long seenTime = (Long) currentMessage.getSeenTime();
            if (seenTime != null) {
                /*Seen time exists means user saw the message, ignore all other times*/
                String messageSeenTime = currentMessage.getFormattedDate(seenTime);
                holder.showMessageStatus("Seen • " + messageSeenTime);
            } else if (arrivalTime != null) {
                /* user has not really seen the message, but message has been delivered.*/
                String deliveryTime = currentMessage.getFormattedDate(arrivalTime);
                holder.showMessageStatus("Delivered • " + deliveryTime);
            } else {
                /*message has not yet been delivered. but sent to database.*/
                holder.showMessageStatus(currentMessage.getMessageStatus());
            }
        } else holder.hideMessageStatus();
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

    private int getTypingIndicatorIndex(){
        if(typingIndicatorIndex == -1) return messageArrayList.size();
        else return typingIndicatorIndex++;
    }

    public void addNewMessage(Message message){
        int index = getTypingIndicatorIndex();

        message.setShowMessageStatus(true);

        int lastIndex = index - 1;
        if(lastIndex > 0 && messageArrayList.get(lastIndex).getMessageType() == Constants.NORMAL_MESSAGE){
            Message lastMessage = messageArrayList.get(lastIndex);
            lastMessage.setShowMessageStatus(false);
            this.notifyItemChanged(lastIndex);
        }
        //hideTypingIndicator();
        messageArrayList.add(index, message);
        this.notifyItemInserted(index);

    }

    public void updateMessage(Message messageToBeUpdated){
        int index = messageArrayList.indexOf(messageToBeUpdated);
        if (index == myLastMessageIndex)
            messageToBeUpdated.setShowMessageStatus(true);
        messageArrayList.set(index, messageToBeUpdated);
        this.notifyItemChanged(index);
    }


    public void showTypingIndicator(TypingMessage typingMessage) {
        typingMessage.setMessageType(Constants.TYPING_MESSAGE);
        typingIndicatorIndex = getTypingIndicatorIndex();
        messageArrayList.add(typingIndicatorIndex, typingMessage);
        this.notifyItemInserted(typingIndicatorIndex);
        isIndicatorShowing = true;
    }

    public void hideTypingIndicator() {
        if(typingIndicatorIndex < 0) return;
        messageArrayList.remove(typingIndicatorIndex);
        this.notifyItemRemoved(typingIndicatorIndex);
        typingIndicatorIndex = -1;
        isIndicatorShowing = false;
    }
}
