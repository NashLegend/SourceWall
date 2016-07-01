package net.nashlegend.sourcewall.request;

import android.support.annotation.NonNull;

import java.io.File;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by NashLegend on 16/3/16.
 */
public class RequestDelegate {

    private OkHttpClient defaultHttpClient;

    public RequestDelegate(@NonNull OkHttpClient okHttpClient) {
        defaultHttpClient = okHttpClient;
    }

    public Call get(String url, Object tag) throws Exception {
        return requestSync(Method.GET, null, url, tag);
    }

    public Call get(String url, HashMap<String, String> params, Object tag) throws Exception {
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

    public Call post(String url, HashMap<String, String> params, Object tag) throws Exception {
        if (params == null) {
            params = new HashMap<>();
        }
        FormBody.Builder builder = new FormBody.Builder();
        if (params.size() > 0) {
            for (HashMap.Entry<String, String> entry : params.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }
        return requestSync(Method.POST, builder.build(), url, tag);
    }

    public Call put(String url, HashMap<String, String> params, Object tag) throws Exception {
        if (params == null) {
            params = new HashMap<>();
        }
        FormBody.Builder builder = new FormBody.Builder();
        if (params.size() > 0) {
            for (HashMap.Entry<String, String> entry : params.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }
        return requestSync(Method.PUT, builder.build(), url, tag);
    }

    public Call put(String url, Object tag) throws Exception {
        return put(url, null, tag);
    }

    /**
     * Delete 也是需要RequestBody的，然而这里并没有……
     */
    public Call delete(String url, Object tag) throws Exception {
        return requestSync(Method.DELETE, RequestBody.create(null, new byte[0]), url, tag);
    }

    public Call delete(String url, HashMap<String, String> params, Object tag) throws Exception {
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

    /**
     * 同步上传
     *
     * @param uploadUrl
     * @param params
     * @param filePath  要上传图片的路径
     */
    public Call upload(String uploadUrl, HashMap<String, String> params,
                       String fileKey, MediaType mediaType, String filePath) throws Exception {
        File file = new File(filePath);
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(fileKey, file.getName(), RequestBody.create(mediaType, file));
        if (params != null && params.size() > 0) {
            for (HashMap.Entry<String, String> entry : params.entrySet()) {
                builder.addFormDataPart(entry.getKey(), entry.getValue());
            }
        }
        Request request = new Request.Builder().url(uploadUrl).post(builder.build()).build();
        return defaultHttpClient.newCall(request);
    }

    public Call getAsync(String url, Callback defCallBack, Object tag) {
        return requestAsync(Method.GET, null, url, tag, defCallBack);
    }

    public Call getAsync(String url, HashMap<String, String> params, Callback defCallBack, Object tag) {
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

    public Call postAsync(String url, HashMap<String, String> params, Callback defCallBack, Object tag) {
        if (params == null) {
            params = new HashMap<>();
        }
        FormBody.Builder builder = new FormBody.Builder();
        if (params.size() > 0) {
            for (HashMap.Entry<String, String> entry : params.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }
        return requestAsync(Method.POST, builder.build(), url, tag, defCallBack);
    }

    public Call putAsync(String url, HashMap<String, String> params, Callback defCallBack, Object tag) {
        if (params == null) {
            params = new HashMap<>();
        }
        FormBody.Builder builder = new FormBody.Builder();
        if (params.size() > 0) {
            for (HashMap.Entry<String, String> entry : params.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }
        return requestAsync(Method.PUT, builder.build(), url, tag, defCallBack);
    }

    public Call putAsync(String url, Callback defCallBack, Object tag) {
        return putAsync(url, null, defCallBack, tag);
    }

    /**
     * Delete 也是需要RequestBody的，然而这里并没有……
     */
    public Call deleteAsync(String url, Callback defCallBack, Object tag) {
        Request request = new Request.Builder().delete().url(url).tag(tag).build();
        return requestAsync(Method.DELETE, RequestBody.create(null, new byte[0]), url, tag, defCallBack);
    }

    public Call deleteAsync(String url, HashMap<String, String> params, Callback defCallBack, Object tag) {
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

    /**
     * 异步上传
     *
     * @param uploadUrl 上传的地址
     * @param params    上传参数
     * @param filePath  要上传图片的路径
     * @param callBack
     * @return 返回ResultObject，resultObject.result是上传后的图片地址
     */
    public Call uploadAsync(String uploadUrl, HashMap<String, String> params,
                                         String fileKey, MediaType mediaType, String filePath, Callback callBack) {
        File file = new File(filePath);
        if (file.exists() && !file.isDirectory() && file.length() > 0) {
            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(fileKey, file.getName(), RequestBody.create(mediaType, file));
            if (params != null && params.size() > 0) {
                for (HashMap.Entry<String, String> entry : params.entrySet()) {
                    builder.addFormDataPart(entry.getKey(), entry.getValue());
                }
            }
            return requestAsync(Method.POST, builder.build(), uploadUrl, null, callBack);
        }
        return null;
    }

    public Call requestAsync(String method, RequestBody body, String url, Object tag, Callback callBack) {
        Call call = getCall(method, body, url, tag);
        call.enqueue(callBack);
        return call;
    }

    public Call requestSync(String method, RequestBody body, String url, Object tag) {
        return getCall(method, body, url, tag);
    }

    private Call getCall(String method, RequestBody body, String url, Object tag) {
        Request request = new Request.Builder().method(method, body).url(url).tag(tag).build();
        return defaultHttpClient.newCall(request);
    }

    static class Method {
        public static final String GET = "GET";
        public static final String POST = "POST";
        public static final String DELETE = "DELETE";
        public static final String PUT = "PUT";
        public static final String HEAD = "HEAD";
        public static final String PATCH = "PATCH";
    }
}
