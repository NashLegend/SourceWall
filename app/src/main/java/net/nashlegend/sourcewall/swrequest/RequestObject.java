package net.nashlegend.sourcewall.swrequest;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import net.nashlegend.sourcewall.swrequest.parsers.Parser;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.HashMap;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
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
    public static final String DefaultTag = "Default";

    private ResponseObject<T> responseObject = new ResponseObject<>();

    private boolean isReactable = false;

    public int requestType = RequestType.PLAIN;

    public int method = Method.POST;

    public HashMap<String, String> params = new HashMap<>();

    public String url = "";

    public CallBack<T> callBack = null;

    public Parser<T> parser;

    public Object tag = DefaultTag;

    public String uploadParamKey = "file";

    public MediaType mediaType = null;

    public RetryHandler retryHandler;

    public String cache;// TODO: 2015/10/20 0020

    public String filePath = "";

    public Callback callback;

    public boolean ignoreHandler = false;

    public Handler handler;

    private Call call = null;

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
     * 异步请求，如果在enqueue执行之前就执行了cancel，那么将不会有callback执行，用户将不知道已经取消了请求。
     * 我们在请求中已经添加了synchronized，所以不考虑这种情况了
     */
    public void requestAsync() {
        handleHandler();
        switch (method) {
            case Method.GET:
                call = HttpUtil.getAsync(url, params, getInnerCallback(), tag);
                break;
            case Method.PUT:
                call = HttpUtil.putAsync(url, params, getInnerCallback(), tag);
                break;
            case Method.DELETE:
                call = HttpUtil.deleteAsync(url, params, getInnerCallback(), tag);
                break;
            default:
                call = HttpUtil.postAsync(url, params, getInnerCallback(), tag);
                break;
        }
    }

    /**
     * 同步请求
     *
     * @return
     */
    private Response requestSync() throws Exception {
        Response response;
        switch (method) {
            case Method.GET:
                response = HttpUtil.get(url, params, tag);
                break;
            case Method.PUT:
                response = HttpUtil.put(url, params, tag);
                break;
            case Method.DELETE:
                response = HttpUtil.delete(url, params, tag);
                break;
            default:
                response = HttpUtil.post(url, params, tag);
                break;
        }
        return response;
    }

    /**
     * 异步请求，并不立即执行，仅仅返回Observable
     */
    public Observable<ResponseObject<T>> requestObservable() {
        isReactable = true;
        // TODO: 16/1/28 Retry以后再说
        return Observable
                .create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        try {
                            Response response = requestSync();
                            responseObject.statusCode = response.code();
                            String result = response.body().string();
                            subscriber.onNext(result);
                            subscriber.onCompleted();
                        } catch (Exception e) {
                            subscriber.onError(e);
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .onErrorResumeNext(new Func1<Throwable, Observable<? extends String>>() {
                    @Override
                    public Observable<? extends String> call(Throwable throwable) {
                        return Observable.just("Error!");
                    }
                })
                .map(new Func1<String, ResponseObject<T>>() {
                    @Override
                    public ResponseObject<T> call(String string) {
                        if (parser != null) {
                            try {
                                responseObject.result = parser.parse(string, responseObject);
                            } catch (Exception ignored) {

                            }
                        }
                        return responseObject;
                    }
                })
                .subscribeOn(Schedulers.computation());
    }

    public Subscription requestObservable(Subscriber<ResponseObject<T>> subscriber) {
        return requestObservable().observeOn(AndroidSchedulers.mainThread()).subscribe(subscriber);
    }

    public void uploadAsync() {
        handleHandler();
        HttpUtil.uploadAsync(url, params, uploadParamKey, mediaType, filePath, getInnerCallback());
    }

    private void handleHandler() {
        if (Thread.currentThread().getId() == 1) {
            //是果是在主线程请求,且handler为null，则将其置为在主线程执行callback
            if (!ignoreHandler && handler == null) {
                handler = new Handler(Looper.getMainLooper());
            }
        }
    }

    private Callback getInnerCallback() {
        return new Callback() {
            @Override
            synchronized public void onFailure(Request request, final IOException e) {
                final ResponseObject<T> responseObject = new ResponseObject<>();
                responseObject.requestObject = RequestObject.this;
                JsonHandler.handleRequestException(e, responseObject);
                onRequestFailure(e, responseObject);
            }

            @Override
            synchronized public void onResponse(Response response) throws IOException {
                final ResponseObject<T> responseObject = new ResponseObject<>();
                responseObject.requestObject = RequestObject.this;
                if (callBack != null && parser != null) {
                    try {
                        int statusCode = response.code();
                        String result = response.body().string();
                        responseObject.statusCode = statusCode;
                        responseObject.body = result;
                        if (response.isSuccessful()) {
                            responseObject.result = parser.parse(result, responseObject);
                        }
                        if (handler != null) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (responseObject.ok) {
                                        callBack.onResponse(responseObject);
                                    } else {
                                        onRequestFailure(null, responseObject);
                                    }
                                }
                            });
                        } else {
                            if (responseObject.ok) {
                                callBack.onResponse(responseObject);
                            } else {
                                onRequestFailure(null, responseObject);
                            }
                        }
                    } catch (final Exception e) {
                        JsonHandler.handleRequestException(e, responseObject);
                        onRequestFailure(e, responseObject);
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
    private void onRequestFailure(final Exception e, final ResponseObject<T> result) {
        if (call != null
                && !call.isCanceled()
                && requestType == RequestType.PLAIN
                && retryHandler != null
                && !retryHandler.isTerminated()
                && retryHandler.shouldHandNotifier(e, result)) {
            if (retryHandler.span > 0) {
                if (Thread.currentThread().getId() == 1) {
                    //如果在主线程
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            requestAsync();
                        }
                    }, retryHandler.span);
                } else {
                    try {
                        Thread.sleep(retryHandler.span);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    } finally {
                        requestAsync();
                    }
                }
            } else {
                requestAsync();
            }
            retryHandler.notifyAction();
        } else {
            if (call != null && call.isCanceled()) {
                result.error = ResponseError.CANCELLED;
                result.isCancelled = true;
            }
            // TODO: 16/1/28
//            CommonUtil.reportRequestError(e, result);
            if (callBack != null) {
                if (handler != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callBack.onFailure(e, result);
                        }
                    });
                } else {
                    callBack.onFailure(e, result);
                }
            }
        }
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
        int DOWNLOAD = 2;
    }

    /**
     * http 请求回调
     *
     * @param <T>
     */
    public interface CallBack<T> {
        /**
         * result不可能为空
         *
         * @param e
         * @param result
         */
        void onFailure(@Nullable Exception e, @NonNull ResponseObject<T> result);

        /**
         * 如果执行到此处，必然code==0,ok必然为true
         *
         * @param result
         */
        void onResponse(@NonNull ResponseObject<T> result);
    }

    private static void requestVolley() {
        Thread.setDefaultUncaughtExceptionHandler(null);
        throw new IllegalStateException("Base context already set");
    }

    public static class RetryHandler {
        int maxTimes = 0;//最大重试次数
        int span = 0;//重试间隔
        int crtTime = 0;//当前重试次数
        boolean terminated;

        public RetryHandler(int maxTimes, int span) {
            this.maxTimes = maxTimes;
            this.span = span;
        }

        public boolean shouldHandNotifier(Exception exception, ResponseObject responseObject) {
            return responseObject.code != ResponseCode.CODE_TOKEN_INVALID
                    && crtTime < maxTimes
                    && !(exception instanceof InterruptedIOException)
                    && (responseObject.statusCode < 300 || responseObject.statusCode >= 500)
                    && !terminated;
        }

        public void notifyAction() {
            crtTime++;
            if (crtTime >= maxTimes) {
                terminate();
            }
        }

        public void terminate() {
            terminated = true;
        }

        public boolean isTerminated() {
            return terminated;
        }
    }

    // TODO: 16/1/28
    public class RxRetryHandler implements Func1<Observable<? extends Throwable>, Observable<?>> {

        @Override
        public Observable<?> call(Observable<? extends Throwable> observable) {
            return null;
        }
    }
}
