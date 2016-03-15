package net.nashlegend.mvp.presenter.interfaze;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;

/**
 * Created by NashLegend on 16/1/30.
 */
public interface IActivityPresenter extends IPresenter {

    void onCreate(Intent intent, Bundle savedInstanceState, PersistableBundle persistentState);

    void onCreate(Intent intent, @Nullable Bundle savedInstanceState);

    void onNewIntent(Intent intent);

    void onStart();

    void onRestart();

    void onResume();

    void onPause();

    void onStop();

    void onDestroy();

    void onBackPressed();
}
