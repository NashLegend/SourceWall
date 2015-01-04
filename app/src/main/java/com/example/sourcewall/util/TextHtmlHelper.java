package com.example.sourcewall.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html;
import android.text.Spanned;
import android.widget.TextView;

import com.example.sourcewall.AppApplication;
import com.example.sourcewall.util.ImageUtil.ImageCache;

import java.io.File;

/**
 * Created by NashLegend on 2015/1/4 0004
 */
public class TextHtmlHelper {
    private TextView textView;
    private double maxWidth;
    private Context context;
    private HtmlLoaderTask htmlTask;
    private String html;

    public TextHtmlHelper(Context context) {
        this.context = context;
    }

    public void load(TextView tv, String content) {
        cancelPotentialTask();
        this.textView = tv;
        this.html = content;
        this.maxWidth = getMaxWidth();
        Spanned spanned = Html.fromHtml(html, imageGetter, null);
        CharSequence charSequence = trimEnd(spanned);
        textView.setText(charSequence);
        if (html.contains("<img")) {
            htmlTask = new HtmlLoaderTask();
            htmlTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, html);
        }
    }

    private void cancelPotentialTask() {
        if (htmlTask != null && htmlTask.getStatus() == AsyncTask.Status.RUNNING) {
            htmlTask.cancel(true);
        }
    }

    class HtmlLoaderTask extends AsyncTask<String, Integer, CharSequence> {

        @Override
        protected CharSequence doInBackground(String... params) {
            Spanned spanned = Html.fromHtml(params[0], imageGetter, null);
            CharSequence charSequence = trimEnd(spanned);
            return charSequence;
        }

        @Override
        protected void onPostExecute(CharSequence spanned) {
            textView.setText(spanned);
        }
    }

    Html.ImageGetter imageGetter = new Html.ImageGetter() {
        @Override
        public Drawable getDrawable(String source) {
            float stretch = DisplayUtil.getPixelDensity(AppApplication.getApplication());
            Drawable drawable = null;
            File file = new File(ImageCache.getBitmapCacheFileDir(source));
            if (!file.exists()) {
                ImageCache.downloadImageToFile(source, false);
            }
            //通常来说，就算之前textView.getWidth()是小于0的，那么到此处应该已经是正常宽度了
            maxWidth = getMaxWidth();
            if (file.exists()) {
                //防止图片超出屏幕
                drawable = decodeSampledBitmapFromFile(file.getAbsolutePath(), (int) (maxWidth / stretch));
            }

            if (drawable != null) {
                int width = (int) (drawable.getIntrinsicWidth() * stretch);
                int height = (int) (drawable.getIntrinsicHeight() * stretch);
                if (width > maxWidth) {
                    height *= (maxWidth / width);
                    width = (int) maxWidth;
                }
                drawable.setBounds(0, 0, width, height);
            }
            return drawable;
        }
    };

    private double getMaxWidth() {
        int w = textView.getWidth() - textView.getPaddingLeft() - textView.getPaddingRight();
        double result = 0;
        if (textView.getWidth() > 0) {
            if (w > 0) {
                result = w;
            }
        } else {
            result = DisplayUtil.getScreenWidth(context) * 0.8;
        }
        return result;
    }

    private BitmapDrawable decodeSampledBitmapFromFile(String filename, int maxWidth) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);
        options.inSampleSize = calculateInSampleSize(options, maxWidth);
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(filename, options);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(context.getResources(), bitmap);
        return bitmapDrawable;
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth) {
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (width > reqWidth) {
            final int halfWidth = width / 2;
            while ((halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * 消除Html尾部空白
     *
     * @param s
     * @return
     */
    public static CharSequence trimEnd(CharSequence s) {
        int start = 0;
        int end = s.length();
        //消除头部空白
        //while (start < end && Character.isWhitespace(s.charAt(start))) {
        //     start++;
        //}
        while (end > start && Character.isWhitespace(s.charAt(end - 1))) {
            end--;
        }
        return s.subSequence(start, end);
    }
}
