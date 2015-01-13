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
import com.squareup.picasso.Picasso;

import java.io.IOException;

/**
 * Created by NashLegend on 2015/1/4 0004
 * 用来给TextView显示html外加图片的类
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
        Spanned spanned = Html.fromHtml(html);
        CharSequence charSequence = trimEnd(spanned);
        textView.setText(charSequence);
        if (html.contains("<img")) {
            htmlTask = new HtmlLoaderTask();
            htmlTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, html);
        }
    }

    /**
     * 省去Html.fromHtml一步，因为已经在fetch数据的时候就已经转换出simpleHtml，在getView时更有效
     *
     * @param tv
     * @param content
     * @param simpleHtml
     */
    public void load(TextView tv, String content, CharSequence simpleHtml) {
        cancelPotentialTask();
        textView = tv;
        html = content;
        maxWidth = getMaxWidth();
        //可是相对路径点击会怎样呢？TODO
//        textView.setMovementMethod(LinkMovementMethod.getInstance());
        CharSequence charSequence = trimEnd(simpleHtml);
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
            return trimEnd(spanned);
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
            maxWidth = getMaxWidth();
            Drawable drawable = null;
            try {
                Bitmap bitmap = Picasso.with(context).load(source).resize((int) maxWidth, 0).setTargetSizeAsMax(true).get();
                if (bitmap != null) {
                    drawable = new BitmapDrawable(context.getResources(), bitmap);
                    int width = (int) (drawable.getIntrinsicWidth() * stretch);
                    int height = (int) (drawable.getIntrinsicHeight() * stretch);
                    if (width > maxWidth) {
                        height *= (maxWidth / width);
                        width = (int) maxWidth;
                    }
                    drawable.setBounds(0, 0, width, height);
                }
            } catch (IOException e) {
                e.printStackTrace();
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
        final int height = options.outHeight;
        final int reqHeight = reqWidth * height / width;
        int inSampleSize = 1;
        if (width > reqWidth) {
            final int halfWidth = width / 2;
            while ((halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
            long totalPixels = width * height / inSampleSize;
            // 超过两倍像素的要接着压缩，也就是说最多1.4倍宽高
            final long totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels > totalReqPixelsCap) {
                inSampleSize *= 2;
                totalPixels /= 2;
            }
        }
        //图片最大不能超过4096，宽度也不可能超过4096，所以不计了
        int mHeight = height / inSampleSize;
        while (mHeight > 4096) {
            inSampleSize *= 2;
            mHeight /= 2;
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
