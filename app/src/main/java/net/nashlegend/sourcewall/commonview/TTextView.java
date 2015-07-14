package net.nashlegend.sourcewall.commonview;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.text.Html;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.method.Touch;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.nashlegend.sourcewall.AppApplication;
import net.nashlegend.sourcewall.ImageActivity;
import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.util.Config;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.DisplayUtil;
import net.nashlegend.sourcewall.util.ImageSizeMap;
import net.nashlegend.sourcewall.util.UrlCheckUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by NashLegend on 2015/1/13 0013
 * http://www.cnblogs.com/TerryBlog/archive/2013/04/02/2994815.html
 */
public class TTextView extends TextView {
    boolean noConsumeNonUrlClicks = true;
    boolean linkHit;
    private double maxWidth;
    private HtmlLoaderTask htmlTask;
    String html = "";

    public TTextView(Context context) {
        super(context);
        setMovementMethod(LocalLinkMovementMethod.getInstance());
    }

    public TTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setMovementMethod(LocalLinkMovementMethod.getInstance());
    }

    public TTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setMovementMethod(LocalLinkMovementMethod.getInstance());
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setMovementMethod(LocalLinkMovementMethod.getInstance());
    }

    public void loadHtml(String content) {
        cancelPotentialTask();
        maxWidth = getMaxImageWidth();
        html = content;
        Spanned spanned = correctLinkPaths(Html.fromHtml(content, emptyImageGetter, null));
        CharSequence charSequence = trimEnd(spanned);
        setText(charSequence);
        if (Config.shouldLoadImage() && content.contains("<img")) {
            htmlTask = new HtmlLoaderTask();
            htmlTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, content);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        linkHit = false;
        boolean res = super.onTouchEvent(event);
        if (noConsumeNonUrlClicks)
            return linkHit;
        return res;
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
            setText(spanned);
        }
    }

    /**
     * 消除Html尾部空白
     *
     * @param s 要处理的html span
     * @return 处理过的span
     */
    public static CharSequence trimEnd(CharSequence s) {
        int start = 0;
        int end = s.length();
        while (end > start && Character.isWhitespace(s.charAt(end - 1))) {
            end--;
        }
        return s.subSequence(start, end);
    }

    /**
     * 解决相对路径的问题
     *
     * @param spannedText 要处理的span
     * @return 处理过的span
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

    /**
     * 空emptyImageGetter，用于获取图片前的尺寸准备或者无图模式下返回一个图标
     */
    Html.ImageGetter emptyImageGetter = new Html.ImageGetter() {
        @Override
        public Drawable getDrawable(String source) {
            //这是图片格式
            //http://2.im.guokr.com/xxx.jpg?imageView2/1/w/480/h/329
            float stretch = DisplayUtil.getPixelDensity(AppApplication.getApplication());
            maxWidth = getMaxImageWidth();
            Drawable drawable = null;
            if (Config.shouldLoadImage()) {
                int width = 0;
                int height = 0;
                String reg = ".+/w/(\\d+)/h/(\\d+)";
                Matcher matcher = Pattern.compile(reg).matcher(source);
                if (matcher.find()) {
                    width = (int) (Integer.valueOf(matcher.group(1)) * stretch);
                    height = (int) (Integer.valueOf(matcher.group(2)) * stretch);
                } else {
                    Point point = ImageSizeMap.get(source);
                    if (point != null) {
                        width = point.x;
                        height = point.y;
                    }
                }
                if (width > 0 && height > 0) {
                    if (width > maxWidth) {
                        height *= (maxWidth / width);
                        width = (int) maxWidth;
                    }
                    drawable = new ColorDrawable(Color.parseColor("#dbdbdb"));//透明
                    drawable.setBounds(0, 0, width, height);
                } else {
                    drawable = getContext().getResources().getDrawable(R.drawable.default_image);
                    if (drawable != null) {
                        width = drawable.getIntrinsicWidth();
                        height = drawable.getIntrinsicHeight();
                        drawable.setBounds(0, 0, width, height);
                    }
                }
            } else {
                drawable = getContext().getResources().getDrawable(R.drawable.default_image);
                if (drawable != null) {
                    int width = drawable.getIntrinsicWidth();
                    int height = drawable.getIntrinsicHeight();
                    drawable.setBounds(0, 0, width, height);
                }
            }
            return drawable;
        }
    };

    Html.ImageGetter imageGetter = new Html.ImageGetter() {
        @Override
        public Drawable getDrawable(String source) {
            float stretch = DisplayUtil.getPixelDensity(AppApplication.getApplication());
            maxWidth = getMaxImageWidth();
            Drawable drawable = null;
            try {
                if (source.startsWith("http")) {
                    Bitmap bitmap = Picasso.with(getContext()).load(source).resize((int) maxWidth, 0).setTargetSizeAsMax(true).get();
                    if (bitmap != null) {
                        String reg = ".+/w/(\\d+)/h/(\\d+)";
                        Matcher matcher = Pattern.compile(reg).matcher(source);
                        float width;
                        float height;
                        if (matcher.find()) {
                            width = Integer.valueOf(matcher.group(1)) * stretch;
                            height = Integer.valueOf(matcher.group(2)) * stretch;
                        } else {
                            width = bitmap.getWidth() * stretch;
                            height = bitmap.getHeight() * stretch;
                        }
                        if (width > maxWidth) {
                            height *= (maxWidth / width);
                            width = (int) maxWidth;
                        }
                        ImageSizeMap.put(source, (int) width, (int) height);

                        String realLink = source.replaceAll("\\?.*$", "");
                        String suffix = "";
                        int offset = realLink.lastIndexOf(".");
                        if (offset >= 0) {
                            suffix = realLink.substring(offset + 1);
                        }
                        if ("gif".equalsIgnoreCase(suffix)) {
                            Bitmap tmpBitmap = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);
                            Bitmap indBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.gif_text);
                            Canvas canvas = new Canvas(tmpBitmap);
                            Matrix matrix = new Matrix();
                            matrix.setScale(width / bitmap.getWidth(), height / bitmap.getHeight());
                            canvas.drawBitmap(bitmap, matrix, null);
                            if (width > 2 * indBitmap.getWidth() && height > 2 * indBitmap.getHeight()) {
                                canvas.drawBitmap(indBitmap, 0, 0, null);
                            }
                            drawable = new BitmapDrawable(getContext().getResources(), tmpBitmap);
                            drawable.setBounds(0, 0, (int) width, (int) height);
                        } else {
                            drawable = new BitmapDrawable(getContext().getResources(), bitmap);
                            drawable.setBounds(0, 0, (int) width, (int) height);
                        }
                        return drawable;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (drawable == null) {
                drawable = getContext().getResources().getDrawable(R.drawable.broken_image);
                if (drawable != null) {
                    int width = drawable.getIntrinsicWidth();
                    int height = drawable.getIntrinsicHeight();
                    drawable.setBounds(0, 0, width, height);
                }
            }
            return drawable;
        }
    };

    private double getMaxImageWidth() {
        int w = getWidth() - getPaddingLeft() - getPaddingRight();
        double result = 0;
        if (getWidth() > 0) {
            if (w > 0) {
                result = w;
            }
        } else {
            result = DisplayUtil.getScreenWidth(getContext()) * 0.8;
        }
        return result;
    }

    private static void handleURLSpanClick(URLSpan urlSpan) {
        UrlCheckUtil.redirectRequest(urlSpan.getURL());
    }

    private static void handleImageSpanClick(TextView textView, ImageSpan imageSpan) {
        if (textView instanceof TTextView) {
            String html = ((TTextView) textView).html;
            String clickedUrl = imageSpan.getSource();
            if (!TextUtils.isEmpty(html)) {
                Document doc = Jsoup.parse(html);
                Elements elements = doc.getElementsByTag("img");
                ArrayList<String> images = new ArrayList<>();
                int clickedPosition = 0;
                for (int i = 0; i < elements.size(); i++) {
                    Element element = elements.get(i);
                    String src = element.attr("src");
                    if (!TextUtils.isEmpty(src) && src.startsWith("http")) {
                        if (src.equals(clickedUrl)) {
                            clickedPosition = images.size();
                        }
                        images.add(src);
                    }
                }
                if (images.size() > 0) {
                    Intent intent = new Intent();
                    intent.putStringArrayListExtra(Consts.Extra_Image_String_Array, images);
                    intent.putExtra(Consts.Extra_Image_Current_Position, clickedPosition);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Context context = textView.getContext();
                    if (context != null && context instanceof Activity) {
                        intent.setClass(context, ImageActivity.class);
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(AppApplication.getApplication(), R.anim.scale_in_center, 0);
                        ActivityCompat.startActivity((Activity) context, intent, options.toBundle());
                    } else {
                        intent.setClass(AppApplication.getApplication(), ImageActivity.class);
                        AppApplication.getApplication().startActivity(intent);
                    }
                }

            }
        }
    }

    public static class LocalLinkMovementMethod extends LinkMovementMethod {
        static LocalLinkMovementMethod sInstance;

        public static LocalLinkMovementMethod getInstance() {
            if (sInstance == null)
                sInstance = new LocalLinkMovementMethod();
            return sInstance;
        }

        @Override
        public boolean onTouchEvent(@NonNull TextView widget, @NonNull Spannable spannable, @NonNull MotionEvent event) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_UP ||
                    action == MotionEvent.ACTION_DOWN) {
                int x = (int) event.getX();
                int y = (int) event.getY();
                x -= widget.getTotalPaddingLeft();
                y -= widget.getTotalPaddingTop();
                x += widget.getScrollX();
                y += widget.getScrollY();
                Layout layout = widget.getLayout();
                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);
                URLSpan[] link = spannable.getSpans(off, off, URLSpan.class);
                if (link.length > 0) {
                    if (action == MotionEvent.ACTION_UP) {
                        handleURLSpanClick(link[0]);
                    } else {
                        Selection.setSelection(spannable,
                                spannable.getSpanStart(link[0]),
                                spannable.getSpanEnd(link[0]));
                    }
                    if (widget instanceof TTextView) {
                        ((TTextView) widget).linkHit = true;
                    }
                    return true;
                } else {
                    Selection.removeSelection(spannable);
                    ImageSpan[] images = spannable.getSpans(off, off, ImageSpan.class);
                    if (images.length > 0) {
                        ImageSpan span = images[images.length - 1];//0貌似有时不太管用，images[images.length-1]应该可以解决
                        if (action == MotionEvent.ACTION_UP) {
                            handleImageSpanClick(widget, span);
                        }
                        if (widget instanceof TTextView) {
                            ((TTextView) widget).linkHit = true;
                        }
                        return true;
                    } else {
                        Touch.onTouchEvent(widget, spannable, event);
                        return false;
                    }
                }
            }
            return Touch.onTouchEvent(widget, spannable, event);
        }
    }
}

