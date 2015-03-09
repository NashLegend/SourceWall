package net.nashlegend.sourcewall.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.nashlegend.sourcewall.AppApplication;
import net.nashlegend.sourcewall.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by NashLegend on 2015/1/4 0004
 * 用来给TextView显示html外加图片的类
 */
public class TextHtmlHelper {
    private TextView textView;
    private double maxWidth;
    private Context context;
    private HtmlLoaderTask htmlTask;

    public TextHtmlHelper(Context context) {
        this.context = context;
    }

    public void load(TextView tv, String content) {
        cancelPotentialTask();
        textView = tv;
        maxWidth = getMaxWidth();
        Spanned spanned = correctLinkPaths(Html.fromHtml(content, emptyImageGetter, null));
        CharSequence charSequence = trimEnd(spanned);
        textView.setText(charSequence);
        if (Config.shouldLoadImage() && content.contains("<img")) {
            htmlTask = new HtmlLoaderTask();
            htmlTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, content);
        }
    }

    private void cancelPotentialTask() {
        if (htmlTask != null && htmlTask.getStatus() == AsyncTask.Status.RUNNING) {
            htmlTask.cancel(false);
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

    Html.ImageGetter emptyImageGetter = new Html.ImageGetter() {
        @Override
        public Drawable getDrawable(String source) {
            //这是图片格式
            //http://2.im.guokr.com/xxx.jpg?imageView2/1/w/480/h/329
            float stretch = DisplayUtil.getPixelDensity(AppApplication.getApplication());
            maxWidth = getMaxWidth();
            String reg = ".+/w/(\\d+)/h/(\\d+)";
            Matcher matcher = Pattern.compile(reg).matcher(source);
            if (Config.shouldLoadImage() && matcher.find()) {
                int width = (int) (Integer.valueOf(matcher.group(1)) * stretch);
                int height = (int) (Integer.valueOf(matcher.group(2)) * stretch);
                if (width > maxWidth) {
                    height *= (maxWidth / width);
                    width = (int) maxWidth;
                }
                ColorDrawable drawable = new ColorDrawable(0);//透明
                drawable.setBounds(0, 0, width, height);
                return drawable;
            } else {
                Drawable drawable = context.getResources().getDrawable(R.drawable.default_text_image);
                int width = drawable.getIntrinsicWidth();
                int height = drawable.getIntrinsicHeight();
                drawable.setBounds(0, 0, width, height);
                return drawable;
            }
        }
    };

    Html.ImageGetter imageGetter = new Html.ImageGetter() {
        @Override
        public Drawable getDrawable(String source) {
            float stretch = DisplayUtil.getPixelDensity(AppApplication.getApplication());
            maxWidth = getMaxWidth();
            Drawable drawable = null;
            try {
                if (source.startsWith("http")) {
                    Bitmap bitmap = null;
                    bitmap = Picasso.with(context).load(source).resize((int) maxWidth, 0).setTargetSizeAsMax(true).get();
                    if (bitmap != null) {
                        drawable = new BitmapDrawable(context.getResources(), bitmap);
                        String reg = ".+/w/(\\d+)/h/(\\d+)";
                        Matcher matcher = Pattern.compile(reg).matcher(source);
                        int width;
                        int height;
                        if (matcher.find()) {
                            width = (int) (Integer.valueOf(matcher.group(1)) * stretch);
                            height = (int) (Integer.valueOf(matcher.group(2)) * stretch);
                        } else {
                            width = (int) (drawable.getIntrinsicWidth() * stretch);
                            height = (int) (drawable.getIntrinsicHeight() * stretch);
                        }
                        if (width > maxWidth) {
                            height *= (maxWidth / width);
                            width = (int) maxWidth;
                        }
                        drawable.setBounds(0, 0, width, height);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (drawable == null) {
                drawable = context.getResources().getDrawable(R.drawable.broken_image);
                int width = drawable.getIntrinsicWidth();
                int height = drawable.getIntrinsicHeight();
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

    /**
     * 解决相对路径的问题
     *
     * @param spannedText
     * @return
     */
    public static Spanned correctLinkPaths(Spanned spannedText) {
        Object[] spans = spannedText.getSpans(0, spannedText.length(), Object.class);
        for (Object span : spans) {
            int start = spannedText.getSpanStart(span);
            int end = spannedText.getSpanEnd(span);
            int flags = spannedText.getSpanFlags(span);
            if (span instanceof URLSpan) {
                URLSpan urlSpan = (URLSpan) span;
                if (!urlSpan.getURL().startsWith("http")) {
                    if (urlSpan.getURL().startsWith("/")) {
                        urlSpan = new URLSpan("http://www.guokr.com" + urlSpan.getURL());
                    } else {
                        urlSpan = new URLSpan("http://www.guokr.com/" + urlSpan.getURL());
                    }
                }
                ((Spannable) spannedText).removeSpan(span);
                ((Spannable) spannedText).setSpan(urlSpan, start, end, flags);
            }
        }
        return spannedText;
    }
}
