package net.nashlegend.sourcewall.request;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import net.nashlegend.sourcewall.request.RequestObject.Method;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by NashLegend on 16/3/16.
 */
public class RequestDelegate {

    private OkHttpClient httpClient;

    public RequestDelegate(@NonNull OkHttpClient okHttpClient) {
        httpClient = okHttpClient;
    }

    public Call get(RequestObject<?> request) throws Exception {
        return get(request.url, request.params, request.tag);
    }

    public Call get(String url, Object tag) throws Exception {
        return requestSync(Method.GET, null, url, tag);
    }

    public Call get(String url, List<Param> params, Object tag) throws Exception {
        return get(combine(url, params), tag);
    }

    public Call post(RequestObject<?> request) throws Exception {
        return post(request.url, request.params, request.tag);
    }

    public Call post(String url, List<Param> params, Object tag) throws Exception {
        return requestSync(Method.POST, getFormBody(params), url, tag);
    }

    public Call put(RequestObject<?> request) throws Exception {
        return put(request.url, request.params, request.tag);
    }

    public Call put(String url, List<Param> params, Object tag) throws Exception {
        return requestSync(Method.PUT, getFormBody(params), url, tag);
    }

    public Call delete(RequestObject<?> request) throws Exception {
        return delete(request.url, request.params, request.tag);
    }

    /**
     * Delete 也是需要RequestBody的，然而这里并没有……
     */
    public Call delete(String url, Object tag) throws Exception {
        return requestSync(Method.DELETE, RequestBody.create(null, new byte[0]), url, tag);
    }

    public Call delete(String url, List<Param> params, Object tag) throws Exception {
        return delete(combine(url, params), tag);
    }

    public Call upload(RequestObject<?> request) throws Exception {
        return upload(request.url, request.uploadFileKey, request.uploadFilePath, request.params, request.mediaType);
    }

    /**
     * 同步上传
     *
     * @param url
     * @param params
     * @param fileKey
     * @param filePath  要上传图片的路径
     * @param mediaType
     * @return
     * @throws Exception
     */
    public Call upload(String url, String fileKey, String filePath,
                       List<Param> params, MediaType mediaType) throws Exception {
        MultipartBody body = getMultipartBody(mediaType, fileKey, filePath, params);
        return requestSync(Method.POST, body, url, null);
    }

    public Call getAsync(RequestObject<?> request, Callback defCallBack) {
        return getAsync(request.url, request.params, defCallBack, request.tag);
    }

    public Call getAsync(String url, Callback defCallBack, Object tag) {
        return requestAsync(Method.GET, null, url, tag, defCallBack);
    }

    public Call getAsync(String url, List<Param> params, Callback defCallBack, Object tag) {
        return getAsync(combine(url, params), defCallBack, tag);
    }

    public Call postAsync(RequestObject<?> request, Callback defCallBack) {
        return postAsync(request.url, request.params, defCallBack, request.tag);
    }

    public Call postAsync(String url, List<Param> params, Callback defCallBack, Object tag) {
        return requestAsync(Method.POST, getFormBody(params), url, tag, defCallBack);
    }

    public Call putAsync(RequestObject<?> request, Callback defCallBack) {
        return putAsync(request.url, request.params, defCallBack, request.tag);
    }

    public Call putAsync(String url, List<Param> params, Callback defCallBack, Object tag) {
        return requestAsync(Method.PUT, getFormBody(params), url, tag, defCallBack);
    }

    public Call deleteAsync(RequestObject<?> request, Callback defCallBack) {
        return deleteAsync(request.url, request.params, defCallBack, request.tag);
    }

    /**
     * Delete 也是需要RequestBody的，然而这里并没有,所以给个默认的……
     */
    public Call deleteAsync(String url, Callback defCallBack, Object tag) {
        return requestAsync(Method.DELETE, RequestBody.create(null, new byte[0]), url, tag, defCallBack);
    }

    public Call deleteAsync(String url, List<Param> params, Callback defCallBack, Object tag) {
        return deleteAsync(combine(url, params), defCallBack, tag);
    }

    /**
     * 异步上传
     *
     * @param request
     * @param callBack
     * @return 返回ResultObject，resultObject.result是上传后的图片地址
     */
    public Call uploadAsync(RequestObject<?> request, Callback callBack) {
        return uploadAsync(request.url, request.uploadFileKey, request.uploadFilePath, request.params, request.mediaType, callBack);
    }

    /**
     * 异步上传
     *
     * @param uploadUrl 上传的地址
     * @param fileKey
     * @param filePath  要上传图片的路径
     * @param params    上传参数
     * @param mediaType
     * @param callBack
     * @return 返回ResultObject，resultObject.result是上传后的图片地址
     */
    public Call uploadAsync(String uploadUrl, String fileKey, String filePath,
                            List<Param> params, MediaType mediaType, Callback callBack) {
        MultipartBody body = getMultipartBody(mediaType, fileKey, filePath, params);
        return requestAsync(Method.POST, body, uploadUrl, null, callBack);
    }

    private String combine(String url, List<Param> params) {
        HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();
        if (params != null) {
            for (Param param : params) {
                builder.addQueryParameter(param.key, param.value);
            }
        }
        return builder.build().toString();
    }

    /**
     * 生成queryString,如:a=b&c=d&e=f
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

    private MultipartBody getMultipartBody(MediaType mediaType, String fileKey, String filePath, List<Param> params) {
        ArrayList<Param> fileParam = new ArrayList<>();
        fileParam.add(new Param(fileKey, filePath));
        return getMultipartBody(mediaType, fileParam, params);
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

    public Call requestAsync(Request request, Callback callBack) {
        Call call = httpClient.newCall(request);
        call.enqueue(callBack);
        return call;
    }

    public Call requestSync(Request request, Object tag) {
        Call call = httpClient.newCall(request);
        return call;
    }

    private Call getCall(String method, RequestBody body, String url, Object tag) {
        Request request = new Request.Builder().method(method, body).url(url).tag(tag).build();
        return httpClient.newCall(request);
    }

    public Request getRequest(RequestObject<?> object) {
        Request.Builder build = new Request.Builder()
                .method(object.method, getBody(object))
                .url(getUrl(object))
                .tag(object.tag);
        if (object.headers != null) {
            build.headers(object.headers.build());
        }
        if (object.cacheControl != null) {
            build.cacheControl(object.cacheControl);
        }
        Request request = build.build();
        return request;
    }

    private String getUrl(RequestObject<?> object) {
        switch (object.method) {
            case Method.GET:
            case Method.HEAD:
            case Method.DELETE:
                return combine(object.url, object.params);
            default:
                if (object.requestBody!=null){
                    return combine(object.url, object.params);
                }
                return object.url;
        }
    }

    private RequestBody getBody(RequestObject<?> object) {
        if (object.requestBody != null) {
            return object.requestBody;
        } else {
            switch (object.method) {
                case Method.GET:
                case Method.HEAD:
                    return null;
                case Method.POST:
                    if (object.requestType == RequestObject.RequestType.UPLOAD) {
                        return getMultipartBody(object.mediaType, object.uploadFileKey, object.uploadFilePath, object.params);
                    }
                case Method.PUT:
                case Method.PATCH:
                    return getFormBody(object.params);
                case Method.DELETE:
                    return null;
                default:
                    return null;
            }
        }
    }
}
