package com.makarand.duetmessenger.Helper;

import android.content.Context;
import android.util.AttributeSet;

import com.airbnb.lottie.LottieAnimationView;

public class SquareLottieView extends LottieAnimationView {

    public SquareLottieView(Context context) {
        super(context);
    }

    public SquareLottieView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareLottieView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        setMeasuredDimension(width, width);
    }


}
