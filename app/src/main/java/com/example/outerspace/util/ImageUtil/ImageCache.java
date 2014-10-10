package com.example.outerspace.util.ImageUtil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.outerspace.AppApplication;
import com.example.outerspace.connection.HttpFetcher;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.net.URLEncoder;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by NashLegend on 2014/9/24 0024.
 */
public class ImageCache {

    private final static ConcurrentHashMap<String, SoftReference<Bitmap>> cachedBitmaps = new ConcurrentHashMap<String, SoftReference<Bitmap>>();

    public static Bitmap get(String key) {
        Bitmap bitmap = null;
        if (cachedBitmaps.containsKey(key)) {
            bitmap = cachedBitmaps.get(key).get();
        }
        if (bitmap == null) {
            bitmap = readImageFromFile(key);
        }
        return bitmap;
    }

    public static void add(String key, Bitmap bitmap) {
        cachedBitmaps.put(key, new SoftReference<Bitmap>(bitmap));
    }

    public static String getBitmapCacheFileDir(String url) {
        try {
            return new File(AppApplication.getApplication().getExternalCacheDir(), URLEncoder.encode(url, "UTF-8")).getAbsolutePath();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static Bitmap downloadImageToFile(String url) {
        return downloadImageToFile(url,true);
    }

    public static Bitmap downloadImageToFile(String url,boolean memryCache) {
        Bitmap bitmap = null;
        File file = new File(getBitmapCacheFileDir(url));
        if (HttpFetcher.downloadFile(url, file.getAbsolutePath())) {
            if (file.exists() && file.isFile()) {
                bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                if (memryCache){
                    ImageCache.add(url, bitmap);
                }
            }
        }
        return bitmap;
    }

    protected static Bitmap readImageFromFile(String url) {
        Bitmap bitmap = null;
        File file = new File(getBitmapCacheFileDir(url));
        if (file.exists() && file.isFile()) {
            bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            ImageCache.add(url, bitmap);
        }
        return bitmap;
    }

}
