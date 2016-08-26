package net.nashlegend.sourcewall.view.common;

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
import android.util.Base64;
import android.view.MotionEvent;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.umeng.analytics.MobclickAgent;

import net.nashlegend.sourcewall.App;
import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.activities.ImageActivity;
import net.nashlegend.sourcewall.util.Config;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.Consts.Extras;
import net.nashlegend.sourcewall.util.DisplayUtil;
import net.nashlegend.sourcewall.util.ImageSizeMap;
import net.nashlegend.sourcewall.util.Mob;
import net.nashlegend.sourcewall.util.UiUtil;
import net.nashlegend.sourcewall.util.UrlCheckUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.net.URLDecoder;
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
    private static final float ImageDensity = 2.0f;//图片显示的像素密度
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
        if (content.contains("<img")) {
            htmlTask = new HtmlLoaderTask();
            htmlTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, content);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        linkHit = false;
        boolean res = super.onTouchEvent(event);
        if (noConsumeNonUrlClicks) {
            return linkHit;
        }
        return res;
    }

    private void cancelPotentialTask() {
        if (htmlTask != null && htmlTask.getStatus() == AsyncTask.Status.RUNNING) {
            htmlTask.cancel(false);
        }
    }

    class HtmlLoaderTask extends AsyncTask<String, Integer, CharSequence> {

        boolean someImageLoaded = false;//是否有图片加载出来

        @Override
        protected void onPreExecute() {
            someImageLoaded = false;
        }

        @Override
        protected CharSequence doInBackground(String... params) {
            Spanned spanned = Html.fromHtml(params[0], imageGetter, null);
            CharSequence result = trimEnd(spanned);
            if (!Config.shouldLoadImage() && !someImageLoaded) {
                //如果为无图模式，并且没有图加载了出来（缓存中没有或者加载失败）那么就将result设为null
                //如果为null，那么将不会setText。
                result = null;
            }
            return result;
        }

        @Override
        protected void onPostExecute(CharSequence spanned) {
            if (spanned != null) {
                setText(spanned);
            }
        }

        Html.ImageGetter imageGetter = new Html.ImageGetter() {
            @Override
            public Drawable getDrawable(String source) {
                if (Config.shouldLoadImage()) {
                    return getOnlineOrCachedDrawable(source);
                } else {
                    //无图模式下要这样的话，如果完全没有图，那么也会加载两遍
                    return getEmptyOrCachedDrawable(source);
                }
            }
        };

        /**
         * 获取在线或者缓存的图片，有图模式用
         *
         * @param source
         * @return
         */
        private Drawable getOnlineOrCachedDrawable(String source) {
            someImageLoaded = true;
            float stretch = DisplayUtil.getPixelDensity(App.getApp());
            Drawable drawable = null;
            try {
                if (source.startsWith("http")) {
                    Point point = ImageSizeMap.get(source);
                    Bitmap bitmap;
                    maxWidth = getMaxImageWidth();
                    if (point != null && point.x > 0 && point.y > 0) {
                        bitmap = ImageLoader.getInstance().loadImageSync(source, new ImageSize(point.x, point.y));
                    } else {
                        bitmap = ImageLoader.getInstance().loadImageSync(source, new ImageSize((int) Math.min(maxWidth, maxWidth * ImageDensity / stretch), 4096));
                    }
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
                    }
                } else {
                    if (source.startsWith("data:image/")) {
                        source = URLDecoder.decode(source, "utf-8");
                        String encodedBitmap = source.replaceAll("data:image/\\w{3,4};base64,", "");
                        byte[] data = Base64.decode(encodedBitmap, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        drawable = new BitmapDrawable(getContext().getResources(), bitmap);
                        int width;
                        int height;
                        width = (int) (drawable.getIntrinsicWidth() * stretch);
                        height = (int) (drawable.getIntrinsicHeight() * stretch);
                        if (width <= 0) {
                            return null;
                        } else {
                            if (width > maxWidth) {
                                height *= (maxWidth / width);
                                width = (int) maxWidth;
                            }
                            drawable.setBounds(0, 0, width, height);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (drawable == null) {
                drawable = getContext().getResources().getDrawable(R.drawable.ic_broken_image_24dp);
                if (drawable != null) {
                    int width = drawable.getIntrinsicWidth();
                    int height = drawable.getIntrinsicHeight();
                    drawable.setBounds(0, 0, width, height);
                }
            }
            return drawable;
        }

        /**
         * 获取空白的或者缓存的图片，在无图模式中使用
         *
         * @param source
         * @return
         */
        private Drawable getEmptyOrCachedDrawable(String source) {
            //是否有缓存
            if (source.startsWith("http")) {
                File schrodingerFile = ImageLoader.getInstance().getDiskCache().get(source);
                if (schrodingerFile != null && schrodingerFile.exists()) {
                    //有缓存
                    return getOnlineOrCachedDrawable(source);
                } else {
                    //无缓存
                    return getEmptyDrawable(source);
                }
            } else if (source.startsWith("data:image/")) {
                //无图模式下，base64的图片还是要显示的，毕竟加载都加载了
                return getOnlineOrCachedDrawable(source);
            } else {
                return getEmptyDrawable(source);
            }

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
            return getEmptyDrawable(source);
        }
    };

    /**
     * 获取空白的图片，在图片加载完成前或者无图模式下用
     *
     * @param source
     * @return
     */
    private Drawable getEmptyDrawable(String source) {
        //这是图片格式
        //http://2.im.guokr.com/xxx.jpg?imageView2/1/w/480/h/329
        float stretch = DisplayUtil.getPixelDensity(App.getApp());
        maxWidth = getMaxImageWidth();
        Drawable drawable;
        if (source.startsWith("http")) {
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
        if (result > 4096) {
            result = 4096;
        }
        return result;
    }

    private static void handleURLSpanClick(URLSpan urlSpan) {
        UrlCheckUtil.redirectRequest(urlSpan.getURL());
    }

    private static void handleImageSpanClick(TextView textView, ImageSpan imageSpan) {
        if (UiUtil.shouldThrottle()) {
            return;
        }
        if (textView instanceof TTextView) {
            String html = ((TTextView) textView).html;
            String clickedUrl = imageSpan.getSource();
            if (isImageSrcValid(clickedUrl) && !TextUtils.isEmpty(html)) {
                Document doc = Jsoup.parse(html);
                Elements elements = doc.getElementsByTag("img");
                ArrayList<String> images = new ArrayList<>();
                int clickedPosition = 0;
                for (int i = 0; i < elements.size(); i++) {
                    Element element = elements.get(i);
                    String src = element.attr("src");
                    if (isImageSrcValid(src)) {
                        if (src.equals(clickedUrl)) {
                            clickedPosition = images.size();
                        }
                        images.add(src);
                    }
                }
                if (images.size() > 0) {
                    Intent intent = new Intent();
                    intent.putStringArrayListExtra(Extras.Extra_Image_String_Array, images);
                    intent.putExtra(Extras.Extra_Image_Current_Position, clickedPosition);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Context context = textView.getContext();
                    MobclickAgent.onEvent(context, Mob.Event_Open_Image_From_TextView);
                    if (context != null && context instanceof Activity) {
                        intent.setClass(context, ImageActivity.class);
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(App.getApp(), R.anim.scale_in_center, 0);
                        ActivityCompat.startActivity((Activity) context, intent, options.toBundle());
                    } else {
                        intent.setClass(App.getApp(), ImageActivity.class);
                        App.getApp().startActivity(intent);
                    }
                }
            }
        }
    }

    private static boolean isImageSrcValid(String src) {
        return !TextUtils.isEmpty(src) && src.startsWith("http") || src.startsWith("data:image/");
    }

    public static class LocalLinkMovementMethod extends LinkMovementMethod {
        static LocalLinkMovementMethod sInstance;

        public static LocalLinkMovementMethod getInstance() {
            if (sInstance == null) {
                sInstance = new LocalLinkMovementMethod();
            }
            return sInstance;
        }

        @Override
        public boolean onTouchEvent(@NonNull TextView widget, @NonNull Spannable spannable, @NonNull MotionEvent event) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
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
                        Selection.setSelection(spannable, spannable.getSpanStart(link[0]), spannable.getSpanEnd(link[0]));
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

