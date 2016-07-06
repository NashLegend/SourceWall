package net.nashlegend.sourcewall.request;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.nashlegend.sourcewall.request.parsers.Parser;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;

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
    public int maxRetryTimes = 0;//最大重试次数
    public int interval = 0;//重试间隔
    public int requestType = RequestType.PLAIN;
    public int method = Method.POST;
    public final List<Param> params = new ArrayList<>();
    public String url = "";
    public DetailedCallBack<T> callBack = null;
    public Parser<T> parser;
    public Object tag = DefaultTag;
    public String uploadFileKey = "file";
    public MediaType mediaType = null;
    public String uploadFilePath = "";
    public String downloadFilePath = "";

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

    @SuppressWarnings("unchecked")
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

    @SuppressWarnings("StringBufferReplaceableByString")
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
     * http 请求基本回调
     *
     * @param <T>
     */
    public static abstract class CallBack<T> implements DetailedCallBack<T> {
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
    public interface DetailedCallBack<T> {
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
