package net.nashlegend.sourcewall.request;

/**
 * http 请求基本回调
 */
public abstract class CallBack<T> implements RequestCallBack<T> {
    @Override
    public void onRequestProgress(long current, long total) {

    }

    @Override
    public void onResponseProgress(long current, long total, boolean done) {

    }
}
