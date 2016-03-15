package net.nashlegend.mvp.sample.FragmentSample;

import net.nashlegend.mvp.presenter.FragmentPresenter;

/**
 * Created by NashLegend on 16/3/15.
 */
public class SampleFragmentPresenter extends FragmentPresenter<SampleFragmentView> implements ISampleFragmentPresenter {
    public SampleFragmentPresenter(SampleFragmentView view) {
        super(view);
    }
}
