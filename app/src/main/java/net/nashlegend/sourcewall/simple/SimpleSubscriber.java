package net.nashlegend.sourcewall.simple;

import net.nashlegend.sourcewall.util.ErrorUtils;

import rx.Subscriber;

/**
 * Created by NashLegend on 16/9/10.
 */

public abstract class SimpleSubscriber<T> extends Subscriber<T> {
    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {
        ErrorUtils.onException(e);
    }
}
