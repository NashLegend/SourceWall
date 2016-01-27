package net.nashlegend.request;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import net.nashlegend.request.parsers.Parser;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.HashMap;

/**
 * Created by NashLegend on 2015/9/23 0023.
 * 网络请求的对象
 */
public class RequestObject<T> {

    /**
     * 默认Tag
     */
    public static final String DefaultTag = "Default";

    public int requestType = RequestType.PLAIN;

    public int method = Method.POST;

    public HashMap<String, String> params = new HashMap<>();

    public String url = "";

    public boolean isAsync = false;

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
        isAsync = object.isAsync;
        tag = object.tag;
        uploadParamKey = object.uploadParamKey;
        mediaType = object.mediaType;
    }

    /**
     * 同步请求
     *
     * @return
     */
    public ResponseObject<T> requestSync() {
        ResponseObject<T> response = new ResponseObject<>();
        try {
            ResponseObject<String> responseObject;
            switch (method) {
                case Method.GET:
                    responseObject = HttpUtil.get(url, params, tag);
                    break;
                case Method.PUT:
                    responseObject = HttpUtil.put(url, params, tag);
                    break;
                case Method.DELETE:
                    responseObject = HttpUtil.delete(url, params, tag);
                    break;
                default:
                    responseObject = HttpUtil.post(url, params, tag);
                    break;
            }
            response.copyPartFrom(responseObject);
            if (parser != null) {
                response.result = parser.parse(responseObject.result, response);
            }
        } catch (Exception e) {
            JsonHandler.handleRequestException(e, response);
        }
        return response;
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

    public ResponseObject<T> uploadSync() {
        ResponseObject<T> response = new ResponseObject<>();
        try {
            ResponseObject<String> responseObject = HttpUtil.upload(url, params,
                    uploadParamKey, mediaType, filePath);
            response.copyPartFrom(responseObject);
            if (parser != null) {
                response.result = parser.parse(responseObject.result, response);
            }
        } catch (Exception e) {
            JsonHandler.handleRequestException(e, response);
        }
        return response;
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
                && isAsync && retryHandler != null
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
        err.append("    ").append("isAsync").append(":").append(isAsync).append("\n");
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
}
