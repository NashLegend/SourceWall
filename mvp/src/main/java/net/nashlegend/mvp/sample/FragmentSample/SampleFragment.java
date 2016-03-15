package net.nashlegend.mvp.sample.FragmentSample;

import android.support.annotation.NonNull;

/**
 * Created by NashLegend on 16/3/15.
 */
public class SampleFragment extends BaseFragment<SampleFragmentPresenter> implements SampleFragmentView {
    @NonNull
    @Override
    public SampleFragmentPresenter createPresenter() {
        return new SampleFragmentPresenter(this);
    }
}
