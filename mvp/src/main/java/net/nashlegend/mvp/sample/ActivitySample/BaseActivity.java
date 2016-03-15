package net.nashlegend.mvp.sample.ActivitySample;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import net.nashlegend.mvp.presenter.interfaze.IActivityPresenter;
import net.nashlegend.mvp.view.ActivityView;

/**
 * Created by NashLegend on 16/1/30.
 * Sample
 * 如果Activity多层继承就坑爹了，不过多层继承说明做的渣，Muhaha~
 */
public abstract class BaseActivity<T extends IActivityPresenter>
        extends AppCompatActivity implements ActivityView {
    public T presenter;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        setPresenter();
        super.onCreate(savedInstanceState, persistentState);
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