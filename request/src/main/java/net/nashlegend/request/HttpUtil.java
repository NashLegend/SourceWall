package net.nashlegend.request;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by NashLegend on 2014/9/15 0015
 */
public class HttpUtil {

    private final static int CONNECTION_TIMEOUT = 30000;//网络状况差的时候这个时间可能很长
    private final static int SO_TIMEOUT = 60000;
    private final static int UPLOAD_SO_TIMEOUT = 300000;//300秒的上传时间
    private static final int IO_BUFFER_SIZE = 8 * 1024;
    private static OkHttpClient defaultHttpClient;
    private static OkHttpClient uploadHttpClient;

    /**
     * 取消相关tag的请求
     *
     * @param tag
     */
    public static void cancelRequestByTag(Object tag) {
        getDefaultHttpClient().cancel(tag);
    }

    public static ResponseObject<String> get(String url, Object tag) throws Exception {
        ResponseObject<String> responseObject = new ResponseObject<>();
        Request request = new Request.Builder().get().url(url).tag(tag).build();
        Response response = getDefaultHttpClient().newCall(request).execute();
        int statusCode = response.code();
        String result = response.body().string();
        responseObject.statusCode = statusCode;
        responseObject.result = result;
        return responseObject;
    }

    public static ResponseObject<String> get(String url, HashMap<String, String> params, Object
            tag) throws Exception {
        StringBuilder paramString = new StringBuilder("");
        if (params == null) {
            params = new HashMap<>();
        }
        if (params.size() > 0) {
            paramString.append("?");
            for (HashMap.Entry<String, String> entry : params.entrySet()) {
                paramString.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            paramString.deleteCharAt(paramString.length() - 1);
        }
        return get(url + paramString.toString(), tag);
    }

    public static ResponseObject<String> post(String url, HashMap<String, String> params, Object
            tag) throws Exception {
        ResponseObject<String> responseObject = new ResponseObject<>();
        if (params == null) {
            params = new HashMap<>();
        }
        FormEncodingBuilder builder = new FormEncodingBuilder();
        if (params.size() > 0) {
            for (HashMap.Entry<String, String> entry : params.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }
        RequestBody formBody = builder.build();
        Request request = new Request.Builder().post(formBody).url(url).tag(tag).build();
        Response response = getDefaultHttpClient().newCall(request).execute();
        int statusCode = response.code();
        String result = response.body().string();
        responseObject.statusCode = statusCode;
        responseObject.result = result;
        return responseObject;
    }

    public static ResponseObject<String> put(String url, HashMap<String, String> params, Object
            tag) throws Exception {
        ResponseObject<String> responseObject = new ResponseObject<>();
        if (params == null) {
            params = new HashMap<>();
        }
        FormEncodingBuilder builder = new FormEncodingBuilder();
        if (params.size() > 0) {
            for (HashMap.Entry<String, String> entry : params.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }
        RequestBody formBody = builder.build();
        Request request = new Request.Builder().post(formBody).url(url).tag(tag).build();
        Response response = getDefaultHttpClient().newCall(request).execute();
        int statusCode = response.code();
        String result = response.body().string();
        responseObject.statusCode = statusCode;
        responseObject.result = result;
        return responseObject;
    }

    public static ResponseObject<String> put(String url, Object tag) throws Exception {
        return put(url, null, tag);
    }

    /**
     * Delete 也是需要RequestBody的，然而这里并没有……
     */
    public static ResponseObject<String> delete(String url, Object tag) throws Exception {
        ResponseObject<String> responseObject = new ResponseObject<>();
        Request request = new Request.Builder().delete().url(url).tag(tag).build();
        Response response = getDefaultHttpClient().newCall(request).execute();
        int statusCode = response.code();
        String result = response.body().string();
        responseObject.statusCode = statusCode;
        responseObject.result = result;
        return responseObject;
    }

    public static ResponseObject<String> delete(String url, HashMap<String, String> params,
                                                Object tag) throws Exception {
        StringBuilder paramString = new StringBuilder("");
        if (params == null) {
            params = new HashMap<>();
        }
        if (params.size() > 0) {
            paramString.append("?");
            for (HashMap.Entry<String, String> entry : params.entrySet()) {
                paramString.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            paramString.deleteCharAt(paramString.length() - 1);
        }
        return delete(url + paramString.toString(), tag);
    }

    synchronized public static Call getAsync(String url, Callback defCallBack, Object tag) {
        Request request = new Request.Builder().get().url(url).tag(tag).build();
        Call call = getDefaultHttpClient().newCall(request);
        call.enqueue(defCallBack);
        return call;
    }

    synchronized public static Call getAsync(String url, HashMap<String, String> params, Callback defCallBack, Object tag) {
        StringBuilder paramString = new StringBuilder("");
        if (params == null) {
            params = new HashMap<>();
        }
        if (params.size() > 0) {
            paramString.append("?");
            for (HashMap.Entry<String, String> entry : params.entrySet()) {
                paramString.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            paramString.deleteCharAt(paramString.length() - 1);
        }
        return getAsync(url + paramString.toString(), defCallBack, tag);
    }

    synchronized public static Call postAsync(String url, HashMap<String, String> params, Callback defCallBack, Object tag) {
        if (params == null) {
            params = new HashMap<>();
        }
        FormEncodingBuilder builder = new FormEncodingBuilder();
        if (params.size() > 0) {
            for (HashMap.Entry<String, String> entry : params.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }
        Request request = new Request.Builder().post(builder.build()).url(url).tag(tag).build();
        Call call = getDefaultHttpClient().newCall(request);
        call.enqueue(defCallBack);
        return call;
    }

    synchronized public static Call putAsync(String url, HashMap<String, String> params, Callback defCallBack, Object tag) {
        if (params == null) {
            params = new HashMap<>();
        }
        FormEncodingBuilder builder = new FormEncodingBuilder();
        if (params.size() > 0) {
            for (HashMap.Entry<String, String> entry : params.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }
        RequestBody formBody = builder.build();
        Request request = new Request.Builder().post(formBody).url(url).tag(tag).build();
        Call call = getDefaultHttpClient().newCall(request);
        call.enqueue(defCallBack);
        return call;
    }

    synchronized public static Call putAsync(String url, Callback defCallBack, Object tag) {
        return putAsync(url, null, defCallBack, tag);
    }

    /**
     * Delete 也是需要RequestBody的，然而这里并没有……
     */
    synchronized public static Call deleteAsync(String url, Callback defCallBack, Object tag) {
        Request request = new Request.Builder().delete().url(url).tag(tag).build();
        Call call = getDefaultHttpClient().newCall(request);
        call.enqueue(defCallBack);
        return call;
    }

    synchronized public static Call deleteAsync(String url, HashMap<String, String> params, Callback defCallBack, Object tag) {
        StringBuilder paramString = new StringBuilder("");
        if (params == null) {
            params = new HashMap<>();
        }
        if (params.size() > 0) {
            paramString.append("?");
            for (HashMap.Entry<String, String> entry : params.entrySet()) {
                paramString.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            paramString.deleteCharAt(paramString.length() - 1);
        }
        return deleteAsync(url + paramString.toString(), defCallBack, tag);
    }

    synchronized public static OkHttpClient getDefaultHttpClient() {
        if (defaultHttpClient == null) {
            defaultHttpClient = new OkHttpClient();
            CookieManager cookieManager = new CookieManager();
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
            defaultHttpClient.setCookieHandler(cookieManager);
            defaultHttpClient.setConnectTimeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
            defaultHttpClient.setReadTimeout(SO_TIMEOUT, TimeUnit.MILLISECONDS);
            defaultHttpClient.setWriteTimeout(UPLOAD_SO_TIMEOUT, TimeUnit.MILLISECONDS);
        }
        return defaultHttpClient;
    }

    synchronized public static OkHttpClient getDefaultUploadHttpClient() {
        if (uploadHttpClient == null) {
            uploadHttpClient = new OkHttpClient();
            CookieManager cookieManager = new CookieManager();
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
            uploadHttpClient.setCookieHandler(cookieManager);
            uploadHttpClient.setConnectTimeout(CONNECTION_TIMEOUT * 5, TimeUnit.MILLISECONDS);
            uploadHttpClient.setReadTimeout(SO_TIMEOUT * 5, TimeUnit.MILLISECONDS);
            uploadHttpClient.setWriteTimeout(UPLOAD_SO_TIMEOUT * 5, TimeUnit.MILLISECONDS);
        }
        return uploadHttpClient;
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
     * @param filePath  要上传图片的路径
     * @return 返回ResultObject，resultObject.result是上传后的图片地址
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
                        String url = JsonHandler.getJsonString(object, "url");
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
     * @return 返回ResultObject，resultObject.result是上传后的图片地址
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
