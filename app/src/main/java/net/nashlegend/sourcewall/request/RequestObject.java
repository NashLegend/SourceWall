package net.nashlegend.sourcewall.request;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import net.nashlegend.sourcewall.request.cache.CacheHeaderUtil;
import net.nashlegend.sourcewall.request.cache.RequestCache;
import net.nashlegend.sourcewall.request.parsers.Parser;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ForwardingSink;
import okio.ForwardingSource;
import okio.GzipSink;
import okio.Okio;
import okio.Sink;
import okio.Source;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.Exceptions;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by NashLegend on 2015/9/23 0023.
 * 网络请求的对象
 */
public class RequestObject<T> {
    /**
     * 默认Tag
     */
    public static final String DefaultTag = "RequestObject";
    public boolean requestWithGzip = false;//是否使用Gzip压缩请求数据
    public String fakeResponse = null;
    protected int maxRetryTimes = 0;//最大重试次数
    protected int interval = 0;//重试间隔
    protected int requestType = RequestType.PLAIN;
    protected int method = Method.POST;
    protected HashMap<String, String> params = new HashMap<>();
    protected String url = "";
    protected DetailedCallBack<T> callBack = null;
    protected Parser<T> parser;
    protected Object tag = DefaultTag;
    protected String uploadParamKey = "file";
    protected MediaType mediaType = null;
    protected String uploadFilePath = "";
    protected String downloadFilePath = "";
    /**
     * 是否优先使用缓存，如果useCachedFirst为true，那么useCachedIfFailed就为false了
     * 仅仅在使用Rx时有效，与useCacheIfFailed互斥
     */
    protected boolean useCachedFirst = false;
    /**
     * 请求失败时是否使用缓存，如果为true，那么将使用缓存，请求成功的话也会将成功的数据缓存下来,
     * 与useCachedFirst互斥
     */
    protected boolean useCachedIfFailed = false;
    /**
     * 缓存时间，如果上次保存的缓存时间与本次请求的时间差相差超过了cacheTimeOut，则重新请求一次
     */
    protected long cacheTimeOut = -1;
    /*果壳貌似本身并没有Cache-Control，或者Cache-Control的max-age=0 所以这里的缓存是本地缓存*/

    /**
     * 下面部分均标为过时，请求逐步改为上面的Rx请求方式
     */

    @Deprecated
    protected boolean ignoreHandler = false;
    @Deprecated
    protected Handler handler;
    private RequestDelegate requestDelegate;
    private OkHttpClient okHttpClient;
    private ResponseObject<T> responseObject = new ResponseObject<>();
    private int crtTime = 0;//当前重试次数
    private boolean softCancelled = false;//取消掉一个请求，但是并不中断请求，只是不再执行CallBack,请求完成后无任何动作
    private Call call = null;
    private String cacheKey = null;

    public void setCallBack(final CallBack<T> call) {
        if (call == null) {
            return;
        }
        //noinspection StatementWithEmptyBody
        if (call instanceof DetailedCallBack) {
            //do nothing
        } else {
            this.callBack = new DetailedCallBack<T>() {
                @Override
                public void onFailure(@Nullable Throwable e, @NonNull ResponseObject<T> result) {
                    call.onFailure(e, result);
                }

                @Override
                public void onSuccess(@NonNull T result, @NonNull ResponseObject<T> detailed) {
                    call.onSuccess(result, detailed);
                }

                @Override
                public void onRequestProgress(long current, long total) {
                    //do nothing
                    System.out.println(current + "_" + total);
                }

                @Override
                public void onResponseProgress(long current, long total, boolean done) {
                    //do nothing
                    System.out.println("downloadAsync " + current + "_" + total);
                }
            };
        }
    }

    synchronized public RequestDelegate getRequestDelegate() {
        if (requestDelegate == null) {
            requestDelegate = new RequestDelegate(getHttpClient());
        }
        return new RequestDelegate(getHttpClient());
    }

