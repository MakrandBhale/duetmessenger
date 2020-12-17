package com.makarand.duetmessenger.ViewHolder;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.makarand.duetmessenger.R;
import com.makeramen.roundedimageview.RoundedImageView;

public class TypingIndicatorViewHolder extends RecyclerView.ViewHolder  {
    private TextView one, two, three;
    private RoundedImageView avtarImageView;
    private LinearLayout threeDotsContainer;

    public TypingIndicatorViewHolder(@NonNull View itemView) {
        super(itemView);
        one = itemView.findViewById(R.id.one);
        two = itemView.findViewById(R.id.two);
        three = itemView.findViewById(R.id.three);
        avtarImageView = itemView.findViewById(R.id.avtar_imageview);
        threeDotsContainer = itemView.findViewById(R.id.three_dots_container);
    }


    public void startAnimation(){
        //setAvtar(link, context);

        YoYo.with(Techniques.Bounce)
                .delay(100)
                .duration(1000)
                .repeat(YoYo.INFINITE)
                .playOn(one);
        YoYo.with(Techniques.Bounce)
                .delay(150)
                .duration(1000)
                .repeat(YoYo.INFINITE)
                .playOn(two);
        YoYo.with(Techniques.Bounce)
                .delay(200)
                .duration(1000)
                .repeat(YoYo.INFINITE)
                .playOn(three);
    }

    public void stopAnimation(){
        //threeDotsContainer.setVisibility(View.GONE);
        //avtarImageView.setVisibility(View.GONE);
    }

    private void setAvtar(String link, Context context){
        Glide
                .with(context)
                .load(link)
                .centerCrop()
                .placeholder(R.drawable.ic_user)
                .into(avtarImageView);
    }


}
