package net.nashlegend.sourcewall.swrequest;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import net.nashlegend.sourcewall.App;
import net.nashlegend.sourcewall.swrequest.api.UserAPI;
import net.nashlegend.sourcewall.swrequest.cache.RequestCache;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
@SuppressWarnings("ResultOfMethodCallIgnored")
public class HttpUtil {

    private final static int HTTP_RESPONSE_DISK_CACHE_MAX_SIZE = 20 * 1024 * 1024;//Cache的最大体积,20M
    private final static int CONNECTION_TIMEOUT = 30000;//网络状况差的时候这个时间可能很长
    private final static int SO_TIMEOUT = 60000;
    private final static int UPLOAD_SO_TIMEOUT = 300000;//300秒的上传时间
    private static final int IO_BUFFER_SIZE = 8 * 1024;
    private static OkHttpClient defaultHttpClient;
    private static OkHttpClient uploadHttpClient;

    synchronized public static OkHttpClient getDefaultHttpClient() {
        if (defaultHttpClient == null) {
            defaultHttpClient = new OkHttpClient();
            final File cacheDir = RequestCache.getDiskCacheDir(App.getApp(), "OkHttp.cache");
            defaultHttpClient.setCache(new Cache(cacheDir, HTTP_RESPONSE_DISK_CACHE_MAX_SIZE));
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

    /**
     * 同步上传
     *
     * @param uploadUrl
     * @param params
     * @param fileKey
     * @param mediaType
     * @param filePath  要上传图片的路径
     * @return 返回ResponseObject，resultObject.result是上传后的图片地址
     */
    public static ResponseObject<String> upload(String uploadUrl, HashMap<String, String> params, String fileKey, MediaType mediaType, String filePath) {
        ResponseObject<String> responseObject = new ResponseObject<>();
        File file = new File(filePath);
        if (file.exists() && !file.isDirectory() && file.length() > 0) {
            try {
                MultipartBuilder builder = new MultipartBuilder().type(MultipartBuilder.FORM)
                        .addFormDataPart(fileKey, file.getName(), RequestBody.create(mediaType, file));
                if (params != null && params.size() > 0) {
                    for (HashMap.Entry<String, String> entry : params.entrySet()) {
                        builder.addFormDataPart(entry.getKey(), entry.getValue());
                    }
                }
                Request request = new Request.Builder().url(uploadUrl).post(builder.build()).build();
                Response response = getDefaultUploadHttpClient().newCall(request).execute();
                if (response.isSuccessful()) {
                    JSONObject object = JsonHandler.getUniversalJsonObject(response.body().string(), responseObject);
                    if (object != null) {
                        String url = object.optString("url");
                        responseObject.ok = true;
                        responseObject.result = url;
                    }
                }
            } catch (Exception e) {
                JsonHandler.handleRequestException(e, responseObject);
            }
        }
        return responseObject;
    }

    /**
     * 异步上传
     *
     * @param uploadUrl 上传的地址
     * @param params    上传参数
     * @param filePath  要上传图片的路径
     * @param callBack
     * @return 返回ResponseObject，resultObject.result是上传后的图片地址
     */
    synchronized public static Call uploadAsync(String uploadUrl, HashMap<String, String> params,
                                                String fileKey, MediaType mediaType, String filePath, Callback callBack) {
        File file = new File(filePath);
        if (file.exists() && !file.isDirectory() && file.length() > 0) {
            MultipartBuilder builder = new MultipartBuilder().type(MultipartBuilder.FORM)
                    .addFormDataPart(fileKey, file.getName(), RequestBody.create(mediaType, file));
            if (params != null && params.size() > 0) {
                for (HashMap.Entry<String, String> entry : params.entrySet()) {
                    builder.addFormDataPart(entry.getKey(), entry.getValue());
                }
            }
            Request request = new Request.Builder().url(uploadUrl).post(builder.build()).build();
            Call call = getDefaultUploadHttpClient().newCall(request);
            call.enqueue(callBack);
            return call;
        }
        return null;
    }
}
