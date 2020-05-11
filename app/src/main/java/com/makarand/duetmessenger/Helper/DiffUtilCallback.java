package com.makarand.duetmessenger.Helper;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.makarand.duetmessenger.Model.Message;


import java.util.ArrayList;

public class DiffUtilCallback extends DiffUtil.Callback {
    private ArrayList<Message> oldMessageList;
    private ArrayList<Message> newMessageList;

    public DiffUtilCallback(ArrayList<Message> oldMessageList, ArrayList<Message> newMessageList) {
        this.oldMessageList = oldMessageList;
        this.newMessageList = newMessageList;
    }

    @Override
    public int getOldListSize() {
        return oldMessageList.size();
    }

    @Override
    public int getNewListSize() {
        return newMessageList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldMessageList.get(oldItemPosition).getMessageId().equals(newMessageList.get(newItemPosition).getMessageId());

    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        Message oldItem = oldMessageList.get(oldItemPosition);
        Message newItem = newMessageList.get(newItemPosition);
        /*if(!oldItem.getSender().equals(newItem.getSender())){
            return false;
        }
        if(!oldItem.getReceiver().equals(newItem.getReceiver())){
            return false;
        }
        if(!oldItem.getMessage().equals(newItem.getMessage())){
            return false;
        }
        if(!(oldItem.getMessageStatus() == newItem.getMessageStatus())){
            return false;
        }
        if(oldItem.getTimestamp() != null && newItem.getTimestamp() != null) {
            if ((long) oldItem.getTimestamp() != (long) newItem.getTimestamp()) {
                return false;
            }
        }
        if(oldItem.getArrivalTime() != null && newItem.getArrivalTime() != null){
            if((long)oldItem.getArrivalTime()  != (long) newItem.getArrivalTime()){
                return false;
            }
        }
        if(oldItem.getSeenTime() != null && newItem.getSeenTime() != null) {
            if ((long) oldItem.getSeenTime() != (long) newItem.getSeenTime()) {
                return false;
            }
        }

        if(oldItem.getShowMessageStatus() != newItem.getShowMessageStatus()) return false;
*/
        return oldItem.getMessageId().equals(newItem.getMessageId());
    }


    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}