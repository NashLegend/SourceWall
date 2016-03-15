package net.nashlegend.mvp.sample.FragmentSample;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import net.nashlegend.mvp.presenter.interfaze.IFragmentPresenter;
import net.nashlegend.mvp.view.FragmentView;

/**
 * Created by NashLegend on 16/3/15.
 */
public abstract class BaseFragment<T extends IFragmentPresenter>
        extends Fragment implements FragmentView {
    public T presenter;

    public BaseFragment() {
        setPresenter();
    }

    public void setPresenter() {
        presenter = createPresenter();
    }

    /**
     * 这特么的是不是过度设计了呢，当然是……
     *
     * @return
     */
    @NonNull
    public abstract T createPresenter();
}
