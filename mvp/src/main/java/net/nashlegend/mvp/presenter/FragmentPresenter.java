package net.nashlegend.mvp.presenter;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.nashlegend.mvp.presenter.interfaze.IFragmentPresenter;
import net.nashlegend.mvp.view.FragmentView;

/**
 * Created by NashLegend on 16/1/30.
 */
public class FragmentPresenter<T extends FragmentView>
        extends Presenter implements IFragmentPresenter {

    @Override
    public void onAttach(Context context) {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return null;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onDestroyView() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onDetach() {

    }
}
