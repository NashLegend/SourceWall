package net.nashlegend.mvp.presenter.interfaze;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by NashLegend on 16/1/30.
 */
public interface IFragmentPresenter extends IPresenter {
    public void onAttach(Context context);

    public void onCreate(@Nullable Bundle savedInstanceState);

    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);

    public void onActivityCreated(@Nullable Bundle savedInstanceState);

    public void onStart();

    public void onResume();

    public void onPause();

    public void onStop();

    public void onDestroyView();

    public void onDestroy();

    public void onDetach();
}
