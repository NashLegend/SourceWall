package net.nashlegend.sourcewall.request.interceptors;

import android.text.TextUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import static net.nashlegend.sourcewall.BuildConfig.VERSION_CODE;
import static net.nashlegend.sourcewall.BuildConfig.VERSION_NAME;

/**
 * Created by NashLegend on 16/6/29.
 */
public class UserAgentInterceptor implements Interceptor {

    private static String userAgent = null;

    synchronized public static void resetUserAgent() {
        userAgent = null;
    }

    /**
     * 返回默认的UserAgent
     *
     * @return
     */
    synchronized public static String getDefaultUserAgent() {
        if (TextUtils.isEmpty(userAgent)) {
            userAgent = "SourceWall/" + VERSION_NAME + "(" + VERSION_CODE + ")";
        }
        return userAgent;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request().newBuilder()
                .header("User-Agent", getDefaultUserAgent())
                .build();
        return chain.proceed(request);
    }
}