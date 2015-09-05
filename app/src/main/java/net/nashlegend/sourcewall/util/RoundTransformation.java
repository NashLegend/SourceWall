package net.nashlegend.sourcewall.util;

import android.graphics.Bitmap;

import com.squareup.picasso.Transformation;

public class RoundTransformation implements Transformation {

    private int mStrokeColor;
    private int mStrokeWidth;
    private boolean mIsCircle;

    public RoundTransformation(int strokeColor, int strokeWidth, boolean isCircle) {
        super();
        mStrokeColor = strokeColor;
        mStrokeWidth = strokeWidth;
        mIsCircle = isCircle;
    }

    @Override
    public String key() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bitmap transform(Bitmap bitmap) {
        Bitmap round;
        if (mIsCircle) {
            round = ImageUtils.convertImgRound(bitmap, mStrokeColor, mStrokeWidth, true);
        } else {
            round = ImageUtils.convertImgRound(bitmap, mStrokeColor, mStrokeWidth, false);
        }
        bitmap.recycle();
        bitmap = null;
        return round;
    }

}
