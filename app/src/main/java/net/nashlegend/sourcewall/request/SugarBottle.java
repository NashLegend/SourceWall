package net.nashlegend.sourcewall.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * Created by NashLegend on 16/7/7.
 */

public class SugarBottle implements CookieJar {
    private static final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        List<Cookie> preCookies = cookieStore.get(url.host());//它与cookies都不可变
        ArrayList<Cookie> mergedCookies = new ArrayList<>();
        if (preCookies != null) {
            List<Cookie> deprecatedCookies = new ArrayList<>();
            for (Cookie cookie : cookies) {
                for (Cookie preCookie : preCookies) {
                    if (cookie.name().equals(preCookie.name())) {
                        deprecatedCookies.add(preCookie);
                    }
                }
            }
            mergedCookies.addAll(preCookies);
            mergedCookies.removeAll(deprecatedCookies);
        }
        mergedCookies.addAll(cookies);
        cookieStore.put(url.host(), mergedCookies);
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> cookies = cookieStore.get(url.host());
        return cookies != null ? cookies : new ArrayList<Cookie>();
    }
}
