package net.nashlegend.mvp.sample.ActivitySample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by NashLegend on 16/1/30.
 */
public class SampleActivity extends BaseActivity<SampleActivityPresenter> implements SampleActivityView {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter.doSomething();
    }

    @NonNull
    @Override
    public SampleActivityPresenter createPresenter() {
        return new SampleActivityPresenter(this);
    }

    @Override
    public void doSomething() {
        System.out.println("do something");
    }
}
