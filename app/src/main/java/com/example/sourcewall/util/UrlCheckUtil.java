package com.example.sourcewall.util;

import android.content.Intent;
import android.net.Uri;
import android.provider.Browser;

import com.example.sourcewall.AppApplication;

/**
 * Created by NashLegend on 2015/1/14 0014
 */
public class UrlCheckUtil {

    /**
     * 是否拦截打开的链接
     *
     * @param url
     * @return
     */
    public static void redirectRequest(String url) {
        redirectRequest(Uri.parse(url));
    }

    /**
     * 是否拦截打开的链接
     *
     * @param uri
     * @return
     */
    public static void redirectRequest(Uri uri) {
        //TODO 可以在此接管链接的跳转
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.putExtra(Browser.EXTRA_APPLICATION_ID, AppApplication.getApplication().getPackageName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        AppApplication.getApplication().startActivity(intent);
    }
}
