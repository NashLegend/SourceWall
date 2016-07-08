package net.nashlegend.sourcewall.request;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.text.TextUtils;

import net.nashlegend.sourcewall.request.RequestObject.DetailedCallBack;
import net.nashlegend.sourcewall.request.RequestObject.Method;
import net.nashlegend.sourcewall.request.RequestObject.RequestType;
import net.nashlegend.sourcewall.request.api.UserAPI;
import net.nashlegend.sourcewall.request.parsers.DirectlyStringParser;
import net.nashlegend.sourcewall.request.parsers.Parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.CacheControl;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import rx.Observable;
import rx.Subscription;

/**
 * Created by NashLegend on 2015/9/22 0022.
 * 默认callback执行在主线程上
 */
public class RequestBuilder<T> {

    @StringDef({Method.GET, Method.POST, Method.PUT, Method.DELETE, Method.HEAD, Method.PATCH})
    public @interface MethodDef {
    }

    /**
     * 有handler就在handler上执行callback
     * 没有的话,如果：
     * 一、在主线程发起，如果ignoreHandler为false则在主线程上执行callback，否则该在哪就在哪
     * 二、后台线程发起，该在哪就在哪
     */

    private RequestObject<T> request = new RequestObject<>();

    private boolean useToken = true;//是否使用token，默认使用

    public static void fakeRequest(RequestObject.CallBack<String> callBack) {
        new RequestBuilder<String>()
                .url("http://bbs.hupu.com/bxj")
                .addParam("key", "value")
                .callback(callBack)
                .parser(new DirectlyStringParser())
                .requestAsync();
    }

    /**
     * 设置请求方法
     * {@link Method#GET}
     * {@link Method#POST}
     * {@link Method#PUT}
     * {@link Method#DELETE}
     *
     * @param method
     * @return
     */
    public RequestBuilder<T> method(@MethodDef String method) {
        request.method = method;
        return this;
    }

    /**
     * 设置请求方法get
     *
     * @return
     */
    public RequestBuilder<T> get() {
        return method(Method.GET);
    }

    /**
     * 设置请求方法post
     *
     * @return
     */
    public RequestBuilder<T> post() {
        return method(Method.POST);
    }

    /**
     * 设置请求方法put
     *
     * @return
     */
    public RequestBuilder<T> put() {
        return method(Method.PUT);
    }

    /**
     * 设置请求方法delete
     *
     * @return
     */
    public RequestBuilder<T> delete() {
        return method(Method.DELETE);
    }

    /**
     * 设置键值对请求参数，如果之前曾经设置过，则将会清空之前的参数
     *
     * @param params
     * @return
     */
    public RequestBuilder<T> params(List<Param> params) {
        request.params.clear();
        request.params.addAll(params);
        return this;
    }

