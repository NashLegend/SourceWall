package net.nashlegend.sourcewall.commonview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

/**
 * Created by NashLegend on 2015/10/20 0020.
 */
public class ScalingImage extends SubsamplingScaleImageView {
    public ScalingImage(Context context, AttributeSet attr) {
        super(context, attr);
    }

    public ScalingImage(Context context) {
        super(context);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        try {
            return super.onTouchEvent(event);
        } catch (Exception e) {
            return false;
        }
    }
}
