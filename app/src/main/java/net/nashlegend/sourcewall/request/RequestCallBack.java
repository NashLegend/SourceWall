package net.nashlegend.sourcewall.request;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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