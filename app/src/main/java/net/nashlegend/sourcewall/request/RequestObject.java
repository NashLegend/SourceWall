package net.nashlegend.sourcewall.request;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.nashlegend.sourcewall.request.parsers.Parser;

import java.util.ArrayList;
import java.util.List;

import okhttp3.CacheControl;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * Created by NashLegend on 2015/9/23 0023.
 * 网络请求的对象
 */
public class RequestObject<T> {
    /**
     * 默认Tag
     */
    public static final String DefaultTag = "RequestObject";

    public String url = "";
    public Method method = Method.GET;
    public Object tag = DefaultTag;
    public RequestBody requestBody;
    public Headers.Builder headers;
    public CacheControl cacheControl;

    public boolean requestWithGzip = false;//是否使用Gzip压缩请求数据
    public String fakeResponse = null;
    public int maxRetryTimes = 0;//最大重试次数
    public int interval = 0;//重试间隔
    public int requestType = RequestType.PLAIN;
    public final List<Param> params = new ArrayList<>();
    public RequestCallBack<T> callBack = null;
    public Parser<T> parser;
    public String uploadFileKey = "file";
    public MediaType mediaType = null;
    public String uploadFilePath = "";
    public String downloadFilePath = "";

    /**
     * 是否优先使用缓存，与useCacheIfFailed互斥
     */
    protected boolean useCachedFirst = false;
    /**
     * 请求失败时是否使用缓存代理，与useCachedFirst互斥
     */
    protected boolean useCachedIfFailed = false;
    /**
     * 缓存时间，如果上次保存的缓存时间与本次请求的时间差相差超过了cacheTimeOut，则重新请求一次
     * <p>
     * 果壳貌似本身并没有Cache-Control，或者Cache-Control的max-age=0 所以这里的缓存是本地缓存
     */
    protected long cacheTimeOut = -1;//如果是-1则表示永不超时

    public RequestObject() {
        this.method = Method.GET;
        this.headers = new Headers.Builder();
    }

    public void copyPartFrom(@NonNull RequestObject<?> object) {
        params.clear();
        for (int i = 0; i < object.params.size(); i++) {
            Param param = object.params.get(i);
            params.add(param.copy());
        }
        method = object.method;
        url = object.url;
        tag = object.tag;
        uploadFileKey = object.uploadFileKey;
        mediaType = object.mediaType;
    }

    public String dump() {
        StringBuilder err = new StringBuilder();
        err.append("\t\t").append("params").append(":").append(Urls.getQueryString(params)).append("\n");
        err.append("\t\t").append("method").append(":").append(method).append("\n");
        err.append("\t\t").append("url").append(":").append(url).append("\n");
        err.append("\t\t").append("tag").append(":").append(tag).append("\n");
        if (requestType == RequestType.UPLOAD) {
            err.append("\t\t").append("uploadFileKey").append(":").append(uploadFileKey).append("\n");
            err.append("\t\t").append("mediaType").append(":").append(mediaType).append("\n");
        }
        return err.toString();
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
     * http 请求基本回调
     *
     * @param <T>
     */
    public static class SimpleCallBack<T> implements RequestCallBack<T> {
        @Override
        public void onFailure(@Nullable Throwable e, @NonNull ResponseObject<T> result) {
            onFailure(result);
        }

        @Override
        public void onSuccess(@NonNull T result, @NonNull ResponseObject<T> detailed) {
            onSuccess(result);
        }


        @Override
        public void onRequestProgress(long current, long total) {

        }

        @Override
        public void onResponseProgress(long current, long total, boolean done) {

        }

        public void onSuccess(@NonNull T result) {
            onSuccess();
        }

        public void onSuccess() {

        }

        public void onFailure(@NonNull ResponseObject<T> result) {
            onFailure();
        }

        public void onFailure() {

        }
    }

    /**
     * http 请求基本回调
     *
     * @param <T>
     */
    public static abstract class CallBack<T> implements RequestCallBack<T> {
        @Override
        public void onRequestProgress(long current, long total) {

        }

        @Override
        public void onResponseProgress(long current, long total, boolean done) {

        }
    }

    /**
     * http 请求的完整回调
     *
     * @param <T>
     */
    public interface RequestCallBack<T> {
        /**
         * result不可能为空
         *
         * @param e
         * @param result
         */
        void onFailure(@Nullable Throwable e, @NonNull ResponseObject<T> result);

        /**
         * 如果执行到此处，ok必然为true,{@link ResponseObject#result}必然不为null
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

}
