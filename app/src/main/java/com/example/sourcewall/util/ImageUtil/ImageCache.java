package com.example.sourcewall.util.ImageUtil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import com.example.sourcewall.AppApplication;
import com.example.sourcewall.connection.HttpFetcher;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.net.URLEncoder;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by NashLegend on 2014/9/24 0024
 */
public class ImageCache {

    private final static ConcurrentHashMap<String, SoftReference<BitmapDrawable>> cachedBitmaps = new ConcurrentHashMap<String, SoftReference<BitmapDrawable>>();

    public static BitmapDrawable get(String key) {
        BitmapDrawable bitmap = null;
        if (cachedBitmaps.containsKey(key)) {
            bitmap = cachedBitmaps.get(key).get();
        }
        if (bitmap == null) {
            bitmap = readImageFromFile(key);
        }
        return bitmap;
    }

    public static void add(String key, BitmapDrawable bitmap) {
        cachedBitmaps.put(key, new SoftReference<BitmapDrawable>(bitmap));
    }

    public static String getBitmapCacheFileDir(String url) {
        try {
            return new File(AppApplication.getApplication().getExternalCacheDir(), URLEncoder.encode(url, "UTF-8")).getAbsolutePath();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static BitmapDrawable downloadImageToFile(String url) {
        return downloadImageToFile(url, true);
    }

    public static BitmapDrawable downloadImageToFile(String url, boolean memryCache) {
        BitmapDrawable bitmap = null;
        File file = new File(getBitmapCacheFileDir(url));
        if (HttpFetcher.downloadFile(url, file.getAbsolutePath())) {
            if (file.exists() && file.isFile()) {
                bitmap = new BitmapDrawable(AppApplication.getApplication().getResources(), BitmapFactory.decodeFile(file.getAbsolutePath()));
                if (memryCache) {
                    ImageCache.add(url, bitmap);
                }
            }
        }
        return bitmap;
    }

    protected static BitmapDrawable readImageFromFile(String url) {
        BitmapDrawable bitmap = null;
        File file = new File(getBitmapCacheFileDir(url));
        if (file.exists() && file.isFile()) {
            Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
            bitmap = new BitmapDrawable(AppApplication.getApplication().getResources(), bmp);
            ImageCache.add(url, bitmap);
        }
        return bitmap;
    }

}
