package net.nashlegend.sourcewall.request;

import android.support.annotation.NonNull;

import net.nashlegend.sourcewall.request.RequestObject.Method;
import net.nashlegend.sourcewall.request.RequestObject.RequestType;
import net.nashlegend.sourcewall.request.cache.CacheHeaderUtil;
import net.nashlegend.sourcewall.request.cache.RequestCache;
import net.nashlegend.sourcewall.request.interceptors.DownloadProgressInterceptor;
import net.nashlegend.sourcewall.request.interceptors.GzipRequestInterceptor;
import net.nashlegend.sourcewall.request.interceptors.UploadProgressInterceptor;
import net.nashlegend.sourcewall.util.ErrorUtils;

import java.io.File;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.Exceptions;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static net.nashlegend.sourcewall.request.HttpUtil.SO_TIMEOUT;
import static net.nashlegend.sourcewall.request.HttpUtil.WRITE_TIMEOUT;

/**
 * Created by NashLegend on 16/7/6.
 */

public class NetworkTask<T> {

    private RequestDelegate delegate;
    private OkHttpClient okHttpClient;
    private int crtTime = 0;//当前重试次数
    private Call call = null;
    private Subscription subscription;
    private String cacheKey = null;
    private boolean dismissed = false;//取消掉一个请求，但是并不中断请求，只是不再执行CallBack,请求完成后无任何动作

    public final ResponseObject<T> responseObject = new ResponseObject<>();
    public RequestObject<T> request;

    public NetworkTask(@NonNull RequestObject<T> request) {
        this.request = request;
    }

    synchronized private RequestDelegate getDelegate() {
        if (delegate == null) {
            delegate = new RequestDelegate(getHttpClient());
        }
        return delegate;
    }

    synchronized private OkHttpClient getHttpClient() {
        if (okHttpClient == null) {
            OkHttpClient.Builder builder = HttpUtil.getDefaultHttpClient().newBuilder();
            switch (request.requestType) {
                case RequestType.UPLOAD:
                    builder.readTimeout(SO_TIMEOUT * 10, MILLISECONDS)
                            .writeTimeout(WRITE_TIMEOUT * 10, MILLISECONDS)
                            .addNetworkInterceptor(new UploadProgressInterceptor(request.callBack));
                    break;
                case RequestType.DOWNLOAD:
                    builder.readTimeout(SO_TIMEOUT * 10, MILLISECONDS)
                            .writeTimeout(WRITE_TIMEOUT * 50, MILLISECONDS)
                            .addNetworkInterceptor(new DownloadProgressInterceptor(request.callBack));
                    break;
                default:
                    //do nothing
                    break;
            }
            if (request.requestWithGzip) {
                builder.addNetworkInterceptor(new GzipRequestInterceptor()).build();
            }
            okHttpClient = builder.build();
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
                    keyBuilder.append(request.method).append("/{").append(request.url).append("}/");
                    if (request.params.size() > 0) {
                        ArrayList<Param> entryArrayList = new ArrayList<>();
                        for (Param param : request.params) {
                            if (Utils.isEmpty(param.key)) {
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
        if (request.cacheTimeOut < 0) {
            return false;
        } else {
            return System.currentTimeMillis() - CacheHeaderUtil.readTime(getCachedKey()) > request.cacheTimeOut;
        }
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
     * 清除缓存
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

    public void cancel() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
        if (call != null && !call.isCanceled()) {
            call.cancel();
        }
        dismiss();
    }

    public boolean isCancelled() {
        return call != null && call.isCanceled();
    }

    public void dismiss() {
        this.dismissed = true;
    }

    public boolean isDismissed() {
        return dismissed;
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
                                if (responseObject.ok) {
                                    return responseObject;
                                } else {
                                    removeCache();
                                    return null;
                                }
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
                        if (!responseObject.ok) {
                            ErrorUtils.dumpRequest(responseObject);
                            if (responseObject.isCached) {
                                removeCache();
                            }
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
    public Subscription requestRx() {
        subscription = flatMap()
                .takeLast(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseObject<T>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        //requestObservable基本走不到onError，因为都已经封好了，否则第二个参数不太好传给别人
                        if (!dismissed && request.callBack != null) {
                            request.callBack.onFailure(e, responseObject);
                        }
                    }

                    @Override
                    public void onNext(ResponseObject<T> tResponseObject) {
                        if (!dismissed && request.callBack != null) {
                            if (tResponseObject.ok) {
                                request.callBack.onSuccess(tResponseObject.result, tResponseObject);
                            } else {
                                request.callBack.onFailure(tResponseObject.throwable, tResponseObject);
                            }
                        }
                    }
                });
        return subscription;
    }

    /**
     * 通过RxJava的方式执行请求，与requestAsync一样，CallBack执行在主线程上
     */
    public NetworkTask<T> startRequestAsync() {
        requestRx();
        return this;
    }

    private boolean shouldHandNotifier(Throwable exception, ResponseObject responseObject) {
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
                if (!responseObject.ok) {
                    ErrorUtils.dumpRequest(responseObject);
                }
            }
            return responseObject;
        } catch (Exception e) {
            ErrorUtils.onException(e);
            return responseObject;
        }
    }

    public Subscription getSubscription() {
        return subscription;
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
                                return Observable.timer(request.maxRetryTimes, MILLISECONDS);
                            } else {
                                return Observable.error(throwable);
                            }
                        }
                    });
        }
    }


}
