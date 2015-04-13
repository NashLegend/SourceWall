package net.nashlegend.sourcewall.view;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.squareup.picasso.Picasso;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.commonview.LoadingView;
import net.nashlegend.sourcewall.commonview.ZoomImageView;
import net.nashlegend.sourcewall.request.RequestCache;
import net.nashlegend.sourcewall.request.ResultObject;

import java.io.File;
import java.io.IOException;

import pl.droidsonroids.gif.GifImageView;

/**
 * Created by NashLegend on 2015/3/31 0031
 */
public class ImageViewer extends FrameLayout implements LoadingView.ReloadListener {
    ZoomImageView imageView;
    GifImageView gifImageView;
    LoadingView loadingView;
    LoaderTask task;
    String url = "";

    public ImageViewer(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_image_viewer, this);
        imageView = (ZoomImageView) findViewById(R.id.zoom_image);
        gifImageView = (GifImageView) findViewById(R.id.gifImage);
        gifImageView.setVisibility(VISIBLE);
        imageView.setVisibility(GONE);
        loadingView = (LoadingView) findViewById(R.id.image_loading);
    }

    public void load(String u) {
        url = u;
        task = new LoaderTask();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
    }

    public void unload() {
        if (task != null && task.getStatus() == AsyncTask.Status.RUNNING) {
            task.cancel(true);
        }
    }

    @Override
    public void reload() {

    }

    class LoaderTask extends AsyncTask<String, Integer, ResultObject> {

        @Override
        protected ResultObject doInBackground(String... params) {
            ResultObject resultObject = new ResultObject();
            String url = params[0];
            String filePath = RequestCache.getInstance().getCachedFile(url);
            if (filePath != null) {
                resultObject.ok = true;
                resultObject.result = filePath;
            } else {
                Picasso.with(getContext()).load(url).download();
                filePath = RequestCache.getInstance().getCachedFile(url);
                if (filePath != null) {
                    resultObject.ok = true;
                    resultObject.result = filePath;
                }
            }
            return resultObject;
        }

        @Override
        protected void onPostExecute(ResultObject resultObject) {
            if (resultObject.ok) {
                loadingView.onLoadSuccess();
                String realLink = url.replaceAll("\\?.*$", "");
                String suffix = "";
                int offset = realLink.lastIndexOf(".");
                if (offset >= 0) {
                    suffix = realLink.substring(offset + 1);
                }
                if ("gif".equalsIgnoreCase(suffix)) {
                    gifImageView.setVisibility(VISIBLE);
                    imageView.setVisibility(GONE);
                    gifImageView.setImageURI(Uri.fromFile(new File((String) resultObject.result)));
                } else {
                    gifImageView.setVisibility(GONE);
                    imageView.setVisibility(VISIBLE);
                    imageView.setImageFile((String) resultObject.result);
                }
            } else {
                loadingView.onLoadFailed();
            }
        }
    }
}
