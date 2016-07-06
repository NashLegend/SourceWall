package net.nashlegend.sourcewall.request;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import net.nashlegend.sourcewall.request.RequestObject.Method;
import net.nashlegend.sourcewall.request.RequestObject.RequestType;
import net.nashlegend.sourcewall.request.cache.CacheHeaderUtil;
import net.nashlegend.sourcewall.request.cache.RequestCache;
import net.nashlegend.sourcewall.request.interceptors.DownloadProgressInterceptor;
import net.nashlegend.sourcewall.request.interceptors.GzipRequestInterceptor;
import net.nashlegend.sourcewall.request.interceptors.UploadProgressInterceptor;
import net.nashlegend.sourcewall.util.ErrorUtils;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.Exceptions;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by NashLegend on 16/7/6.
 */

public class NetworkTask<T> {

    private RequestDelegate delegate;
    private OkHttpClient okHttpClient;
    private final ResponseObject<T> responseObject = new ResponseObject<>();
    private int crtTime = 0;//当前重试次数
    private Call call = null;
    private String cacheKey = null;
    private boolean softCancelled = false;//取消掉一个请求，但是并不中断请求，只是不再执行CallBack,请求完成后无任何动作


    RequestObject<T> request;

    public NetworkTask(@NonNull RequestObject<T> request) {
        this.request = request;
    }

    synchronized public RequestDelegate getDelegate() {
        if (delegate == null) {
            delegate = new RequestDelegate(getHttpClient());
        }
        return new RequestDelegate(getHttpClient());
    }

