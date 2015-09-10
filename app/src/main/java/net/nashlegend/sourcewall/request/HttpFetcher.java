package net.nashlegend.sourcewall.request;

import android.text.TextUtils;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import net.nashlegend.sourcewall.request.api.UserAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by NashLegend on 2014/9/15 0015
 */
public class HttpFetcher {

    private static OkHttpClient defaultHttpClient;
    private static OkHttpClient uploadHttpClient;
    private static final int MAX_EXECUTION_COUNT = 2;
    private final static int CONNECTION_TIMEOUT = 30000;//网络状况差的时候这个时间可能很长
    private final static int SO_TIMEOUT = 60000;
    private final static int UPLOAD_SO_TIMEOUT = 300000;//300秒的上传时间

    public static ResultObject<String> get(String url) throws Exception {
        ResultObject<String> resultObject = new ResultObject<>();
        Request request = new Request.Builder().get().url(url).build();
        Response response = getDefaultHttpClient().newCall(request).execute();
        int statusCode = response.code();
        String result = response.body().string();
        resultObject.statusCode = statusCode;
        resultObject.result = result;
        return resultObject;
    }


    public static ResultObject<String> get(String url, HashMap<String, String> params, boolean needToken) throws Exception {
        StringBuilder paramString = new StringBuilder("");
        String token = UserAPI.getToken();
        if (params == null) {
            params = new HashMap<>();
        }
        if (needToken && !TextUtils.isEmpty(token)) {
            params.put("access_token", token);
        }
        if (params.size() > 0) {
            paramString.append("?");
            for (HashMap.Entry<String, String> entry : params.entrySet()) {
                paramString.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            paramString.deleteCharAt(paramString.length() - 1);
        }
        return get(url + paramString.toString());
    }

    public static ResultObject<String> get(String url, HashMap<String, String> params) throws Exception {
        return get(url, params, true);
    }

    public static ResultObject<String> post(String url, HashMap<String, String> params, boolean needToken) throws Exception {
        ResultObject<String> resultObject = new ResultObject<>();
        String token = UserAPI.getToken();
        if (params == null) {
            params = new HashMap<>();
        }
        if (needToken && !TextUtils.isEmpty(token)) {
            params.put("access_token", token);
        }
        FormEncodingBuilder builder = new FormEncodingBuilder();
        if (params.size() > 0) {
            for (HashMap.Entry<String, String> entry : params.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }
        RequestBody formBody = builder.build();
        Request request = new Request.Builder().post(formBody).url(url).build();
        Response response = getDefaultHttpClient().newCall(request).execute();
        int statusCode = response.code();
        String result = response.body().string();
        resultObject.statusCode = statusCode;
        resultObject.result = result;
        return resultObject;
    }

    public static ResultObject<String> post(String url, HashMap<String, String> params) throws Exception {
        return post(url, params, true);
    }

    public static ResultObject<String> put(String url, HashMap<String, String> params, boolean needToken) throws Exception {
        ResultObject<String> resultObject = new ResultObject<>();
        String token = UserAPI.getToken();
        if (params == null) {
            params = new HashMap<>();
        }
        if (needToken && !TextUtils.isEmpty(token)) {
            params.put("access_token", token);
        }
        FormEncodingBuilder builder = new FormEncodingBuilder();
        if (params.size() > 0) {
            for (HashMap.Entry<String, String> entry : params.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }
        RequestBody formBody = builder.build();
        Request request = new Request.Builder().post(formBody).url(url).build();
        Response response = getDefaultHttpClient().newCall(request).execute();
        int statusCode = response.code();
        String result = response.body().string();
        resultObject.statusCode = statusCode;
        resultObject.result = result;
        return resultObject;
    }

    public static ResultObject<String> put(String url) throws Exception {
        return put(url, null, true);
    }

    public static ResultObject<String> put(String url, HashMap<String, String> params) throws Exception {
        return put(url, params, true);
    }

    /**
     * Delete 也是需要RequestBody的，然而这里并没有……
     */
    public static ResultObject<String> delete(String url) throws Exception {
        ResultObject<String> resultObject = new ResultObject<>();
        Request request = new Request.Builder().delete().url(url).build();
        Response response = getDefaultHttpClient().newCall(request).execute();
        int statusCode = response.code();
        String result = response.body().string();
        resultObject.statusCode = statusCode;
        resultObject.result = result;
        return resultObject;
    }

    public static ResultObject<String> delete(String url, HashMap<String, String> params) throws Exception {
        return delete(url, params, true);
    }

    public static ResultObject<String> delete(String url, HashMap<String, String> params, boolean needToken) throws Exception {
        StringBuilder paramString = new StringBuilder("");
        String token = UserAPI.getToken();
        if (params == null) {
            params = new HashMap<>();
        }
        if (needToken && !TextUtils.isEmpty(token)) {
            params.put("access_token", token);
        }
        if (params.size() > 0) {
            paramString.append("?");
            for (HashMap.Entry<String, String> entry : params.entrySet()) {
                paramString.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            paramString.deleteCharAt(paramString.length() - 1);
        }
        return delete(url + paramString.toString());
    }

    synchronized public static OkHttpClient getDefaultHttpClient() {
        if (defaultHttpClient == null) {
            defaultHttpClient = new OkHttpClient();
            CookieManager cookieManager = new CookieManager();
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
            defaultHttpClient.setCookieHandler(cookieManager);
            defaultHttpClient.networkInterceptors().add(new RedirectInterceptor());
            defaultHttpClient.setConnectTimeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
            defaultHttpClient.setReadTimeout(SO_TIMEOUT, TimeUnit.MILLISECONDS);
            defaultHttpClient.setWriteTimeout(UPLOAD_SO_TIMEOUT, TimeUnit.MILLISECONDS);
            setCookie(defaultHttpClient);
        }
        return defaultHttpClient;
    }

    synchronized public static OkHttpClient getDefaultUploadHttpClient() {
        if (uploadHttpClient == null) {
            uploadHttpClient = new OkHttpClient();
            CookieManager cookieManager = new CookieManager();
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
            uploadHttpClient.setCookieHandler(cookieManager);
            uploadHttpClient.setConnectTimeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
            uploadHttpClient.setReadTimeout(SO_TIMEOUT, TimeUnit.MILLISECONDS);
            uploadHttpClient.setWriteTimeout(UPLOAD_SO_TIMEOUT, TimeUnit.MILLISECONDS);
            setCookie(uploadHttpClient);
        }
        return uploadHttpClient;
    }

    /**
     * @param client
     */
    synchronized public static void setCookie(OkHttpClient client) {
        List<String> values = new ArrayList<>(Arrays.asList("_32353_access_token=" + UserAPI.getToken(), "_32353_ukey=" + UserAPI.getUkey()));
        Map<String, List<String>> cookies = new HashMap<>();
        cookies.put("Set-Cookie", values);
        try {
            client.getCookieHandler().put(new URI("http://.guokr.com"), cookies);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void clearCookiesForOkHttp(OkHttpClient client) {
        if (client.getCookieHandler() != null && client.getCookieHandler() instanceof CookieManager) {
            ((CookieManager) client.getCookieHandler()).getCookieStore().removeAll();
        }
    }

    static class RedirectInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Response response = chain.proceed(request);
            if (response.isRedirect()) {
                String url = chain.request().url().toString();
                String article_reply_reg = "^http://(www|m).guokr.com/article/reply/\\d+/$";//http://www.guokr.com/article/reply/2903740/
                String post_reply_reg = "^http://(www|m).guokr.com/post/reply/\\d+/$";//http://www.guokr.com/post/reply/6148664/
                //上面两条，只有通知才会跳到
                String publish_post_reg = "http://www.guokr.com/group/\\d+/post/edit/";//这是发贴的链接跳转
                boolean flag = url.matches(article_reply_reg) || url.matches(post_reply_reg) || url.matches(publish_post_reg);
                if (flag) {
                    //匹配上了，要重定向，将code设置成200
                    response = response.newBuilder().code(HttpURLConnection.HTTP_OK).build();
                }
            }
            return response;
        }
    }

    private static final int IO_BUFFER_SIZE = 8 * 1024;

    public static boolean downloadFile(String urlString, String dest) {
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        boolean errorCatch = false;
        try {
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            File deskFile = new File(dest);
            if (deskFile.exists()) {
                deskFile.delete();
            } else if (!deskFile.getParentFile().exists()) {
                deskFile.getParentFile().mkdirs();
            }
            inputStream = urlConnection.getInputStream();
            fileOutputStream = new FileOutputStream(new File(dest));
            byte[] buff = new byte[IO_BUFFER_SIZE];
            int len;
            while ((len = inputStream.read(buff)) != -1) {
                fileOutputStream.write(buff, 0, len);
            }
        } catch (Exception e) {
            errorCatch = true;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                errorCatch = true;
            }
        }
        return !errorCatch;
    }
}
