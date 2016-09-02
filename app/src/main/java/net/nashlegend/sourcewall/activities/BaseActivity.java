package net.nashlegend.sourcewall.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.umeng.analytics.MobclickAgent;

import net.nashlegend.sourcewall.util.ToastUtil;

/**
 * Created by NashLegend on 2014/9/15 0015
 */
public abstract class BaseActivity extends SwipeActivity {

    private boolean isActive = false;

    @Override
    public void setTheme(int resid) {
        super.setTheme(resid);
    }

    public void directlySetTheme(int resid) {
        super.setTheme(resid);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        isActive = true;
    }

    @Override
    protected void onPause() {
        isActive = false;
        MobclickAgent.onPause(this);
        super.onPause();
    }

    public void gotoLogin() {
        startActivity(LoginActivity.class);
    }

    public boolean isActive() {
        return isActive;
    }

    public void toast(int msgID) {
        if (isActive()) {
            ToastUtil.toast(msgID);
        }
    }

    public void toast(String msg) {
        if (isActive()) {
            ToastUtil.toast(msg);
        }
    }

    public void toastSingleton(int msgID) {
        if (isActive()) {
            ToastUtil.toastSingleton(msgID);
        }
    }

    public void toastSingleton(String msg) {
        if (isActive()) {
            ToastUtil.toastSingleton(msg);
        }
    }

    public void startActivity(Class clazz) {
        startOneActivity(new Intent(this, clazz));
    }

    @Override
    public void startActivity(Intent intent) {
        if (!intent.hasExtra("requestFrom")) {
            intent.putExtra("requestFrom", getClass().getCanonicalName());
        }
        super.startActivity(intent);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if (!intent.hasExtra("requestCode")) {
            intent.putExtra("requestCode", requestCode);
        }
        if (!intent.hasExtra("requestFrom")) {
            intent.putExtra("requestFrom", getClass().getCanonicalName());
        }
        super.startActivityForResult(intent, requestCode);
    }

    /**
     * 返回发起请求的类
     *
     * @return
     */
    @NonNull
    public String getRequestFrom() {
        String from = "";
        try {
            from = getIntent().getStringExtra("requestFrom");
        } catch (Exception e) {
            return "";
        }
        return from == null ? "" : from;
    }

    /**
     * 返回发起请求的requestCode
     *
     * @return
     */
    public int getRequestCode() {
        int code = -1;
        try {
            code = getIntent().getIntExtra("requestCode", -1);
        } catch (Exception e) {
            code = -1;
        }
        return code;
    }
}
