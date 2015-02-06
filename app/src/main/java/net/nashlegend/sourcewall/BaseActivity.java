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
public abstract class BaseActivity extends ActionBarActivity implements IStackedAsyncTaskInterface {

    private final ArrayList<AsyncTask> stackedTasks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
    }

    /**
     * 将AsyncTask添加到队列，在AsyncTask.onPreExecute中执行
     *
     * @param task 要添加的AsyncTask
     */
    @Override
    public void addToStackedTasks(AsyncTask task) {
        stackedTasks.add(task);
    }

    /**
     * 将AsyncTask从队列中删除，有可能从AsyncTask.onCancelled或者AsyncTask.onPostExecute里面调用
     * 由于stopAllTasks会调用AsyncTask.cancel，所以最后会多执行一次，我擦
     *
     * @param task 要清除的AsyncTask
     */
    @Override
    public void removeFromStackedTasks(AsyncTask task) {
        stackedTasks.remove(task);
    }

    @Override
    public void flushAllTasks() {
        stackedTasks.clear();
    }

    @Override
    public void stopAllTasks() {
        for (int i = 0; i < stackedTasks.size(); i++) {
            AsyncTask task = stackedTasks.get(i);
            if (task != null && task.getStatus() == AsyncTask.Status.RUNNING) {
                System.out.println("cancel");
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
        ToastUtil.toastSingleton(getString(R.string.login_needed));
    }

    public void startLoginActivity() {
        startActivity(new Intent(this, LoginActivity.class));
    }


}
