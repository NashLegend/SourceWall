package net.nashlegend.sourcewall.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Base64;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.nostra13.universalimageloader.core.ImageLoader;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.commonview.LoadingView;
import net.nashlegend.sourcewall.commonview.ScalingImage;
import net.nashlegend.sourcewall.swrequest.ResponseObject;
import net.nashlegend.sourcewall.util.Config;
import net.nashlegend.sourcewall.util.DisplayUtil;
import net.nashlegend.sourcewall.util.ImageUtils;

import java.io.File;
import java.net.URLDecoder;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * Created by NashLegend on 2015/3/31 0031
 */
public class ImageViewer extends FrameLayout implements LoadingView.ReloadListener, View.OnClickListener {
    ScalingImage imageView;
    GifImageView gifImageView;
    LoadingView loadingView;
    LoaderTask task;
    String url = "";
    int doubleTapZoomDpi = 96;
    GestureDetector gestureDetector;
    private SimpleOnGestureListener gestureListener = new SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            performClick();
            return true;
        }
    };

    private OnTouchListener touchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            gestureDetector.onTouchEvent(event);
            return true;
        }
    };

    public ImageViewer(final Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_image_viewer, this);
        gestureDetector = new GestureDetector(getContext(), gestureListener);
        imageView = (ScalingImage) findViewById(R.id.zoom_image);
        gifImageView = (GifImageView) findViewById(R.id.gifImage);
        gifImageView.setVisibility(VISIBLE);
        imageView.setVisibility(GONE);
        imageView.setMinimumDpi(doubleTapZoomDpi);
        imageView.setDoubleTapZoomDpi(doubleTapZoomDpi);
        loadingView = (LoadingView) findViewById(R.id.image_loading);
        imageView.setOnClickListener(this);
        gifImageView.setOnTouchListener(touchListener);
        this.setOnTouchListener(touchListener);
        gifImageView.setOnClickListener(this);
        setOnClickListener(this);
    }

    public void load(String imageUrl) {
        loadingView.startLoading();
        url = imageUrl;
        if (url.startsWith("http")) {
            task = new LoaderTask();
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
        } else if (url.startsWith("data:image/")) {
            try {
                url = URLDecoder.decode(url, "utf-8");
                String encodedBitmap = url.replaceAll("data:image/\\w{3,4};base64,", "");
                byte[] data = Base64.decode(encodedBitmap, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                gifImageView.setVisibility(GONE);
                imageView.setVisibility(VISIBLE);
                imageView.setImage(ImageSource.bitmap(bitmap));
                properScale(bitmap);
                loadingView.onLoadSuccess();
            } catch (Exception e) {
                loadingView.onLoadFailed();
            }
        }
    }

    public void unload() {
        if (task != null && task.getStatus() == AsyncTask.Status.RUNNING) {
            task.cancel(true);
        }
    }

    @Override
    public void reload() {
        load(url);
    }

    @Override
    public void onClick(View v) {
        tapFinish();
    }

    private void tapFinish() {
        Context ctx = getContext();
        if (ctx instanceof Activity) {
            ((Activity) ctx).finish();
        }
    }

    private void properScale(Bitmap bitmap) {
        properScale(bitmap.getWidth(), bitmap.getHeight());
    }

    private void properScale(File imageFile) {
        Point point = ImageUtils.getImageFileScale(imageFile);
        if (point != null) {
            properScale(point.x, point.y);
        } else {
            imageView.setDoubleTapZoomDpi(doubleTapZoomDpi);
        }
    }

    private void properScale(int width, int height) {
        if (width > 0 && height > 0 && (height + 0.0) / width > Config.longImageRatio) {
            int sw = DisplayUtil.getScreenWidth(getContext());
            float density = DisplayUtil.getPixelDensity(getContext());
            float minDensity = doubleTapZoomDpi / 160;
            if (sw <= width * density / minDensity) {
                //如果默认情况下图片双击会超过屏幕，那么就不让他超过
                imageView.setDoubleTapZoomScale(sw / width);
                return;
            }
        }
        imageView.setDoubleTapZoomDpi(doubleTapZoomDpi);
    }

    class LoaderTask extends AsyncTask<String, Integer, ResponseObject<File>> {

        @Override
        protected ResponseObject<File> doInBackground(String... params) {
            ResponseObject<File> resultObject = new ResponseObject<>();
            try {
                String url = params[0];
                File tmpFile = ImageLoader.getInstance().getDiskCache().get(url);
                if (tmpFile != null && tmpFile.exists()) {
                    resultObject.ok = true;
                    resultObject.result = tmpFile;
                } else {
                    ImageLoader.getInstance().loadImageSync(url, ImageUtils.downloadOptions);
                    tmpFile = ImageLoader.getInstance().getDiskCache().get(url);
                    if (tmpFile != null && tmpFile.exists()) {
                        resultObject.ok = true;
                        resultObject.result = tmpFile;
                    }
                }
            } catch (Exception ignored) {

            }
            return resultObject;
        }

        @Override
        protected void onPostExecute(ResponseObject<File> result) {
            if (result.ok) {
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
                    try {
                        GifDrawable gifDrawable = new GifDrawable(result.result);
                        int initWidth = gifDrawable.getIntrinsicWidth();
                        int initHeight = gifDrawable.getIntrinsicHeight();
                        if (initWidth > 0 && initHeight > 0) {
                            //以96dpi或者更高dpi显示gif图，以不超出屏幕为准
                            float rat = DisplayUtil.getPixelDensity(getContext()) * 1.66f;
                            int minScaledWidth = (int) (DisplayUtil.getScreenWidth(getContext()) / rat);
                            if (minScaledWidth > initWidth) {
                                //如果gif宽度不足以占满屏幕宽度
                                ViewGroup.LayoutParams params = gifImageView.getLayoutParams();
                                if (params != null) {
                                    params.width = (int) (initWidth * rat);
                                    params.height = (int) (initHeight * rat);
                                    gifImageView.setLayoutParams(params);
                                }
                            }
                        }
                        gifImageView.setImageDrawable(gifDrawable);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    gifImageView.setVisibility(GONE);
                    imageView.setVisibility(VISIBLE);
                    imageView.setImage(ImageSource.uri(Uri.fromFile(result.result)));
                    properScale(result.result);
                }
            } else {
                loadingView.onLoadFailed();
            }
        }
    }
}
