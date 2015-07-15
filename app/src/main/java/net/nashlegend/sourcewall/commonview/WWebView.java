package net.nashlegend.sourcewall.commonview;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import net.nashlegend.sourcewall.AppApplication;
import net.nashlegend.sourcewall.ImageActivity;
import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.util.Config;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.DisplayUtil;
import net.nashlegend.sourcewall.util.UrlCheckUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;

/**
 * Created by NashLegend on 2015/1/4 0004
 * 用于显示内容的
 */
public class WWebView extends WebView {

    long startMills = 0;
    long duration = 500;
    float distance = 10;
    float distanceDP = 4;
    float startX = -100;
    float startY = -100;
    String primarySource = null;
    ArrayList<String> images = null;

    public WWebView(Context context) {
        super(context);
        init();
    }

    public WWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public WWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void setPrimarySource(String primarySource) {
        this.primarySource = primarySource;
    }

    private void init() {
        distance = DisplayUtil.dip2px(distanceDP, getContext());
        setOnClickListener(onClickListener);
        setWebViewClient(client);//一旦设置了webViewClient，默认情况下链接就会在本页打开了……
        getSettings().setAllowFileAccess(true);
        getSettings().setAllowContentAccess(true);
        getSettings().setBlockNetworkImage(true);//暂时不加载图片，因为要延迟加载，只渲染文字还是比较快的
        getSettings().setDefaultTextEncodingName("UTF-8");
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startMills = System.currentTimeMillis();
                startX = event.getX();
                startY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                if (System.currentTimeMillis() - startMills < duration
                        && Math.abs(event.getX() - startX) < distance
                        && Math.abs(event.getY() - startY) < distance) {
                    performClick();
                }
                startMills = 0;
                startX = -100;
                startY = -100;
                break;
            case MotionEvent.ACTION_CANCEL:
                startMills = 0;
                startX = -100;
                startY = -100;
                break;
        }

        return super.onTouchEvent(event);
    }

    OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            HitTestResult result = getHitTestResult();
            if (result != null) {
                if (result.getType() == HitTestResult.IMAGE_TYPE) {
                    String url = result.getExtra();
                    if (!TextUtils.isEmpty(url) && url.startsWith("http")) {
                        onImageClicked(url);
                    }
                }
            }
        }
    };

    public void onImageClicked(String clickedUrl) {
        String html = primarySource;
        int clickedPosition = 0;
        //images不为null说明已经解析过了
        if (images == null) {
            images = new ArrayList<>();
            if (!TextUtils.isEmpty(html)) {
                Document doc = Jsoup.parse(html);
                Elements elements = doc.getElementsByTag("img");
                for (int i = 0; i < elements.size(); i++) {
                    String src = elements.get(i).attr("src");
                    if (!TextUtils.isEmpty(src) && src.startsWith("http")) {
                        images.add(src);
                    }
                }
            }
        }

        for (int i = 0; i < images.size(); i++) {
            String src = images.get(i);
            if (src.equals(clickedUrl)) {
                clickedPosition = i;
                break;
            }
        }

        ArrayList<String> tmpImages = images;
        if (images.size() == 0) {
            tmpImages = new ArrayList<>();
            tmpImages.add(clickedUrl);
            clickedPosition = 0;
        }

        Intent intent = new Intent();
        intent.putStringArrayListExtra(Consts.Extra_Image_String_Array, tmpImages);
        intent.putExtra(Consts.Extra_Image_Current_Position, clickedPosition);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Context context = getContext();
        if (context != null && context instanceof Activity) {
            intent.setClass(context, ImageActivity.class);
            ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(AppApplication.getApplication(), R.anim.scale_in_center, 0);
            ActivityCompat.startActivity((Activity) context, intent, options.toBundle());
        } else {
            intent.setClass(AppApplication.getApplication(), ImageActivity.class);
            AppApplication.getApplication().startActivity(intent);
        }
    }

    public void setExtWebViewClient(WebViewClient client) {
        extClient = client;
    }

    private WebViewClient extClient;
    private WebViewClient client = new WebViewClient() {

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (Config.shouldLoadImage()) {
                //图片在此进行延迟加载
                getSettings().setBlockNetworkImage(false);
            }
            if (extClient != null) {
                extClient.onPageFinished(view, url);
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            UrlCheckUtil.redirectRequest(url);
            if (extClient != null) {
                extClient.shouldOverrideUrlLoading(view, url);
            }
            return true;
        }
    };

}
