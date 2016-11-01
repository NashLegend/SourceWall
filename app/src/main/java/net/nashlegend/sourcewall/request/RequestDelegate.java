package net.nashlegend.sourcewall.request;

import android.support.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
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

    private OkHttpClient httpClient;

    public RequestDelegate(@NonNull OkHttpClient okHttpClient) {
        httpClient = okHttpClient;
    }

    public Call getCall(RequestObject object) {
        Request request = getRequest(object);
        return httpClient.newCall(request);
    }

    private Request getRequest(RequestObject object) {
        Request.Builder builder = new Request.Builder()
                .method(object.method.value(), getBody(object))
                .url(getUrl(object))
                .tag(object.tag);
        if (object.headers != null) {
            builder.headers(object.headers.build());
        }
        if (object.cacheControl != null) {
            builder.cacheControl(object.cacheControl);
        }
        return builder.build();
    }

    private String getUrl(RequestObject<?> object) {
        switch (object.method) {
            case GET:
            case HEAD:
            case DELETE:
                return Urls.combine(object.url, object.params);
            default:
                if (object.requestBody != null) {
                    return Urls.combine(object.url, object.params);
                }
                return object.url;
        }
    }

    /**
     * 生成请求的RequestBody
     */
    private RequestBody getBody(RequestObject<?> object) {
        if (object.requestBody != null) {
            return object.requestBody;
        } else {
            switch (object.method) {
                case GET:
                case HEAD:
                    return null;
                case POST:
                    if (object.requestType == RequestType.UPLOAD) {
                        return getMultipartBody(object.mediaType, object.uploadFileKey, object.uploadFilePath, object.params);
                    }
                case PUT:
                case PATCH:
                    return getFormBody(object.params);
                case DELETE:
                    return null;
                default:
                    return null;
            }
        }
    }

    /**
     * 生成表单body
     *
     * @param params
     * @return
     */
    private FormBody getFormBody(List<Param> params) {
        FormBody.Builder builder = new FormBody.Builder();
        if (params != null && params.size() > 0) {
            for (Param param : params) {
                if (Utils.isEmpty(param.key) || Utils.isEmpty(param.value)) {
                    continue;
                }
                builder.add(param.key, param.value);
            }
        }
        return builder.build();
    }

    /**
     * 上传单个文件的生成的body
     */
    private MultipartBody getMultipartBody(MediaType mediaType, String fileKey, String filePath, List<Param> params) {
        ArrayList<Param> fileParam = new ArrayList<>();
        fileParam.add(new Param(fileKey, filePath));
        return getMultipartBody(mediaType, fileParam, params);
    }

    /**
     * 上传多个文件生成的body
     */
    private MultipartBody getMultipartBody(MediaType mediaType, List<Param> fileParams, List<Param> params) {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        if (fileParams != null && fileParams.size() > 0) {
            for (Param param : fileParams) {
                if (Utils.isEmpty(param.key) || Utils.isEmpty(param.value)) {
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
                if (Utils.isEmpty(param.key) || Utils.isEmpty(param.value)) {
                    continue;
                }
                builder.addFormDataPart(param.key, param.value);
            }
        }
        return builder.build();
    }
}
