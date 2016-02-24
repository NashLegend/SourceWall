package net.nashlegend.sourcewall.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by NashLegend on 16/2/24.
 */
public class ArticleImage extends ImageView {

    private int fixedWidth = 0;

    public ArticleImage(Context context) {
        super(context);
    }

    public ArticleImage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ArticleImage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ArticleImage(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public int getFixedWidth() {
        return fixedWidth;
    }

    public void setFixedWidth(int fixedWidth) {
        this.fixedWidth = fixedWidth;
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        if (drawable != null && drawable.getIntrinsicWidth() > 0 && drawable.getIntrinsicHeight() > 0 && fixedWidth > 0) {
            int fixedHeight = fixedWidth * drawable.getIntrinsicHeight() / drawable.getIntrinsicWidth();
            drawable.setBounds(0, 0, fixedWidth, fixedHeight);
            ViewGroup.LayoutParams params = getLayoutParams();
            if (params != null) {
                params.height = fixedHeight;
                requestLayout();
            }
        }
        super.setImageDrawable(drawable);
    }
}
