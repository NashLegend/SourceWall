package net.nashlegend.sourcewall;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import net.nashlegend.sourcewall.util.ToastUtil;

/**
 * Created by NashLegend on 2014/9/15 0015
 */
public class BaseActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
    }

    public void notifyNeedLog() {
        ToastUtil.toastSingleton(getString(R.string.login_needed));
    }

    public void startLoginActivity() {
        startActivity(new Intent(this, LoginActivity.class));
    }

}
