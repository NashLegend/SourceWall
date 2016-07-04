package net.nashlegend.sourcewall.request;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;

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

    public Call get(String url, List<Param> params, Object tag) throws Exception {
        return get(url + "?" + getQueryString(params), tag);
    }

    public Call post(String url, List<Param> params, Object tag) throws Exception {
        return requestSync(Method.POST, getFormBody(params), url, tag);
    }

    public Call put(String url, List<Param> params, Object tag) throws Exception {
        return requestSync(Method.PUT, getFormBody(params), url, tag);
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

    public Call delete(String url, List<Param> params, Object tag) throws Exception {
        return delete(url + "?" + getQueryString(params), tag);
    }

    /**
     * 同步上传
     *
     * @param uploadUrl
     * @param params
     * @param filePath  要上传图片的路径
     */
    public Call upload(String uploadUrl, List<Param> params,
                       String fileKey, MediaType mediaType, String filePath) throws Exception {
        File file = new File(filePath);
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(fileKey, file.getName(), RequestBody.create(mediaType, file));
        if (params != null && params.size() > 0) {
            for (Param param : params) {
                if (TextUtils.isEmpty(param.key) || TextUtils.isEmpty(param.value)) {
                    continue;
                }
                builder.addFormDataPart(param.key, param.value);
            }
        }
        Request request = new Request.Builder().url(uploadUrl).post(builder.build()).build();
        return defaultHttpClient.newCall(request);
    }

    public Call getAsync(String url, Callback defCallBack, Object tag) {
        return requestAsync(Method.GET, null, url, tag, defCallBack);
    }

    public Call getAsync(String url, List<Param> params, Callback defCallBack, Object tag) {
        return getAsync(url + "?" + getQueryString(params), defCallBack, tag);
    }

    public Call postAsync(String url, List<Param> params, Callback defCallBack, Object tag) {
        return requestAsync(Method.POST, getFormBody(params), url, tag, defCallBack);
    }

    public Call putAsync(String url, List<Param> params, Callback defCallBack, Object tag) {
        return requestAsync(Method.PUT, getFormBody(params), url, tag, defCallBack);
    }

    public Call putAsync(String url, Callback defCallBack, Object tag) {
        return putAsync(url, null, defCallBack, tag);
    }

    /**
     * Delete 也是需要RequestBody的，然而这里并没有,所以给个默认的……
     */
    public Call deleteAsync(String url, Callback defCallBack, Object tag) {
        return requestAsync(Method.DELETE, RequestBody.create(null, new byte[0]), url, tag, defCallBack);
    }

    public Call deleteAsync(String url, List<Param> params, Callback defCallBack, Object tag) {
        return deleteAsync(url + "?" + getQueryString(params), defCallBack, tag);
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
    public Call uploadAsync(String uploadUrl, List<Param> params,
                            String fileKey, MediaType mediaType, String filePath, Callback callBack) {
        File file = new File(filePath);
        if (file.exists() && !file.isDirectory() && file.length() > 0) {
            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(fileKey, file.getName(), RequestBody.create(mediaType, file));
            if (params != null && params.size() > 0) {
                for (Param param : params) {
                    if (TextUtils.isEmpty(param.key) || TextUtils.isEmpty(param.value)) {
                        continue;
                    }
                    builder.addFormDataPart(param.key, param.value);
                }
            }
            return requestAsync(Method.POST, builder.build(), uploadUrl, null, callBack);
        }
        return null;
    }

    /**
     * 生成queryString,如:a=b&b=c&c=d
     *
     * @param params
     * @return
     */
    private String getQueryString(List<Param> params) {
        StringBuilder paramString = new StringBuilder("");
        if (params != null && params.size() > 0) {
            for (Param param : params) {
                if (TextUtils.isEmpty(param.key) || TextUtils.isEmpty(param.value)) {
                    continue;
                }
                paramString.append(param.key).append("=").append(param.value).append("&");
            }
            paramString.deleteCharAt(paramString.length() - 1);
        }
        return paramString.toString();
    }

    /**
     * 生成表单数据
     *
     * @param params
     * @return
     */
    private FormBody getFormBody(List<Param> params) {
        FormBody.Builder builder = new FormBody.Builder();
        if (params != null && params.size() > 0) {
            for (Param param : params) {
                if (TextUtils.isEmpty(param.key) || TextUtils.isEmpty(param.value)) {
                    continue;
                }
                builder.add(param.key, param.value);
            }
        }
        return builder.build();
    }

    private MultipartBody getMultipartBody(MediaType mediaType, List<Param> fileParams, List<Param> params) {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        if (fileParams != null && fileParams.size() > 0) {
            for (Param param : fileParams) {
                if (TextUtils.isEmpty(param.key) || TextUtils.isEmpty(param.value)) {
                    continue;
                }
                File file = new File(param.value);
                if (file.exists() && !file.isDirectory() && file.length() > 0) {
                    builder.addFormDataPart(param.key, file.getName(), RequestBody.create(mediaType, file));
                }
            }
        }
        if (params != null && params.size() > 0) {
            for (Param param : params) {
                if (TextUtils.isEmpty(param.key) || TextUtils.isEmpty(param.value)) {
                    continue;
                }
                builder.addFormDataPart(param.key, param.value);
            }
        }
        return builder.build();
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
