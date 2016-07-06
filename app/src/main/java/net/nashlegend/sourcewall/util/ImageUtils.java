package net.nashlegend.sourcewall.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Environment;
import android.support.annotation.Nullable;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.CircleBitmapDisplayer;

import net.nashlegend.sourcewall.App;
import net.nashlegend.sourcewall.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageUtils {

    public static DisplayImageOptions defaultImageOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .resetViewBeforeLoading(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();

    public static DisplayImageOptions articleTitleImageOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .resetViewBeforeLoading(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();

    public static DisplayImageOptions avatarOptions = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.default_avatar)
            .showImageForEmptyUri(R.drawable.default_avatar)
            .showImageOnFail(R.drawable.default_avatar)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .displayer(new CircleBitmapDisplayer())
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();

    public static DisplayImageOptions downloadOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(false)
            .cacheOnDisk(true)
            .build();

    /**
     * 压缩图片，jpg格式差不多可以压缩到100k左右
     *
     * @param path 要压缩的图片路径
     * @return 是否成功压缩
     * @throws IOException
     */
    public static String compressImage(String path) {
        if (FileUtil.getFileSuffix(new File(path)).equals("gif")) {
            return path;
        }
        float maxSize = Config.getUploadImageSizeRestrict();//将其中一边至少压缩到maxSize，而不是两边都压缩到maxSize，否则有可能图片很不清楚
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        final int outWidth = options.outWidth;
        final int outHeight = options.outHeight;
        final int halfHeight = outHeight / 2;
        final int halfWidth = outWidth / 2;
        int sample = 1;
        while (halfWidth / sample > maxSize && halfHeight / sample > maxSize) {
            sample *= 2;
        }
        if (outWidth > maxSize && outHeight > maxSize) {
            options.inJustDecodeBounds = false;
            options.inSampleSize = sample;
            Bitmap finalBitmap = BitmapFactory.decodeFile(path, options);
            int finalWidth = finalBitmap.getWidth();
            int finalHeight = finalBitmap.getHeight();
            float scale = (finalWidth < finalHeight) ? maxSize / finalWidth : maxSize / finalHeight;
            Matrix matrix = new Matrix();
            matrix.setScale(scale, scale);
            Bitmap compressedBitmap = Bitmap.createBitmap(finalBitmap, 0, 0, finalWidth, finalHeight, matrix, false);

            String parentPath;
            File pFile = null;
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                pFile = App.getApp().getExternalCacheDir();
            }
            if (pFile == null) {
                pFile = App.getApp().getCacheDir();
            }
            parentPath = pFile.getAbsolutePath();
            String cachePath = new File(parentPath, System.currentTimeMillis() + ".jpg").getAbsolutePath();
            FileOutputStream outputStream = null;
            boolean ok = false;
            try {
                outputStream = new FileOutputStream(cachePath);
                compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);//jpg速度远快于png，并且体积要小
                outputStream.flush();
                ok = true;
            } catch (IOException e) {
                ok = false;
            } finally {
                try {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (ok) {
                return cachePath;
            } else {
                return path;
            }
        } else {
            return path;
        }
    }

    /**
     * 将图片转换为圆角, 当radiusRat为半径时为圆形.
     *
     * @param bitmap
     * @param strokeColor 边框颜色
     * @param strokeWidth 边框宽度
     * @param isCircle    是否圆形
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

    @Nullable
    public static Point getImageFileScale(File imageFile) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
            return new Point(options.outWidth, options.outHeight);
        } catch (Exception e) {
            return null;
        }
    }
}
