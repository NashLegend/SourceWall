package net.nashlegend.sourcewall;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import net.nashlegend.sourcewall.util.ToastUtil;

import java.util.ArrayList;

/**
 * Created by NashLegend on 2014/9/15 0015
 */
public abstract class BaseActivity extends ActionBarActivity {

    private final ArrayList<AsyncTask> stackedTasks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
    }


    public void addToStackedTasks(AsyncTask task) {
        stackedTasks.add(task);
    }

    public void removeFromStackedTasks(AsyncTask task) {
        stackedTasks.remove(task);
    }

    public void flushAllTasks() {
        stackedTasks.clear();
    }

    public void stopAllTasks() {
        for (int i = 0; i < stackedTasks.size(); i++) {
            AsyncTask task = stackedTasks.get(i);
            if (task != null && task.getStatus() == AsyncTask.Status.RUNNING) {
                task.cancel(true);
            }
        }
        stackedTasks.clear();
    }

    @Override
    public void finish() {
        stopAllTasks();
        super.finish();
    }

    public void notifyNeedLog() {
        ToastUtil.toastSingleton(getString(R.string.login_needed));
    }

    public void startLoginActivity() {
        startActivity(new Intent(this, LoginActivity.class));
    }


}
