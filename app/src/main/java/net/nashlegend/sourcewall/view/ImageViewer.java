package net.nashlegend.sourcewall.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.commonview.LoadingView;
import net.nashlegend.sourcewall.commonview.ZoomImageView;
import net.nashlegend.sourcewall.model.ImageInfo;
import net.nashlegend.sourcewall.request.ResultObject;
import net.nashlegend.sourcewall.util.ImageFetcher.AsyncTask;

/**
 * Created by NashLegend on 2015/3/31 0031
 */
public class ImageViewer extends FrameLayout implements LoadingView.ReloadListener {
    ZoomImageView imageView;
    LoadingView loadingView;

    public ImageViewer(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_image_viewer, this);
        imageView = (ZoomImageView) findViewById(R.id.zoom_image);
        loadingView = (LoadingView) findViewById(R.id.image_loading);
    }

    @Override
    public void reload() {

    }

    class LoaderTask extends AsyncTask<ImageInfo, Integer, ResultObject> {

        @Override
        protected ResultObject doInBackground(ImageInfo... params) {
            ResultObject resultObject = new ResultObject();
            return resultObject;
        }

        @Override
        protected void onPostExecute(ResultObject resultObject) {
            if (resultObject.ok) {

            } else {

            }
        }
    }
}
