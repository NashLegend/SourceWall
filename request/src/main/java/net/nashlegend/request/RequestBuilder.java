package net.nashlegend.request;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.squareup.okhttp.MediaType;

import net.nashlegend.request.parsers.DirectlyStringParser;
import net.nashlegend.request.parsers.Parser;

import java.util.HashMap;

/**
 * Created by NashLegend on 2015/9/22 0022.
 * 默认callback执行在主线程上
 */
public class RequestBuilder<T> {
    /**
     * 有handler就在handler上执行callback
     * 没有的话,如果：
     * 一、在主线程发起，如果ignoreHandler为false则在主线程上执行callback，否则该在哪就在哪
     * 二、后台线程发起，该在哪就在哪
     */

    private RequestObject<T> rbRequest = new RequestObject<>();

    private boolean useToken = true;//是否使用token，默认使用

    public static void fakeRequest(RequestObject.CallBack<String> callBack) {
        new RequestBuilder<String>()
                .setUrl("http://bbs.hupu.com/bxj")
                .setRequestCallBack(callBack)
                .setParser(new DirectlyStringParser())
                .requestAsync();
    }

    /**
     * 设置请求方法
     * {@link RequestObject.Method#GET}
     * {@link RequestObject.Method#POST}
     * {@link RequestObject.Method#PUT}
     * {@link RequestObject.Method#DELETE}
     *
     * @param method
     * @return
     */
    public RequestBuilder<T> setMethod(int method) {
        rbRequest.method = method;
        return this;
    }

    /**
     * 设置键值对请求参数，如果之前曾经设置过，则将会清空之前的参数
     *
     * @param params
     * @return
     */
    public RequestBuilder<T> setParams(HashMap<String, String> params) {
        rbRequest.params = params;
        return this;
    }

    /**
     * 在当前参数的基础上再添加几个键值对请求参数
     *
     * @param params
     * @return
     */
    public RequestBuilder<T> addParams(HashMap<String, String> params) {
        if (rbRequest.params == null) {
            rbRequest.params = new HashMap<>();
        }
        rbRequest.params.putAll(params);
        return this;
    }

    /**
     * 在当前参数的基础上添加一条键值对请求参数
     *
     * @param key
     * @param value
     * @return
     */
    public RequestBuilder<T> addParam(String key, String value) {
        if (rbRequest.params == null) {
            rbRequest.params = new HashMap<>();
        }
        rbRequest.params.put(key, value);
        return this;
    }

    /**
     * 如果不设置parser或者callback则不会收到回调
     *
     * @param parser
     * @return
     */
    public RequestBuilder<T> setParser(Parser<T> parser) {
        rbRequest.parser = parser;
        return this;
    }

    /**
     * 同步请求不支持重试,上传不支持重试
     *
     * @param maxTimes 重试次数，不包括原本的第一次请求
     * @param interval 请求间隔
     * @return
     */
    public RequestBuilder<T> setRetry(int maxTimes, int interval) {
        if (maxTimes > 0) {
            rbRequest.retryHandler = new RequestObject.RetryHandler(maxTimes, interval);
        }
        return this;
    }

    /**
     * 设置请求地址
     *
     * @param url
     * @return
     */
    public RequestBuilder<T> setUrl(@NonNull String url) {
        rbRequest.url = url;
        return this;
    }

    /**
     * 设置是否使用token，默认为true，即使用token
     *
     * @param use
     * @return
     */
    public RequestBuilder<T> setWithToken(boolean use) {
        useToken = use;
        return this;
    }

    /**
     * 上传文件的Key
     *
     * @param key
     * @return
     */
    public RequestBuilder<T> setUploadParamKey(@NonNull String key) {
        rbRequest.uploadParamKey = key;
        return this;
    }

    /**
     * 设置请求的mediaType
     *
     * @param mediaType
     * @return
     */
    public RequestBuilder<T> setMediaType(@NonNull String mediaType) {
        rbRequest.mediaType = MediaType.parse(mediaType);
        return this;
    }

    /**
     * 设置请求的mediaType
     *
     * @param mediaType
     * @return
     */
    public RequestBuilder<T> setMediaType(@NonNull MediaType mediaType) {
        rbRequest.mediaType = mediaType;
        return this;
    }

    /**
     * 设置请求成功或者失败后的回调，如果没有parser，将不会回调
     *
     * @param callBack
     * @return
     */
    public RequestBuilder<T> setRequestCallBack(RequestObject.CallBack<T> callBack) {
        rbRequest.callBack = callBack;
        return this;
    }

    /**
     * 设置请求的tag，可通过tag
     *
     * @param tag
     * @return
     */
    public RequestBuilder<T> setTag(Object tag) {
        rbRequest.tag = tag;
        return this;
    }

    /**
     * 在主线程执行回调.
     * 如果请求在主线程发起，默认就在主线程发起回调
     *
     * @return
     */
    public RequestBuilder<T> runCallbackOnMainThread() {
        rbRequest.ignoreHandler = false;
        rbRequest.handler = new Handler(Looper.getMainLooper());
        return this;
    }

    /**
     * 设置在哪个线程执行回调
     *
     * @param looper
     * @return
     */
    public RequestBuilder<T> runCallbackOn(Looper looper) {
        rbRequest.ignoreHandler = false;
        rbRequest.handler = new Handler(looper);
        return this;
    }

    /**
     * 不指定回调执行线程，在主线程发起请求也将不在主线程回调
     *
     * @return
     */
    public RequestBuilder<T> runCallbackWhatever() {
        rbRequest.ignoreHandler = true;
        rbRequest.handler = null;
        return this;
    }

    private void addToken() {
        if (useToken) {
            if (rbRequest.params == null) {
                rbRequest.params = new HashMap<>();
            }
            // TODO: 16/1/28
            String token = "";
            if (!TextUtils.isEmpty(token)) {
                rbRequest.params.put("token", token);
            }
        }
    }

    /**
     * 同步请求
     *
     * @return
     */
    public ResponseObject<T> requestSync() {
        addToken();
        rbRequest.isAsync = false;
        rbRequest.requestType = RequestObject.RequestType.PLAIN;
        return rbRequest.requestSync();
    }

    /**
     * 异步请求
     */
    public void requestAsync() {
        addToken();
        rbRequest.isAsync = true;
        rbRequest.requestType = RequestObject.RequestType.PLAIN;
        rbRequest.requestAsync();
    }

    public ResponseObject<T> uploadSync(String filePath) {
        addToken();
        rbRequest.isAsync = false;
        rbRequest.filePath = filePath;
        rbRequest.requestType = RequestObject.RequestType.UPLOAD;
        return rbRequest.uploadSync();
    }

    public void uploadAsync(String filePath) {
        addToken();
        rbRequest.isAsync = true;
        rbRequest.filePath = filePath;
        rbRequest.requestType = RequestObject.RequestType.UPLOAD;
        rbRequest.uploadAsync();
    }

}
