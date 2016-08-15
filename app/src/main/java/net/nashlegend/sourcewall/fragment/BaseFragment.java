package net.nashlegend.sourcewall.fragment;

import android.content.Intent;
import android.support.v4.app.Fragment;

import net.nashlegend.sourcewall.util.ToastUtil;
import net.nashlegend.sourcewall.util.AAsyncTask;
import net.nashlegend.sourcewall.view.common.IStackedAsyncTaskInterface;

import java.util.ArrayList;

import butterknife.Unbinder;

/**
 * Created by NashLegend on 2014/9/18 0018
 */
public abstract class BaseFragment extends Fragment implements IStackedAsyncTaskInterface {
    private final ArrayList<AAsyncTask> stackedTasks = new ArrayList<>();
    private boolean isActive = false;
    public Unbinder unbinder;

    @Override
    public void addToStackedTasks(AAsyncTask task) {
        stackedTasks.add(task);
    }

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
        for (int i = 0; i < stackedTasks.size(); i++) {
            AAsyncTask task = stackedTasks.get(i);
            if (task != null && task.getStatus() == AAsyncTask.Status.RUNNING) {
                task.cancel(true);
            }
        }
        stackedTasks.clear();
    }

    public boolean isActive() {
        return isActive;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        stopAllTasks();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        isActive = true;
    }

    @Override
    public void onPause() {
        isActive = false;
        super.onPause();
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

    public boolean takeOverBackPress() {
        return false;
    }

    public void startActivity(Class clazz) {
        if (getContext() == null) {
            return;
        }
        Intent intent = new Intent(getContext(), clazz);
        startActivity(intent);
    }
}
