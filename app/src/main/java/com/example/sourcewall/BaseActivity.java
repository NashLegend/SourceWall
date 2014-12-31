package com.example.sourcewall;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;

import com.example.sourcewall.util.ToastUtil;

/**
 * Created by NashLegend on 2014/9/15 0015
 */
public class BaseActivity extends ActionBarActivity {

    public void notifyNeedLog(){
        ToastUtil.toastSingleton("需要登录");
    }

    public void startLoginActivity() {
        startActivity(new Intent(this, LoginActivity.class));
    }

}
