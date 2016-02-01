package net.nashlegend.mvp.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by NashLegend on 16/1/30.
 */
public class SampleActivity extends BaseActivity<SamplePresenter> implements SampleView {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPresenter();
        presenter.doSomething();
    }

    @NonNull
    @Override
    public SamplePresenter initPresenter() {
        return new SamplePresenter(this);
    }

    @Override
    public void doSomething() {
        System.out.println("do something");
    }
}
