package net.nashlegend.sourcewall.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.umeng.analytics.MobclickAgent;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.SharedPreferencesUtil;
import net.nashlegend.sourcewall.util.ToastUtil;
import net.nashlegend.sourcewall.view.common.AAsyncTask;
import net.nashlegend.sourcewall.view.common.IStackedAsyncTaskInterface;

import java.util.ArrayList;

/**
 * Created by NashLegend on 2014/9/15 0015
 */
public abstract class BaseActivity extends SwipeActivity implements IStackedAsyncTaskInterface {

    private final ArrayList<AAsyncTask> stackedTasks = new ArrayList<>();
    private boolean isActive = false;

    @Override
    public void setTheme(int resid) {
        if (SharedPreferencesUtil.readBoolean(Consts.Key_Is_Night_Mode, false)) {
            resid = R.style.AppThemeNight;
        } else {
            resid = R.style.AppTheme;
        }
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

    /**
     * 将AsyncTask添加到队列，在AsyncTask.onPreExecute中执行
     *
     * @param task 要添加的AsyncTask
     */
    @Override
    public void addToStackedTasks(AAsyncTask task) {
        stackedTasks.add(task);
    }

    /**
     * 将AsyncTask从队列中删除，有可能从AsyncTask.onCancelled或者AsyncTask.onPostExecute里面调用
     * 由于stopAllTasks会调用AsyncTask.cancel，所以最后会多执行一次，我擦
     *
     * @param task 要清除的AsyncTask
     */
    @Override
    public void removeFromStackedTasks(AAsyncTask task) {
        stackedTasks.remove(task);
    }

    @Override
    public void flushAllTasks() {
        stackedTasks.clear();
    }

    @Override
    public void stopAllTasks() {
        isActive = false;
        for (int i = 0; i < stackedTasks.size(); i++) {
            AAsyncTask task = stackedTasks.get(i);
            if (task != null && task.getStatus() == AAsyncTask.Status.RUNNING) {
                task.cancel(true);
            }
        }
        stackedTasks.clear();
    }

    @Override
    protected void onDestroy() {
        stopAllTasks();
        super.onDestroy();
    }

    public void notifyNeedLog() {
        toastSingleton(getString(R.string.login_needed));
    }

    public void startLoginActivity() {
        startActivity(new Intent(this, LoginActivity.class));
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
        Intent intent = new Intent(this, clazz);
        startActivity(intent);
    }
}
