package com.example.sourcewall.util.ImageUtil;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * Created by NashLegend on 2014/9/24 0024.
 */
public class ImageLoader {

    public static BitmapDrawable getBitmapForUrl(String url) {
        BitmapDrawable bitmap;
        if ((bitmap = ImageCache.get(url)) == null) {
            bitmap = ImageCache.downloadImageToFile(url);
        }
        return bitmap;
    }

    public static void loadImage(ImageView imageView, String url) {

    }

    public static class BitmapLoaderTask extends AsyncTask<String, Integer, Bitmap> {
        WeakReference<AsyncDrawable> drawableReference;

        @Override
        protected Bitmap doInBackground(String... params) {
            return null;
        }
    }

    public static class AsyncDrawable extends BitmapDrawable {
        WeakReference<BitmapLoaderTask> taskReference;
    }
}
