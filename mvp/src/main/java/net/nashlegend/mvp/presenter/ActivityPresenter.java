package net.nashlegend.mvp.presenter;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;

import net.nashlegend.mvp.presenter.interfaze.IActivityPresenter;
import net.nashlegend.mvp.view.ActivityView;

/**
 * Created by NashLegend on 16/1/30.
 * 是类而不是接口
 */
public class ActivityPresenter<T extends ActivityView> extends Presenter implements IActivityPresenter {
    public ActivityPresenter(T view) {
        this.view = view;
    }

    public T view;

    @Override
    public void onCreate(Intent intent, Bundle savedInstanceState, PersistableBundle persistentState) {

    }

    @Override
    public void onCreate(Intent intent, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onNewIntent(Intent intent) {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onRestart() {

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
    public void onDestroy() {

    }

    @Override
    public void onBackPressed() {

    }
}