    synchronized public OkHttpClient getHttpClient() {
        if (okHttpClient == null) {
            String key;
            switch (requestType) {
                case RequestType.UPLOAD:
                    key = "uploadClient";
                    break;
                case RequestType.DOWNLOAD:
                    key = "downloadClient";
                    break;
                default:
                    key = "defaultClient";
                    break;
            }
            if (requestWithGzip) {
                key += "requestWithGzip";
            }
            OkHttpClient tmpClient = HttpUtil.getOkHttpClient(key);
            if (tmpClient == null) {
                switch (requestType) {
                    case RequestType.UPLOAD:
                        tmpClient = HttpUtil.getDefaultUploadHttpClient().newBuilder()
                                .addNetworkInterceptor(new UploadProgressInterceptor()).build();
                        break;
                    case RequestType.DOWNLOAD:
                        tmpClient = HttpUtil.getDefaultUploadHttpClient().newBuilder()
                                .addNetworkInterceptor(new DownloadProgressInterceptor()).build();
                        break;
                    default:
                        tmpClient = HttpUtil.getDefaultHttpClient();
                        break;
                }
                if (requestWithGzip) {
                    tmpClient = tmpClient.newBuilder().addNetworkInterceptor(new GzipRequestInterceptor()).build();
                }
                HttpUtil.putOkHttpClient(key, tmpClient);
            }
            okHttpClient = tmpClient;
        }
        return okHttpClient;
    }

    /**
     * 生成此次请求的缓存key，
     * key的格式为：Method/{URL}/Params —— Params为a=b&c=d这样，按key排序
     *
     * @return
     */
    private String getCachedKey() {
        if (cacheKey == null) {
            synchronized (RequestObject.class) {
                if (cacheKey == null) {
                    StringBuilder keyBuilder = new StringBuilder("");
                    keyBuilder.append(method).append("/{").append(url).append("}/");
                    if (params == null) {
                        params = new HashMap<>();
                    }
                    if (params.size() > 0) {
                        ArrayList<Map.Entry<String, String>> entryArrayList = new ArrayList<>();
                        for (HashMap.Entry<String, String> entry : params.entrySet()) {
                            if (TextUtils.isEmpty(entry.getKey())) {
                                continue;
                            }
                            entryArrayList.add(entry);
                        }
                        Collections.sort(entryArrayList, new Comparator<Map.Entry<String, String>>() {
                            @Override
                            public int compare(Map.Entry<String, String> lhs, Map.Entry<String, String> rhs) {
                                return lhs.getKey().compareTo(rhs.getKey());
                            }
                        });
                        if (entryArrayList.size() > 0) {
                            keyBuilder.append("?");
                            for (int i = 0; i < entryArrayList.size(); i++) {
                                HashMap.Entry<String, String> entry = entryArrayList.get(i);
                                keyBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                            }
                            keyBuilder.deleteCharAt(keyBuilder.length() - 1);
                        }
                    }
                    cacheKey = keyBuilder.toString();
                }
            }
        }
        return cacheKey;
    }

    private boolean isOutOfDate() {
        return System.currentTimeMillis() - CacheHeaderUtil.readTime(getCachedKey()) > cacheTimeOut;
    }

    /**
     * 在缓存中读取数据
     *
     * @return
     */
    private String readFromCache() {
        return RequestCache.getInstance().getStringFromCache(getCachedKey());
    }

    /**
     * 将数据存入缓存
     *
     * @return
     */
    private void saveToCache(String data) {
        if (shouldCache()) {
            RequestCache.getInstance().addStringToCacheForceUpdate(getCachedKey(), data);
            CacheHeaderUtil.saveTime(getCachedKey(), System.currentTimeMillis());
        }
    }

    /**
     * 将数据存入缓存
     *
     * @return
     */
    private void removeCache() {
        RequestCache.getInstance().removeCache(getCachedKey());
        CacheHeaderUtil.remove(getCachedKey());
    }

    private boolean shouldCache() {
        return useCachedIfFailed || useCachedFirst;
    }

