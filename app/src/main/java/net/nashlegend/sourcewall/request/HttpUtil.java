package net.nashlegend.sourcewall.request;

import net.nashlegend.sourcewall.App;
import net.nashlegend.sourcewall.request.api.UserAPI;
import net.nashlegend.sourcewall.request.cache.RequestCache;

import java.io.File;
import java.io.IOException;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Cookie;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by NashLegend on 2014/9/15 0015
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class HttpUtil {

    private final static int HTTP_RESPONSE_DISK_CACHE_MAX_SIZE = 20 * 1024 * 1024;//Cache的最大体积,20M
    private final static int CONNECTION_TIMEOUT = 30000;//网络状况差的时候这个时间可能很长
    private final static int SO_TIMEOUT = 60000;
    private final static int WRITE_TIMEOUT = 30000;//
    private static OkHttpClient defaultHttpClient;
    private static OkHttpClient uploadHttpClient;
    private static CookieManager cookieManager = new CookieManager();
    private static JavaNetCookieJar cookieJar = new JavaNetCookieJar(cookieManager);

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
                    .addNetworkInterceptor(new RedirectInterceptor())
                    .cache(new Cache(cacheDir, HTTP_RESPONSE_DISK_CACHE_MAX_SIZE))
                    .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
                    .readTimeout(SO_TIMEOUT, TimeUnit.MILLISECONDS)
                    .writeTimeout(WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
                    .cookieJar(cookieJar)
                    .build();
            setCookie(defaultHttpClient);
        }
        return defaultHttpClient;
    }

    synchronized public static OkHttpClient getDefaultUploadHttpClient() {
        if (uploadHttpClient == null) {
            uploadHttpClient = getDefaultHttpClient()
                    .newBuilder()
                    .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
                    .readTimeout(SO_TIMEOUT * 10, TimeUnit.MILLISECONDS)
                    .writeTimeout(WRITE_TIMEOUT * 10, TimeUnit.MILLISECONDS)
                    .build();
        }
        return uploadHttpClient;
    }

    /**
     * @param client
     */
    synchronized public static void setCookie(OkHttpClient client) {
        List<Cookie> cookies = new ArrayList<>();
        cookies.add(new Cookie.Builder().domain("guokr.com").name("_32353_access_token").value(UserAPI.getToken()).build());
        cookies.add(new Cookie.Builder().domain("guokr.com").name("_32353_ukey").value(UserAPI.getUkey()).build());
        try {
            client.cookieJar().saveFromResponse(HttpUrl.parse("http://www.guokr.com/"), cookies);
            client.cookieJar().saveFromResponse(HttpUrl.parse("http://apis.guokr.com/"), cookies);
            client.cookieJar().saveFromResponse(HttpUrl.parse("http://m.guokr.com/"), cookies);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param client
     */
    synchronized public static void clearCookiesForOkHttp(OkHttpClient client) {
        cookieManager.getCookieStore().removeAll();
    }

    static class RedirectInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Response response = chain.proceed(request);
            if (response.isRedirect()) {
                String tmpUrl = chain.request().url().toString();
                tmpUrl = tmpUrl.replaceAll("\\?.+", "");
                String article_reply_reg = "^http://(www|m).guokr.com/article/reply/\\d+/$";//http://www.guokr.com/article/reply/2903740/
                String post_reply_reg = "^http://(www|m).guokr.com/post/reply/\\d+/$";//http://www.guokr.com/post/reply/6148664/
                //上面两条，只有通知才会跳到
                String publish_post_reg = "http://www.guokr.com/group/\\d+/post/edit/";//这是发贴的链接跳转
                boolean flag = tmpUrl.matches(article_reply_reg) || tmpUrl.matches(post_reply_reg) || tmpUrl.matches(publish_post_reg);
                if (flag) {
                    //匹配上了，要重定向，将code设置成200
                    response = response.newBuilder().code(HttpURLConnection.HTTP_OK).build();
                }
            }
            return response;
        }
    }
}
