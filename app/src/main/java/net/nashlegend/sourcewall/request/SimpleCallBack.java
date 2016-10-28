package net.nashlegend.sourcewall.request;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * http 请求基本回调
 *
 * @param <T>
 */
public class SimpleCallBack<T> implements RequestCallBack<T> {
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

