package com.makarand.duetmessenger.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.makarand.duetmessenger.Helper.MessageQueue;
import com.makarand.duetmessenger.Model.Message;
import com.makarand.duetmessenger.R;
import com.makarand.duetmessenger.ViewHolder.MessageListViewHolder;
import java.util.ArrayList;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListViewHolder> {
    private Context context;
    private ArrayList<Message> messageArrayList;
    private String myUid;
    private static int MY_MESSAGE = 0;
    private static int PARTNER_MESSAGE = 1;
    private MessageQueue q;
    private Message myPrevMessage, myNewMessage;
    private int myPrevMessageIndex = 0;
    public MessageListAdapter(Context context, ArrayList<Message> messageArrayList, String myUid){
        this.context = context;
        this.messageArrayList = messageArrayList;
        this.myUid = myUid;
        this.q = new MessageQueue();
    }

    @NonNull
    @Override
    public MessageListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if(viewType == MY_MESSAGE){
            view = LayoutInflater.from(context).inflate(R.layout.item_message_bubble_sent, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_message_bubble_received, parent, false);
        }

        return new MessageListViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull MessageListViewHolder holder, int position) {
        Message currentMessage = messageArrayList.get(position);

        ArrayList<String> formattedDate = currentMessage.getFormattedDate();

        if(currentMessage.getSender().equals(myUid)){
            if(currentMessage.isShowMessageStatus()){
                Long arrivalTime =(Long) currentMessage.getArrivalTime();
                Long seenTime = (Long) currentMessage.getSeenTime();
                if(seenTime != null){
                    /*Seen time exists means user saw the message, ignore all other times*/
                    String messageSeenTime = currentMessage.getFormattedDate(seenTime);
                    holder.showMessageStatus("Seen • " + messageSeenTime);
                } else if(arrivalTime != null) {
                    /* user has not really seen the message, but message has been delivered.*/
                    String deliveryTime = currentMessage.getFormattedDate(arrivalTime);
                    holder.showMessageStatus("Delivered • " + deliveryTime);
                } else {
                    /*message has not yet been delivered. but sent to database.*/
                    holder.showMessageStatus(currentMessage.getMessageStatus());
                }
            } else holder.hideMessageStatus();
        }

        if(position > 0){
            Message prevMessage = messageArrayList.get(position - 1);
            if(prevMessage != null){
                if(currentMessage.isLateReply(prevMessage)){
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
        setAnimation(holder.itemView, position);
        //myPrevMessage = currentMessage;
        //holder.setListener(formattedDate);
    }

    private void setAnimation(View itemView, int pos) {
        /*TODO: animation in recycler view
        *  */
//        if(pos != messageArrayList.size() - 1) return;
//        Animation fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in);
//        itemView.startAnimation(fadeIn);
    }


    @Override
    public int getItemCount() {
        return messageArrayList.size();
    }

    @Override
    public int getItemViewType(int position){
        if(messageArrayList.get(position).getSender().equals(myUid)){
            // this means the message is sent by me;
            return MY_MESSAGE;
        }
        return PARTNER_MESSAGE;
    }
}
