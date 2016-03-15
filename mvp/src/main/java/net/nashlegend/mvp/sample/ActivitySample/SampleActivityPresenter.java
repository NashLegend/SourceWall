package net.nashlegend.mvp.sample.ActivitySample;

import net.nashlegend.mvp.presenter.ActivityPresenter;

/**
 * Created by NashLegend on 16/1/30.
 */
public class SampleActivityPresenter extends ActivityPresenter<SampleActivityView> implements ISampleActivityPresenter {

    public SampleActivityPresenter(SampleActivityView view) {
        super(view);
    }
}
