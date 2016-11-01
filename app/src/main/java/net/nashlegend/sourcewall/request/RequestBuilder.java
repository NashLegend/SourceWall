package net.nashlegend.sourcewall.request;

import android.support.annotation.NonNull;

import net.nashlegend.sourcewall.request.api.UserAPI;
import net.nashlegend.sourcewall.request.parsers.Parser;
import net.nashlegend.sourcewall.request.parsers.StringParser;

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

    private RequestObject<T> request = new RequestObject<>();

    private boolean useToken = true;//是否使用token，默认使用

    public static void sampleRequest(CallBack<String> callBack) {
        new RequestBuilder<String>()
                .get()
                .url("http://bbs.hupu.com/bxj")
                .addParam("key", "value")
                .parser(new StringParser())
                .callback(callBack)
                .requestAsync();
    }

    /**
     * 设置请求方法
     * {@link Method#GET}
     * {@link Method#POST}
     * {@link Method#PUT}
     * {@link Method#DELETE}
     * {@link Method#HEAD}
     * {@link Method#PATCH}
     *
     * @param method
     * @return
     */
    public RequestBuilder<T> method(Method method) {
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
        if (params != null) {
            request.params.addAll(params);
        }
        return this;
    }

    /**
     * 设置键值对请求参数，如果之前曾经设置过，则将会清空之前的参数
     *
     * @param params
     * @return
     */
    public RequestBuilder<T> params(HashMap<String, String> params) {
        if (params == null) {
            params = new HashMap<>();
        }
        ArrayList<Param> paramList = new ArrayList<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            paramList.add(new Param(entry.getKey(), entry.getValue()));
        }
        return params(paramList);
    }

    /**
     * 设置键值对请求参数，如果之前曾经设置过，则将会清空之前的参数
     *
     * @param params
     * @return
     */
    public RequestBuilder<T> params(ParamsMap params) {
        request.params.clear();
        if (params != null) {
            request.params.addAll(params.params);
        }
        return this;
    }

    /**
     * 在当前参数的基础上再添加几个键值对请求参数
     *
     * @param params
     * @return
     */
    public RequestBuilder<T> addParams(List<Param> params) {
        if (params != null) {
            request.params.addAll(params);
        }
        return this;
    }

    /**
     * 在当前参数的基础上添加一条键值对请求参数
     *
     * @param key
     * @param value
     * @return
     */
    public RequestBuilder<T> addParam(String key, Object value) {
        request.params.add(new Param(key, value));
        return this;
    }

    /**
     * 添加或者修改一条Header
     */
    public RequestBuilder<T> header(String name, String value) {
        request.headers.set(name, value);
        return this;
    }

    /**
     * 添加一个Header
     */
    public RequestBuilder<T> addHeader(String name, String value) {
        request.headers.add(name, value);
        return this;
    }

    /**
     * 删除一个Header
     */
    public RequestBuilder<T> removeHeader(String name) {
        request.headers.removeAll(name);
        return this;
    }

    /**
     * 将Header替换成设置的headers
     */
    public RequestBuilder<T> headers(Headers headers) {
        request.headers = headers.newBuilder();
        return this;
    }

    public RequestBuilder<T> cacheControl(CacheControl cacheControl) {
        request.cacheControl = cacheControl;
        return this;
    }

    /**
     * 设置请求的body，如果设置了，那么如果设置过参数文件什么的就没用了
     */
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
     * @param useCache 是否优先使用缓存,默认超时时间为永不超时
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
     * 是否优先使用缓存，如果缓存使用失败才加载网络数据
     * 仅在使用Rx时有效，与useCacheIfFailed互斥
     *
     * @param useCache 是否优化使用缓存
     * @param timeOut  缓存超时时间,小于0表示永不超时,如果超时,可能会按次序返回两次结果
     * @return
     */
    public RequestBuilder<T> useCacheFirst(boolean useCache, long timeOut) {
        request.useCachedFirst = useCache;
        if (useCache) {
            request.useCachedIfFailed = false;
        }
        request.cacheTimeOut = timeOut;
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
    public RequestBuilder<T> callback(RequestCallBack<T> callBack) {
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
            if (!Utils.isEmpty(token)) {
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
        return new NetworkTask<>(request).requestAsync();
    }

}
