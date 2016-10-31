package net.nashlegend.sourcewall.request;

import net.nashlegend.sourcewall.App;
import net.nashlegend.sourcewall.request.api.UserAPI;
import net.nashlegend.sourcewall.request.cache.RequestCache;
import net.nashlegend.sourcewall.request.interceptors.RedirectInterceptor;
import net.nashlegend.sourcewall.request.interceptors.UserAgentInterceptor;

import java.io.File;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

/**
 * Created by NashLegend on 2014/9/15 0015
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class HttpUtil {

    public final static int DISK_CACHE_SIZE = 50 * 1024 * 1024;//Cache的最大体积,20M
    public final static int CONNECTION_TIMEOUT = 30000;//网络状况差的时候这个时间可能很长
    public final static int SO_TIMEOUT = 60000;
    public final static int WRITE_TIMEOUT = 30000;//
    private static OkHttpClient defaultHttpClient;
    private static CookieManager cookieManager;
    private static CakeBox cookieJar;

    public static void cancel(Object tag) {
        if (tag == null) {
            return;
        }
        OkHttpClient client = getDefaultHttpClient();
        for (Call call : client.dispatcher().queuedCalls()) {
            if (!call.isCanceled() && tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
        for (Call call : client.dispatcher().runningCalls()) {
            if (!call.isCanceled() && tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
    }

    synchronized public static OkHttpClient getDefaultHttpClient() {
        if (defaultHttpClient == null) {
            final File cacheDir = RequestCache.getDiskCacheDir(App.getApp(), "OkHttp.cache");
            defaultHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(new UserAgentInterceptor())
                    .addNetworkInterceptor(new RedirectInterceptor())
                    .cache(new Cache(cacheDir, DISK_CACHE_SIZE))
                    .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
                    .readTimeout(SO_TIMEOUT, TimeUnit.MILLISECONDS)
                    .writeTimeout(WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
                    .cookieJar(getCookieJar())
                    .build();
            setCookie(defaultHttpClient, UserAPI.getCookie());
        }
        return defaultHttpClient;
    }

    synchronized private static CookieJar getCookieJar() {
        if (cookieJar == null) {
            cookieJar = new CakeBox(getCookieManager());
        }
        return cookieJar;
    }

    synchronized private static CookieManager getCookieManager() {
        if (cookieManager == null) {
            cookieManager = new CookieManager();
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        }
        return cookieManager;
    }

    /**
     * @param client
     */
    synchronized public static void setCookie(OkHttpClient client, String rawCookie) {
        List<Cookie> cookies = new ArrayList<>();
        boolean hasToken = false;
        boolean hasUkey = false;
        if (!Utils.isEmpty(rawCookie)) {
            String[] rawCookieParams = rawCookie.split(";");
            for (String rawCookieParam : rawCookieParams) {
                String rawCookieParamNameAndValue[] = rawCookieParam.trim().split("=");
                if (rawCookieParamNameAndValue.length != 2) {
                    continue;
                }
                String paramName = rawCookieParamNameAndValue[0].trim();
                String paramValue = rawCookieParamNameAndValue[1].trim();
                if ("_32353_access_token".equals(paramName) && paramValue.length() > 0) {
                    hasToken = true;
                } else if ("_32353_ukey".equals(paramName) && paramValue.length() > 0) {
                    hasUkey = true;
                }
                cookies.add(new Cookie.Builder().domain("guokr.com").name(paramName).value(paramValue).build());
            }
        }
        if (!hasToken) {
            cookies.add(new Cookie.Builder().domain("guokr.com").name("_32353_access_token").value(UserAPI.getToken()).build());
        }
        if (!hasUkey) {
            cookies.add(new Cookie.Builder().domain("guokr.com").name("_32353_ukey").value(UserAPI.getUkey()).build());
        }
        client.cookieJar().saveFromResponse(HttpUrl.parse("http://www.guokr.com/"), cookies);
        client.cookieJar().saveFromResponse(HttpUrl.parse("http://apis.guokr.com/"), cookies);
        client.cookieJar().saveFromResponse(HttpUrl.parse("http://m.guokr.com/"), cookies);
    }

    /**
     * 清除Cookie
     */
    synchronized public static void clearCookies() {
        getCookieManager().getCookieStore().removeAll();
    }
}