    /**
     * 设置键值对请求参数，如果之前曾经设置过，则将会清空之前的参数
     *
     * @param params
     * @return
     */
    public RequestBuilder<T> params(HashMap<String, String> params) {
        ArrayList<Param> paramList = new ArrayList<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            paramList.add(new Param(entry.getKey(), entry.getValue()));
        }
        return params(paramList);
    }

    /**
     * 在当前参数的基础上再添加几个键值对请求参数
     *
     * @param params
     * @return
     */
    public RequestBuilder<T> addParams(List<Param> params) {
        request.params.addAll(params);
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
        request.params.add(new Param(key, value));
        return this;
    }

    /**
     * Sets the header named {@code name} to {@code value}. If this request already has any headers
     * with that name, they are all replaced.
     */
    public RequestBuilder<T> header(String name, String value) {
        request.headers.set(name, value);
        return this;
    }

    /**
     * Adds a header with {@code name} and {@code value}. Prefer this method for multiply-valued
     * headers like "Cookie".
     * <p>
     * <p>Note that for some headers including {@code Content-Length} and {@code Content-Encoding},
     * OkHttp may replace {@code value} with a header derived from the request body.
     */
    public RequestBuilder<T> addHeader(String name, String value) {
        request.headers.add(name, value);
        return this;
    }

    public RequestBuilder<T> removeHeader(String name) {
        request.headers.removeAll(name);
        return this;
    }

    /**
     * Removes all headers on this builder and adds {@code headers}.
     */
    public RequestBuilder<T> headers(Headers headers) {
        request.headers = headers.newBuilder();
        return this;
    }

    public RequestBuilder<T> cacheControl(CacheControl cacheControl) {
        request.cacheControl = cacheControl;
        return this;
    }

    public RequestBuilder<T> requestBody(RequestBody body) {
        request.requestBody = body;
        return this;
    }

    /**
     * 如果不设置parser或者callback则不会收到回调
     *
     * @param parser
     * @return
     */
    public RequestBuilder<T> parser(Parser<T> parser) {
        request.parser = parser;
        return this;
    }

    /**
     * 是否将请求数据进行Gzip压缩，默认为false
     *
     * @param requestWithGzip
     * @return
     */
    public RequestBuilder<T> requestWithGzip(boolean requestWithGzip) {
        request.requestWithGzip = requestWithGzip;
        return this;
    }

    /**
     * 启用伪造数据，不是null则启用，启用后仍然会走正常的请求流程，只是在请求返回后，无论对错，都返回伪造的数据
     * 启用伪造数据时，请添加stopship标志防止打包出去
     *
     * @param faked
     * @return
     */
    public RequestBuilder<T> fakeResponse(String faked) {
        request.fakeResponse = faked;
        return this;
    }

    /**
     * 同步请求不支持重试,上传不支持重试
     *
     * @param maxTimes 重试次数，不包括原本的第一次请求
     * @param interval 请求间隔
     * @return
     */
    public RequestBuilder<T> retry(int maxTimes, int interval) {
        if (maxTimes > 0) {
            request.maxRetryTimes = maxTimes;
            request.interval = interval;
        }
        return this;
    }

    /**
     * 设置请求地址,地址可以自带参数
     *
     * @param url
     * @return
     */
    public RequestBuilder<T> url(@NonNull String url) {
        request.url = url;
        return this;
    }

    /**
     * 设置是否使用token，默认为true，即使用token
     *
     * @param use
     * @return
     */
    public RequestBuilder<T> withToken(boolean use) {
        useToken = use;
        return this;
    }

    /**
     * 上传文件的Key
     *
     * @param key
     * @return
     */
    public RequestBuilder<T> uploadFileKey(@NonNull String key) {
        request.uploadFileKey = key;
        return this;
    }

    /**
     * 设置请求的mediaType
     *
     * @param mediaType
     * @return
     */
    public RequestBuilder<T> mediaType(@NonNull String mediaType) {
        request.mediaType = MediaType.parse(mediaType);
        return this;
    }

    /**
     * 设置请求的mediaType
     *
     * @param mediaType
     * @return
     */
    public RequestBuilder<T> mediaType(@NonNull MediaType mediaType) {
        request.mediaType = mediaType;
        return this;
    }

    /**
     * 如果加载失败，是否使用缓存，默认为false，不使用缓存
     *
     * @param useCache
     * @return
     */
    public RequestBuilder<T> useCacheIfFailed(boolean useCache) {
        request.useCachedIfFailed = useCache;
        if (useCache) {
            request.useCachedFirst = false;
        }
        return this;
    }

    /**
     * 是否优先使用缓存，如果缓存使用失败才加载网络数据
     * 仅在使用Rx时有效，与useCacheIfFailed互斥
     *
     * @param useCache
     * @return
     */
    public RequestBuilder<T> useCacheFirst(boolean useCache) {
        request.useCachedFirst = useCache;
        if (useCache) {
            request.useCachedIfFailed = false;
        }
        return this;
    }

    /**
     * 缓存超时时间
     *
     * @param timeOut
     * @return
     */
    public RequestBuilder<T> cacheTimeOut(long timeOut) {
        request.cacheTimeOut = timeOut;
        return this;
    }

    /**
     * 设置请求成功或者失败后的回调，如果没有parser，将不会回调
     *
     * @param callBack
     * @return
     */
    public RequestBuilder<T> callback(DetailedCallBack<T> callBack) {
        request.callBack = callBack;
        return this;
    }

    /**
     * 设置请求的tag，可通过tag
     *
     * @param tag
     * @return
     */
    public RequestBuilder<T> tag(Object tag) {
        request.tag = tag;
        return this;
    }

    /**
     * 在主线程执行回调.
     * 如果请求在主线程发起，默认就在主线程发起回调
     *
     * @return
     */
    public RequestBuilder<T> runCallbackOnMainThread() {
        request.ignoreHandler = false;
        request.handler = new Handler(Looper.getMainLooper());
        return this;
    }

    /**
     * 设置在哪个线程执行回调
     *
     * @param looper
     * @return
     */
    public RequestBuilder<T> runCallbackOn(Looper looper) {
        request.ignoreHandler = false;
        request.handler = new Handler(looper);
        return this;
    }

    /**
     * 不指定回调执行线程，在主线程发起请求也将不在主线程回调
     *
     * @return
     */
    public RequestBuilder<T> runCallbackWhatever() {
        request.ignoreHandler = true;
        request.handler = null;
        return this;
    }

    public RequestBuilder<T> upload(String filePath) {
        request.uploadFilePath = filePath;
        request.method = Method.POST;
        request.requestType = RequestType.UPLOAD;
        return this;
    }

    public RequestBuilder<T> download(String filePath) {
        request.downloadFilePath = filePath;
        request.requestType = RequestType.DOWNLOAD;
        return this;
    }

    private void addExtras() {
        if (useToken) {
            String token = UserAPI.getToken();
            if (!TextUtils.isEmpty(token)) {
                request.params.add(new Param("access_token", token));
            }
        }
    }

    public NetworkTask<T> build() {
        addExtras();
        return new NetworkTask<>(request);
    }

    /**
     * 异步请求
     */
    public NetworkTask<T> requestAsync() {
        addExtras();
        return new NetworkTask<>(request).startRequestAsync();
    }

    /**
     * 同步请求
     */
    public ResponseObject<T> requestSync() {
        addExtras();
        return new NetworkTask<>(request).startRequestSync();
    }

    /**
     * 异步请求，返回的是一个Observable，但是并没有执行，需要手动subscribe
     */
    public Observable<ResponseObject<T>> flatMap() {
        addExtras();
        return new NetworkTask<>(request).flatMap();
    }

    /**
     * 异步请求，返回的是一个Observable，但是并没有执行，需要手动subscribe
     */
    public Subscription requestRx() {
        addExtras();
        return new NetworkTask<>(request).requestRx();
    }

}
