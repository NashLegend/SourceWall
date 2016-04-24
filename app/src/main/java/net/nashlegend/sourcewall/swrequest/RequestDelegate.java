package net.nashlegend.sourcewall.swrequest;

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
        Request request = new Request.Builder().get().url(url).tag(tag).build();
        return defaultHttpClient.newCall(request);
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
        RequestBody formBody = builder.build();
        Request request = new Request.Builder().post(formBody).url(url).tag(tag).build();
        return defaultHttpClient.newCall(request);
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
        RequestBody formBody = builder.build();
        Request request = new Request.Builder().post(formBody).url(url).tag(tag).build();
        return defaultHttpClient.newCall(request);
    }

    public Call put(String url, Object tag) throws Exception {
        return put(url, null, tag);
    }

    /**
     * Delete 也是需要RequestBody的，然而这里并没有……
     */
    public Call delete(String url, Object tag) throws Exception {
        Request request = new Request.Builder().delete().url(url).tag(tag).build();
        return defaultHttpClient.newCall(request);
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

    synchronized public Call getAsync(String url, Callback defCallBack, Object tag) {
        Request request = new Request.Builder().get().url(url).tag(tag).build();
        Call call = defaultHttpClient.newCall(request);
        call.enqueue(defCallBack);
        return call;
    }

    synchronized public Call getAsync(String url, HashMap<String, String> params, Callback defCallBack, Object tag) {
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

    synchronized public Call postAsync(String url, HashMap<String, String> params, Callback defCallBack, Object tag) {
        if (params == null) {
            params = new HashMap<>();
        }
        FormBody.Builder builder = new FormBody.Builder();
        if (params.size() > 0) {
            for (HashMap.Entry<String, String> entry : params.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }
        Request request = new Request.Builder().post(builder.build()).url(url).tag(tag).build();
        Call call = defaultHttpClient.newCall(request);
        call.enqueue(defCallBack);
        return call;
    }

    synchronized public Call putAsync(String url, HashMap<String, String> params, Callback defCallBack, Object tag) {
        if (params == null) {
            params = new HashMap<>();
        }
        FormBody.Builder builder = new FormBody.Builder();
        if (params.size() > 0) {
            for (HashMap.Entry<String, String> entry : params.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }
        RequestBody formBody = builder.build();
        Request request = new Request.Builder().post(formBody).url(url).tag(tag).build();
        Call call = defaultHttpClient.newCall(request);
        call.enqueue(defCallBack);
        return call;
    }

    synchronized public Call putAsync(String url, Callback defCallBack, Object tag) {
        return putAsync(url, null, defCallBack, tag);
    }

    /**
     * Delete 也是需要RequestBody的，然而这里并没有……
     */
    synchronized public Call deleteAsync(String url, Callback defCallBack, Object tag) {
        Request request = new Request.Builder().delete().url(url).tag(tag).build();
        Call call = defaultHttpClient.newCall(request);
        call.enqueue(defCallBack);
        return call;
    }

    synchronized public Call deleteAsync(String url, HashMap<String, String> params, Callback defCallBack, Object tag) {
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
    synchronized public Call uploadAsync(String uploadUrl, HashMap<String, String> params,
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
            Request request = new Request.Builder().url(uploadUrl).post(builder.build()).build();
            Call call = defaultHttpClient.newCall(request);
            call.enqueue(callBack);
            return call;
        }
        return null;
    }
}
