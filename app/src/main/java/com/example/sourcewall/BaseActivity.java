package com.example.sourcewall;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.example.sourcewall.util.ToastUtil;

/**
 * Created by NashLegend on 2014/9/15 0015
 */
public class BaseActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public void notifyNeedLog() {
        ToastUtil.toastSingleton("需要登录");
    }

    public void startLoginActivity() {
        startActivity(new Intent(this, LoginActivity.class));
    }

}
