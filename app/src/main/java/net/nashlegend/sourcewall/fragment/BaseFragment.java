package net.nashlegend.sourcewall.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.nashlegend.sourcewall.util.ToastUtil;
import net.nashlegend.sourcewall.view.common.AAsyncTask;
import net.nashlegend.sourcewall.view.common.IStackedAsyncTaskInterface;

import java.util.ArrayList;

/**
 * Created by NashLegend on 2014/9/18 0018
 */
public abstract class BaseFragment extends Fragment implements IStackedAsyncTaskInterface {
    public View layoutView;
    private final ArrayList<AAsyncTask> stackedTasks = new ArrayList<>();
    private boolean isActive = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (layoutView == null) {
            layoutView = onCreateLayoutView(inflater, container, savedInstanceState);
        } else {
            if (layoutView.getParent() != null) {
                ((ViewGroup) layoutView.getParent()).removeView(layoutView);
            }
            onCreateViewAgain(inflater, container, savedInstanceState);
        }
        return layoutView;
    }

    abstract public View onCreateLayoutView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    abstract public void onCreateViewAgain(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    abstract public void setTitle();

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

}