    synchronized public OkHttpClient getHttpClient() {
        if (okHttpClient == null) {
            String key;
            switch (request.requestType) {
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
            if (request.requestWithGzip) {
                key += "requestWithGzip";
            }
            OkHttpClient tmpClient = HttpUtil.getOkHttpClient(key);
            if (tmpClient == null) {
                switch (request.requestType) {
                    case RequestType.UPLOAD:
                        tmpClient = HttpUtil.getDefaultUploadHttpClient().newBuilder()
                                .addNetworkInterceptor(new UploadProgressInterceptor(request.callBack)).build();
                        break;
                    case RequestType.DOWNLOAD:
                        tmpClient = HttpUtil.getDefaultUploadHttpClient().newBuilder()
                                .addNetworkInterceptor(new DownloadProgressInterceptor(request.callBack)).build();
                        break;
                    default:
                        tmpClient = HttpUtil.getDefaultHttpClient();
                        break;
                }
                if (request.requestWithGzip) {
                    tmpClient = tmpClient.newBuilder().addNetworkInterceptor(new GzipRequestInterceptor()).build();
                }
                HttpUtil.putOkHttpClient(key, tmpClient);
            }
            okHttpClient = tmpClient;
        }
        return okHttpClient;
    }

    // TODO: 16/7/6


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
                    keyBuilder.append(request.method).append("/{").append(request.url).append("}/");
                    if (request.params.size() > 0) {
                        ArrayList<Param> entryArrayList = new ArrayList<>();
                        for (Param param : request.params) {
                            if (TextUtils.isEmpty(param.key)) {
                                continue;
                            }
                            entryArrayList.add(param);
                        }
                        Collections.sort(entryArrayList, new Comparator<Param>() {
                            @Override
                            public int compare(Param lhs, Param rhs) {
                                return lhs.key.compareTo(rhs.key);
                            }
                        });
                        if (entryArrayList.size() > 0) {
                            keyBuilder.append("?");
                            for (Param param : entryArrayList) {
                                keyBuilder.append(param.key).append("=").append(param.value).append("&");
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
        return System.currentTimeMillis() - CacheHeaderUtil.readTime(getCachedKey()) > request.cacheTimeOut;
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
        return request.useCachedIfFailed || request.useCachedFirst;
    }

    public void softCancel() {
        this.softCancelled = true;
    }

    public boolean isSoftCancelled() {
        return softCancelled;
    }

    /**
     * 同步请求
     */
    private Call syncRequest() throws Exception {
        switch (request.requestType) {
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
        return getDelegate().upload(request);
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
        switch (request.method) {
            case Method.GET:
                call = getDelegate().get(request);
                break;
            case Method.PUT:
                call = getDelegate().put(request);
                break;
            case Method.DELETE:
                call = getDelegate().delete(request);
                break;
            default:
                call = getDelegate().post(request);
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
    private Observable<ResponseObject<T>> cachedObservable() {
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
                        if (s == null || request.parser == null) {
                            return null;
                        } else {
                            try {
                                responseObject.result = request.parser.parse(s, responseObject);
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
    private Observable<ResponseObject<T>> networkObservable() {
        return Observable
                .create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        try {
                            responseObject.isCached = false;
                            call = syncRequest();
                            Response response = call.execute();
                            if (request.requestType == RequestType.DOWNLOAD) {
                                // 下载请求在此处就确定了请求的结果responseObject.ok，可以无parser，
                                // 当然在parse处可以推翻此结论
                                Throwable throwable = null;
                                File downloadedFile = new File(request.downloadFilePath);
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
                                    subscriber.onNext(request.downloadFilePath);
                                } else {
                                    Exceptions.throwOrReport(throwable, subscriber, request);
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
                            Exceptions.throwOrReport(e, subscriber, request);
                        }
                    }
                })
                .retryWhen(new RxRetryHandler())
                .onErrorResumeNext(new Func1<Throwable, Observable<? extends String>>() {
                    @Override
                    public Observable<? extends String> call(Throwable throwable) {
                        if ((call == null || !call.isCanceled()) && request.useCachedIfFailed) {
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
                        if (responseObject.throwable == null && request.parser != null) {
                            try {
                                responseObject.result = request.parser.parse(string, responseObject);
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
    public Observable<ResponseObject<T>> flatMap() {
        Observable<ResponseObject<T>> observable;
        if (request.useCachedFirst) {
            observable = cachedObservable()
                    .flatMap(new Func1<ResponseObject<T>, Observable<ResponseObject<T>>>() {
                        @Override
                        public Observable<ResponseObject<T>> call(ResponseObject<T> r) {
                            if (r == null) {
                                return networkObservable();
                            } else {
                                return Observable.just(r);
                            }
                        }
                    });
        } else {
            observable = networkObservable();
        }
        return observable.subscribeOn(Schedulers.io());
    }

    /**
     * 通过RxJava的方式执行请求，与requestAsync一样，CallBack执行在主线程上
     */
    public NetworkTask<T> requestRx() {
        flatMap()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseObject<T>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        //requestObservable很难走到onError，因为都已经封好了，否则第二个参数不太好传给别人
                        if (!softCancelled && request.callBack != null) {
                            request.callBack.onFailure(e, responseObject);
                        }
                    }

                    @Override
                    public void onNext(ResponseObject<T> tResponseObject) {
                        if (!softCancelled && request.callBack != null) {
                            if (tResponseObject.ok) {
                                request.callBack.onSuccess(tResponseObject.result, tResponseObject);
                            } else {
                                request.callBack.onFailure(tResponseObject.throwable, tResponseObject);
                            }
                        }
                    }
                });
        return this;
    }


    public boolean shouldHandNotifier(Throwable exception, ResponseObject responseObject) {
        return responseObject.code != ResponseCode.CODE_TOKEN_INVALID
                && call != null
                && !call.isCanceled()
                && crtTime < request.maxRetryTimes
                && !(exception instanceof InterruptedIOException)
                && (responseObject.statusCode < 300 || responseObject.statusCode >= 500);
    }

    public void notifyAction() {
        crtTime++;
    }

    /**
     * 同步请求
     */
    public ResponseObject<T> startRequestSync() {
        try {
            Call call = syncRequest();
            Response response = call.execute();
            if (request.requestType == RequestObject.RequestType.DOWNLOAD) {
                File downloadedFile = new File(request.downloadFilePath);
                BufferedSink sink = null;
                try {
                    sink = Okio.buffer(Okio.sink(downloadedFile));
                    sink.writeAll(response.body().source());
                    responseObject.ok = true;
                } catch (Exception e) {
                    responseObject.ok = false;
                } finally {
                    if (sink != null) {
                        sink.close();
                    }
                }
                if (responseObject.ok) {
                    responseObject.body = request.downloadFilePath;
                }
            } else {
                String result = response.body().string();
                responseObject.statusCode = response.code();
                responseObject.body = result;
                if (responseObject.throwable == null && request.parser != null) {
                    try {
                        responseObject.result = request.parser.parse(result, responseObject);
                    } catch (Exception e) {
                        JsonHandler.handleRequestException(e, responseObject);
                    }
                }
                if (!responseObject.ok && responseObject.throwable != null) {
                    ErrorUtils.dumpRequestError(responseObject.throwable, responseObject);
                }
            }
            return responseObject;
        } catch (Exception e) {
            ErrorUtils.onException(e);
            return responseObject;
        }
    }

    /**
     * 重新请求
     */
    public NetworkTask<T> startRequestAsync() {
        switch (request.requestType) {
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
        return this;
    }

    /**
     * 异步上传
     */
    private void uploadAsync() {
        prepareHandler();
        getDelegate().uploadAsync(request, getInnerCallback());
    }

    /**
     * 异步下载
     */
    private void downloadAsync() {
        prepareHandler();
        requestAsync(getInnerDownloadCallback());
    }

    /**
     * 异步请求，如果在enqueue执行之前就执行了cancel，那么将不会有callback执行，用户将不知道已经取消了请求。
     * 我们在请求中已经添加了synchronized，所以不考虑这种情况了
     */
    private void requestAsync() {
        requestAsync(getInnerCallback());
    }

    /**
     * 异步请求，如果在enqueue执行之前就执行了cancel，那么将不会有callback执行，用户将不知道已经取消了请求。
     * 我们在请求中已经添加了synchronized，所以不考虑这种情况了
     */
    public void requestAsync(Callback callback) {
        prepareHandler();
        switch (request.method) {
            case Method.GET:
                call = getDelegate().getAsync(request, callback);
                break;
            case Method.PUT:
                call = getDelegate().putAsync(request, callback);
                break;
            case Method.DELETE:
                call = getDelegate().deleteAsync(request, callback);
                break;
            default:
                call = getDelegate().postAsync(request, callback);
                break;
        }
    }

    private void prepareHandler() {
        if (Thread.currentThread().getId() == 1) {
            //是果是在主线程请求,且handler为null，则将其置为在主线程执行callback
            if (!request.ignoreHandler && request.handler == null) {
                request.handler = new Handler(Looper.getMainLooper());
            }
        }
    }

    private Callback getInnerCallback() {
        return new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                final ResponseObject<T> responseObject = new ResponseObject<>();
                responseObject.requestObject = request;
                JsonHandler.handleRequestException(e, responseObject);
                if ((call == null || !call.isCanceled()) && request.useCachedIfFailed) {
                    //只有在此处才可，其他地方有可能只是别的错误而非请求错误
                    try {
                        String cachedResult = readFromCache();
                        if (cachedResult != null) {
                            responseObject.isCached = true;
                            responseObject.body = cachedResult;
                            if (request.parser != null) {
                                responseObject.result = request.parser.parse(cachedResult, responseObject);
                            }
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
                responseObject.requestObject = request;
                if (request.callBack != null && request.parser != null) {
                    Throwable throwable = null;
                    try {
                        int statusCode = response.code();
                        String result = response.body().string();
                        responseObject.statusCode = statusCode;
                        responseObject.body = result;
                        if (request.parser != null) {
                            responseObject.result = request.parser.parse(result, responseObject);
                        }
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

    private Callback getInnerDownloadCallback() {
        return new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                final ResponseObject<T> responseObject = new ResponseObject<>();
                responseObject.requestObject = request;
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
                responseObject.requestObject = request;
                if (request.callBack != null) {
                    Throwable throwable = null;
                    File downloadedFile = new File(request.downloadFilePath);
                    BufferedSink sink = null;
                    try {
                        sink = Okio.buffer(Okio.sink(downloadedFile));
                        sink.writeAll(response.body().source());
                        responseObject.ok = true;
                        if (request.parser != null) {
                            request.parser.parse(request.downloadFilePath, responseObject);
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
    private void onRequestFailure(final Throwable e, final ResponseObject<T> result) {
        if (call != null && request.requestType == RequestType.PLAIN && shouldHandNotifier(e, result)) {
            try {
                Thread.sleep(request.interval);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            } finally {
                startRequestAsync();
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

    private void callSuccess(@NonNull final ResponseObject<T> responseObject) {
        if (!softCancelled && request.callBack != null) {
            if (request.handler != null) {
                request.handler.post(new Runnable() {
                    @Override
                    public void run() {
                        request.callBack.onSuccess(responseObject.result, responseObject);
                    }
                });
            } else {
                request.callBack.onSuccess(responseObject.result, responseObject);
            }
        }
    }

    private void callFailure(@Nullable final Throwable e, @NonNull final ResponseObject<T> responseObject) {
        if (!softCancelled && request.callBack != null) {
            if (request.handler != null) {
                request.handler.post(new Runnable() {
                    @Override
                    public void run() {
                        request.callBack.onFailure(e, responseObject);
                    }
                });
            } else {
                request.callBack.onFailure(e, responseObject);
            }
        }
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
                                return Observable.timer(request.maxRetryTimes, TimeUnit.MILLISECONDS);
                            } else {
                                return Observable.error(throwable);
                            }
                        }
                    });
        }
    }


}