    @SuppressWarnings("unchecked")
    public void copyPartFrom(@NonNull RequestObject object) {
        try {
            if (object.params != null) {
                params = (HashMap<String, String>) object.params.clone();
            }
        } catch (Exception ignored) {

        }
        method = object.method;
        url = object.url;
        tag = object.tag;
        uploadParamKey = object.uploadParamKey;
        mediaType = object.mediaType;
    }

    /**
     * 重新请求
     */
    public Call startRequestSync() throws Exception {
        switch (requestType) {
            case RequestType.PLAIN:
                return requestSync();
            case RequestType.UPLOAD:
                return uploadSync();
            case RequestType.DOWNLOAD:
                return downloadSync();
            default:
                return requestSync();
        }
    }

    /**
     * 同步上传
     */
    private Call uploadSync() throws Exception {
        return getRequestDelegate().upload(url, params, uploadParamKey, mediaType, uploadFilePath);
    }

    /**
     * 同步下载
     */
    private Call downloadSync() throws Exception {
        return requestSync();
    }

    /**
     * 同步请求
     *
     * @return
     */
    private Call requestSync() throws Exception {
        Call call;
        switch (method) {
            case Method.GET:
                call = getRequestDelegate().get(url, params, tag);
                break;
            case Method.PUT:
                call = getRequestDelegate().put(url, params, tag);
                break;
            case Method.DELETE:
                call = getRequestDelegate().delete(url, params, tag);
                break;
            default:
                call = getRequestDelegate().post(url, params, tag);
                break;
        }
        return call;
    }

