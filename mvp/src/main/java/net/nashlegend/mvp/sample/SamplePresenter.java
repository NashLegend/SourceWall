package net.nashlegend.mvp.sample;

import net.nashlegend.mvp.presenter.ActivityPresenter;

/**
 * Created by NashLegend on 16/1/30.
 */
public class SamplePresenter extends ActivityPresenter<SampleView> implements ISamplePresenter {
    public SamplePresenter(SampleView view) {
        this.view = view;
    }

    @Override
    public void doSomething() {
        view.doSomething();
    }
}
