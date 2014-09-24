package com.example.outerspace.util.ImageUtil;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * Created by NashLegend on 2014/9/24 0024.
 */
public class LoaderTask extends AsyncTask<String,Integer,Bitmap>{
    private final WeakReference<ImageView> imageViewWeakReference;

    public LoaderTask(ImageView image) {
        this.imageViewWeakReference = new WeakReference<ImageView>(image);
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        return null;
    }
}
