package com.example.sourcewall.util;

import android.net.Uri;

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
    public static boolean shouldRedirectRequest(String url) {
        return false;
    }

    /**
     * 是否拦截打开的链接
     *
     * @param uri
     * @return
     */
    public static boolean shouldRedirectRequest(Uri uri) {
        return false;
    }
}
