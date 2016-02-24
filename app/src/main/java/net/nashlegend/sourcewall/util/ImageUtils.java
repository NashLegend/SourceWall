package net.nashlegend.sourcewall.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.CircleBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import net.nashlegend.sourcewall.R;

public class ImageUtils {

    public static DisplayImageOptions defaultImageOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();

    public static DisplayImageOptions articleTitleImageOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();

    public static DisplayImageOptions avatarOptions = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.default_avatar)
            .showImageForEmptyUri(R.drawable.default_avatar)
            .showImageOnFail(R.drawable.default_avatar)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .displayer(new CircleBitmapDisplayer())//是否设置为圆角，弧度为多少
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();

    public static DisplayImageOptions downloadOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(false)
            .cacheOnDisk(true)
            .build();

    /**
     * 将图片转换为圆角, 当radiusRat为半径时为圆形.
     *
     * @param bitmap
     * @param strokeColor 边框颜色
     * @param strokeWidth 边框宽度
     * @param isCircle    是否圆形
     *
     * @return 转换后的bitmap
     */
    public static Bitmap convertImgRound(Bitmap bitmap, int strokeColor, float strokeWidth, boolean isCircle) {
        Bitmap roundBitmap = null;
        if (bitmap != null) {
            // 画图
            roundBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas userIconCanvas = new Canvas(roundBitmap);
            Paint userIconPaint = new Paint();
            userIconPaint.setAntiAlias(true);
            int bitWidth = bitmap.getWidth();
            Rect rect = new Rect(0, 0, bitWidth, bitWidth);
            RectF rectF = new RectF(rect);
            userIconCanvas.drawARGB(0, 0, 0, 0);
            int radiusRat = bitWidth / 10;
            if (isCircle) {
                radiusRat = bitWidth;
            }
            userIconCanvas.drawRoundRect(rectF, radiusRat, radiusRat, userIconPaint);
            userIconPaint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
            userIconCanvas.drawBitmap(bitmap, rect, rect, userIconPaint);

            // 图片加边框
            if (strokeWidth > 0) {
                Canvas canvas = new Canvas(roundBitmap);
                Paint paint = new Paint(Paint.DITHER_FLAG);
                paint.setDither(true);
                paint.setAntiAlias(true);
                paint.setFilterBitmap(true);
                paint.setColor(strokeColor);
                paint.setStrokeWidth(strokeWidth);
                paint.setStyle(Style.STROKE);
                if (isCircle) {
                    float cx = roundBitmap.getWidth() / 2;
                    float radius = cx - strokeWidth + 1.5F;
                    canvas.drawCircle(cx, cx, radius, paint);
                } else {
                    canvas.drawRoundRect(rectF, radiusRat, radiusRat, paint);
                }
            }

        }
        return roundBitmap;
    }
}