    /**
     * 请求缓存数据，如果已经有了缓存，然而还是解析失败了，只可能是版本升级中parser发生了变化
     *
     * @return
     */
    @NonNull
    private Observable<ResponseObject<T>> readCacheObservable() {
        return Observable
                .create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        if (subscriber.isUnsubscribed()) {
                            return;
                        }
                        try {
                            String cachedResult = readFromCache();
                            responseObject.isCached = true;
                            responseObject.body = cachedResult;
                            if (cachedResult != null) {
                                subscriber.onNext(cachedResult);
                                if (isOutOfDate()) {
                                    CacheHeaderUtil.removeOld();
                                    //如果缓存的数据超过了缓存期，则仍然要读取一次网络
                                    subscriber.onNext(null);
                                }
                            } else {
                                subscriber.onNext(null);
                            }
                        } catch (Exception e) {
                            subscriber.onNext(null);
                        }
                        subscriber.onCompleted();
                    }
                })
                .map(new Func1<String, ResponseObject<T>>() {
                    @Override
                    public ResponseObject<T> call(String s) {
                        if (s == null) {
                            return null;
                        } else {
                            try {
                                responseObject.result = parser.parse(s, responseObject);
                                return responseObject;
                            } catch (Exception e) {
                                return null;
                            }
                        }
                    }
                });
    }

    /**
     * 请求网络返回数据
     *
     * @return
     */
    @NonNull
    private Observable<ResponseObject<T>> readNetworkObservable() {
        return Observable
                .create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        try {
                            responseObject.isCached = false;
                            call = startRequestSync();
                            Response response = call.execute();
                            if (requestType == RequestType.DOWNLOAD) {
                                // 下载请求在此处就确定了请求的结果responseObject.ok，可以无parser，
                                // 当然在parse处可以推翻此结论
                                Throwable throwable = null;
                                File downloadedFile = new File(downloadFilePath);
                                BufferedSink sink = null;
                                try {
                                    sink = Okio.buffer(Okio.sink(downloadedFile));
                                    sink.writeAll(response.body().source());
                                    responseObject.ok = true;
                                } catch (Exception e) {
                                    responseObject.ok = false;
                                    throwable = e;
                                } finally {
                                    if (sink != null) {
                                        sink.close();
                                    }
                                }
                                if (responseObject.ok) {
                                    subscriber.onNext(downloadFilePath);
                                } else {
                                    Exceptions.throwOrReport(throwable, subscriber, RequestObject.this);
                                }
                            } else {
                                String result = response.body().string();
                                responseObject.statusCode = response.code();
                                responseObject.body = result;
                                subscriber.onNext(result);
                                subscriber.onCompleted();
                            }
                        } catch (Exception e) {
                            if (call != null && call.isCanceled()) {
                                responseObject.isCancelled = true;
                            }
                            Exceptions.throwOrReport(e, subscriber, RequestObject.this);
                        }
                    }
                })
                .retryWhen(new RxRetryHandler())
                .onErrorResumeNext(new Func1<Throwable, Observable<? extends String>>() {
                    @Override
                    public Observable<? extends String> call(Throwable throwable) {
                        if ((call == null || !call.isCanceled()) && useCachedIfFailed) {
                            //如果请求失败并且可以使用缓存，那么使用缓存
                            //只要不是取消掉的才会读取缓存，如果是取消掉的，读啥缓存啊，请求都没有了
                            String cachedResult = readFromCache();
                            if (cachedResult != null) {
                                responseObject.isCached = true;
                                responseObject.body = cachedResult;
                                return Observable.just(cachedResult);
                            }
                        }
                        JsonHandler.handleRequestException(throwable, responseObject);
                        return Observable.just("Error Occurred!");
                    }
                })
                .map(new Func1<String, ResponseObject<T>>() {
                    @Override
                    public ResponseObject<T> call(String string) {
                        if (responseObject.throwable == null && parser != null) {
                            try {
                                responseObject.result = parser.parse(string, responseObject);
                                if (responseObject.ok && !responseObject.isCached) {
                                    saveToCache(string);
                                }
                            } catch (Exception e) {
                                JsonHandler.handleRequestException(e, responseObject);
                            }
                        }
                        if (!responseObject.ok && responseObject.isCached) {
                            removeCache();
                        }
                        return responseObject;
                    }
                });
    }

    /**
     * 请求缓存数据，如果已经有了缓存，然而还是解析失败了，只可能是版本升级中parser发生了变化
     *
     * @return
     */
    @NonNull
    public Observable<ResponseObject<T>> requestObservable() {
        Observable<ResponseObject<T>> observable;
        if (useCachedFirst) {
            observable = readCacheObservable()
                    .flatMap(new Func1<ResponseObject<T>, Observable<ResponseObject<T>>>() {
                        @Override
                        public Observable<ResponseObject<T>> call(ResponseObject<T> r) {
                            if (r == null) {
                                return readNetworkObservable();
                            } else {
                                return Observable.just(r);
                            }
                        }
                    });
        } else {
            observable = readNetworkObservable();
        }
        return observable.subscribeOn(Schedulers.io());
    }

    /**
     * 通过RxJava的方式执行请求，与requestAsync一样，CallBack执行在主线程上
     */
    public void requestRx() {
        requestObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseObject<T>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        //requestObservable很难走到onError，因为都已经封好了，否则第二个参数不太好传给别人
                        if (!softCancelled && callBack != null) {
                            callBack.onFailure(e, responseObject);
                        }
                    }

                    @Override
                    public void onNext(ResponseObject<T> tResponseObject) {
                        if (!softCancelled && callBack != null) {
                            if (tResponseObject.ok) {
                                callBack.onSuccess(tResponseObject.result, tResponseObject);
                            } else {
                                callBack.onFailure(tResponseObject.throwable, tResponseObject);
                            }
                        }
                    }
                });
    }

    public void softCancel() {
        this.softCancelled = true;
    }

    public boolean isSoftCancelled() {
        return softCancelled;
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    public String dump() {
        StringBuilder err = new StringBuilder();
        err.append("    ").append("params").append(":").append(params).append("\n");
        err.append("    ").append("method").append(":").append(method).append("\n");
        err.append("    ").append("url").append(":").append(url).append("\n");
        err.append("    ").append("tag").append(":").append(tag).append("\n");
        if (requestType == RequestType.UPLOAD) {
            err.append("    ").append("uploadParamKey").append(":").append(uploadParamKey).append("\n");
            err.append("    ").append("mediaType").append(":").append(mediaType).append("\n");
        }
        return err.toString();
    }

    public boolean shouldHandNotifier(Throwable exception, ResponseObject responseObject) {
        return responseObject.code != ResponseCode.CODE_TOKEN_INVALID
                && call != null
                && !call.isCanceled()
                && crtTime < maxRetryTimes
                && !(exception instanceof InterruptedIOException)
                && (responseObject.statusCode < 300 || responseObject.statusCode >= 500);
    }

    public void notifyAction() {
        crtTime++;
    }

    /**
     * 重新请求
     */
    @Deprecated
    public void startRequest() {
        switch (requestType) {
            case RequestType.PLAIN:
                requestAsync();
                break;
            case RequestType.UPLOAD:
                uploadAsync();
                break;
            case RequestType.DOWNLOAD:
                downloadAsync();
                break;
            default:
                requestAsync();
                break;
        }
    }

    /**
     * 异步上传
     */
    @Deprecated
    private void uploadAsync() {
        prepareHandler();
        getRequestDelegate().uploadAsync(url, params, uploadParamKey, mediaType, uploadFilePath, getInnerCallback());
    }

    /**
     * 异步下载
     */
    @Deprecated
    private void downloadAsync() {
        prepareHandler();
        requestAsync(getInnerDownloadCallback());
    }

    /**
     * 异步请求，如果在enqueue执行之前就执行了cancel，那么将不会有callback执行，用户将不知道已经取消了请求。
     * 我们在请求中已经添加了synchronized，所以不考虑这种情况了
     */
    @Deprecated
    private void requestAsync() {
        requestAsync(getInnerCallback());
    }

    /**
     * 异步请求，如果在enqueue执行之前就执行了cancel，那么将不会有callback执行，用户将不知道已经取消了请求。
     * 我们在请求中已经添加了synchronized，所以不考虑这种情况了
     */
    @Deprecated
    public void requestAsync(Callback callback) {
        prepareHandler();
        switch (method) {
            case Method.GET:
                call = getRequestDelegate().getAsync(url, params, callback, tag);
                break;
            case Method.PUT:
                call = getRequestDelegate().putAsync(url, params, callback, tag);
                break;
            case Method.DELETE:
                call = getRequestDelegate().deleteAsync(url, params, callback, tag);
                break;
            default:
                call = getRequestDelegate().postAsync(url, params, callback, tag);
                break;
        }
    }

    @Deprecated
    private void prepareHandler() {
        if (Thread.currentThread().getId() == 1) {
            //是果是在主线程请求,且handler为null，则将其置为在主线程执行callback
            if (!ignoreHandler && handler == null) {
                handler = new Handler(Looper.getMainLooper());
            }
        }
    }

    @Deprecated
    private Callback getInnerCallback() {
        return new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                final ResponseObject<T> responseObject = new ResponseObject<>();
                responseObject.requestObject = RequestObject.this;
                JsonHandler.handleRequestException(e, responseObject);
                if ((call == null || !call.isCanceled()) && useCachedIfFailed) {
                    //只有在此处才可，其他地方有可能只是别的错误而非请求错误
                    try {
                        String cachedResult = readFromCache();
                        if (cachedResult != null) {
                            responseObject.isCached = true;
                            responseObject.body = cachedResult;
                            responseObject.result = parser.parse(cachedResult, responseObject);
                        } else {
                            onRequestFailure(e, responseObject);
                        }
                    } catch (Exception ignored) {
                        //缓存过的数据是不会出错的，除非是改版后parser发生了改变，一般这里走不到
                        onRequestFailure(e, responseObject);
                    }
                } else {
                    onRequestFailure(e, responseObject);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final ResponseObject<T> responseObject = new ResponseObject<>();
                responseObject.requestObject = RequestObject.this;
                if (callBack != null && parser != null) {
                    Throwable throwable = null;
                    try {
                        int statusCode = response.code();
                        String result = response.body().string();
                        responseObject.statusCode = statusCode;
                        responseObject.body = result;
                        responseObject.result = parser.parse(result, responseObject);
                        if (responseObject.ok && response.isSuccessful()) {
                            saveToCache(result);
                        }
                    } catch (final Exception e) {
                        JsonHandler.handleRequestException(e, responseObject);
                        throwable = e;
                    }
                    if (responseObject.ok) {
                        callSuccess(responseObject);
                    } else {
                        onRequestFailure(throwable, responseObject);
                    }
                }
            }
        };
    }

    @Deprecated
    private Callback getInnerDownloadCallback() {
        return new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                final ResponseObject<T> responseObject = new ResponseObject<>();
                responseObject.requestObject = RequestObject.this;
                if (e != null && (e instanceof ConnectException || e instanceof UnknownHostException)) {
                    responseObject.message = "无法连接到网络";
                }
                responseObject.ok = false;
                responseObject.throwable = e;
                onRequestFailure(e, responseObject);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final ResponseObject<T> responseObject = new ResponseObject<>();
                responseObject.requestObject = RequestObject.this;
                if (callBack != null) {
                    Throwable throwable = null;
                    File downloadedFile = new File(downloadFilePath);
                    BufferedSink sink = null;
                    try {
                        sink = Okio.buffer(Okio.sink(downloadedFile));
                        sink.writeAll(response.body().source());
                        responseObject.ok = true;
                        if (parser != null) {
                            parser.parse(downloadFilePath, responseObject);
                        }
                    } catch (Exception e) {
                        responseObject.ok = false;
                        throwable = e;
                    } finally {
                        if (sink != null) {
                            sink.close();
                        }
                    }
                    if (responseObject.ok) {
                        callSuccess(responseObject);
                    } else {
                        onRequestFailure(throwable, responseObject);
                    }
                }
            }
        };
    }

    /**
     * 异步请求出错
     *
     * @param e
     * @param result
     */
    @WorkerThread
    @Deprecated
    private void onRequestFailure(final Throwable e, final ResponseObject<T> result) {
        if (call != null && requestType == RequestType.PLAIN && shouldHandNotifier(e, result)) {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            } finally {
                startRequest();
            }
            notifyAction();
        } else {
            if (call != null && call.isCanceled()) {
                result.error = ResponseError.CANCELLED;
                result.isCancelled = true;
            }
            callFailure(e, result);
        }
    }

    @Deprecated
    private void callSuccess(@NonNull final ResponseObject<T> responseObject) {
        if (!softCancelled && callBack != null) {
            if (handler != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callBack.onSuccess(responseObject.result, responseObject);
                    }
                });
            } else {
                callBack.onSuccess(responseObject.result, responseObject);
            }
        }
    }

    @Deprecated
    private void callFailure(@Nullable final Throwable e, @NonNull final ResponseObject<T> responseObject) {
        if (!softCancelled && callBack != null) {
            if (handler != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callBack.onFailure(e, responseObject);
                    }
                });
            } else {
                callBack.onFailure(e, responseObject);
            }
        }
    }

    /**
     * http 请求方法
     */
    public interface Method {
        int GET = 0;
        int POST = 1;
        int PUT = 2;
        int DELETE = 3;
    }

    /**
     * http 请求方法
     */
    public interface RequestType {
        int PLAIN = 0;
        int UPLOAD = 1;
        /**
         * 理论上说下载返回结果没法用Parser，如果需要Parser，那么parse的是downloadFilePath
         * 所以建议使用DirectlyStringParser,因为在onNext中返回了downloadFilePath
         */
        int DOWNLOAD = 2;
    }


    /**
     * http 请求回调
     *
     * @param <T>
     */
    public interface CallBack<T> {
        /**
         * ok必须为false
         *
         * @param e
         * @param result
         */
        void onFailure(@Nullable Throwable e, @NonNull ResponseObject<T> result);

        /**
         * 如果执行到此处，ok必然为true,当然 如果是下载文件的话，此处result为null，运行到此处必然已经成功了
         *
         * @param result
         */
        void onSuccess(@NonNull T result, @NonNull ResponseObject<T> detailed);
    }

    /**
     * http 请求的完整回调
     *
     * @param <T>
     */
    public interface DetailedCallBack<T> extends CallBack<T> {
        /**
         * result不可能为空
         *
         * @param e
         * @param result
         */
        void onFailure(@Nullable Throwable e, @NonNull ResponseObject<T> result);

        /**
         * 如果执行到此处，ok必然为true,{@link ResponseObject#result}必然不为null
         * <p/>
         * 除了是下载文件的话，此处result为null，运行到此处必然已经成功了
         *
         * @param result
         * @param detailed
         */
        void onSuccess(@NonNull T result, @NonNull ResponseObject<T> detailed);

        /**
         * 请求的进度,非UI线程
         *
         * @param current
         * @param total
         */
        void onRequestProgress(long current, long total);

        /**
         * 响应的进度,非UI线程
         *
         * @param current
         * @param total
         */
        void onResponseProgress(long current, long total, boolean done);
    }

    public class RxRetryHandler implements Func1<Observable<? extends Throwable>, Observable<?>> {

        @Override
        public Observable<?> call(Observable<? extends Throwable> observable) {
            return observable
                    .flatMap(new Func1<Throwable, Observable<?>>() {
                        @Override
                        public Observable<?> call(Throwable throwable) {
                            if (shouldHandNotifier(throwable, responseObject)) {
                                notifyAction();
                                return Observable.timer(maxRetryTimes, TimeUnit.MILLISECONDS);
                            } else {
                                return Observable.error(throwable);
                            }
                        }
                    });
        }
    }

    /**
     * 上传的Interceptor
     */
    class UploadProgressInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
            ProgressRequestBody progressRequestBody = new ProgressRequestBody(originalRequest.body(), callBack);
            Request compressedRequest = originalRequest.newBuilder()
                    .method(originalRequest.method(), progressRequestBody)
                    .build();
            return chain.proceed(compressedRequest);
        }
    }

    /**
     * 下载的Interceptor
     */
    class DownloadProgressInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Response originalResponse = chain.proceed(chain.request());
            //包装响应体并返回
            return originalResponse.newBuilder()
                    .body(new ProgressResponseBody(originalResponse.body(), callBack))
                    .build();
        }
    }

    class GzipRequestInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
            if (originalRequest.body() == null || originalRequest.header("Content-Encoding") != null) {
                return chain.proceed(originalRequest);
            }

            RequestBody body = forceContentLength(gzip(originalRequest.body()));

            Request compressedRequest = originalRequest.newBuilder()
                    .header("Content-Encoding", "gzip")
                    .header("Content-Length", String.valueOf(body.contentLength()))
                    .method(originalRequest.method(), body)
                    .build();
            return chain.proceed(compressedRequest);
        }

        /**
         * https://github.com/square/okhttp/issues/350
         */
        private RequestBody forceContentLength(final RequestBody requestBody) throws IOException {
            final Buffer buffer = new Buffer();
            requestBody.writeTo(buffer);
            return new RequestBody() {
                @Override
                public MediaType contentType() {
                    return requestBody.contentType();
                }

                @Override
                public long contentLength() {
                    return buffer.size();
                }

                @Override
                public void writeTo(BufferedSink sink) throws IOException {
                    sink.write(buffer.snapshot());
                }
            };
        }

        private RequestBody gzip(final RequestBody body) {
            return new RequestBody() {
                @Override
                public MediaType contentType() {
                    return body.contentType();
                }

                @Override
                public long contentLength() {
                    return -1; // We don't know the compressed length in advance!
                }

                @Override
                public void writeTo(BufferedSink sink) throws IOException {
                    BufferedSink gzipSink = Okio.buffer(new GzipSink(sink));
                    body.writeTo(gzipSink);
                    gzipSink.close();
                }
            };
        }
    }

    public class ProgressResponseBody extends ResponseBody {
        //实际的待包装响应体
        private final ResponseBody responseBody;
        //进度回调接口
        @Nullable
        private final DetailedCallBack callBack;
        //包装完成的BufferedSource
        private BufferedSource bufferedSource;

        /**
         * 构造函数，赋值
         *
         * @param responseBody 待包装的响应体
         * @param callBack
         */
        public ProgressResponseBody(ResponseBody responseBody, @Nullable DetailedCallBack callBack) {
            this.responseBody = responseBody;
            this.callBack = callBack;
        }

        /**
         * 重写调用实际的响应体的contentType
         *
         * @return MediaType
         */
        @Override
        public MediaType contentType() {
            return responseBody.contentType();
        }

        /**
         * 重写调用实际的响应体的contentLength
         *
         * @return contentLength
         * @throws IOException 异常
         */
        @Override
        public long contentLength() {
            return responseBody.contentLength();
        }

        /**
         * 重写进行包装source
         *
         * @return BufferedSource
         * @throws IOException 异常
         */
        @Override
        public BufferedSource source() {
            if (bufferedSource == null) {
                //包装
                bufferedSource = Okio.buffer(source(responseBody.source()));
            }
            return bufferedSource;
        }


        /**
         * 读取，回调进度接口
         *
         * @param source Source
         * @return Source
         */
        private Source source(Source source) {

            return new ForwardingSource(source) {
                //当前读取字节数
                long totalBytesRead = 0L;

                @Override
                public long read(Buffer sink, long byteCount) throws IOException {
                    long bytesRead = super.read(sink, byteCount);
                    //增加当前读取的字节数，如果读取完成了bytesRead会返回-1
                    totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                    //回调，如果contentLength()不知道长度，会返回-1
                    if (callBack != null) {
                        callBack.onResponseProgress(totalBytesRead, responseBody.contentLength(), bytesRead == -1);
                    }
                    return bytesRead;
                }
            };
        }
    }

    public class ProgressRequestBody extends RequestBody {
        //实际的待包装请求体
        private final RequestBody requestBody;
        //进度回调接口
        @Nullable
        private final DetailedCallBack callBack;
        //包装完成的BufferedSink
        private BufferedSink bufferedSink;

        /**
         * 构造函数，赋值
         *
         * @param requestBody 待包装的请求体
         * @param callBack
         */
        public ProgressRequestBody(RequestBody requestBody, @Nullable DetailedCallBack callBack) {
            this.requestBody = requestBody;
            this.callBack = callBack;
        }

        /**
         * 重写调用实际的响应体的contentType
         *
         * @return MediaType
         */
        @Override
        public MediaType contentType() {
            return requestBody.contentType();
        }

        /**
         * 重写调用实际的响应体的contentLength
         *
         * @return contentLength
         * @throws IOException 异常
         */
        @Override
        public long contentLength() throws IOException {
            return requestBody.contentLength();
        }

        /**
         * 重写进行写入
         *
         * @param sink BufferedSink
         * @throws IOException 异常
         */
        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            if (bufferedSink == null) {
                //包装
                bufferedSink = Okio.buffer(sink(sink));
            }
            //写入
            requestBody.writeTo(bufferedSink);
            //必须调用flush，否则最后一部分数据可能不会被写入
            bufferedSink.flush();

        }

        /**
         * 写入，回调进度接口
         *
         * @param sink Sink
         * @return Sink
         */
        private Sink sink(Sink sink) {
            return new ForwardingSink(sink) {
                //当前写入字节数
                long bytesWritten = 0L;
                //总字节长度，避免多次调用contentLength()方法
                long contentLength = 0L;

                @Override
                public void write(Buffer source, long byteCount) throws IOException {
                    super.write(source, byteCount);
                    if (contentLength == 0) {
                        //获得contentLength的值，后续不再调用
                        contentLength = contentLength();
                    }
                    //增加当前写入的字节数
                    bytesWritten += byteCount;

                    //回调
                    if (callBack != null) {
                        callBack.onRequestProgress(bytesWritten, contentLength);
                    }
                }
            };
        }
    }
}
