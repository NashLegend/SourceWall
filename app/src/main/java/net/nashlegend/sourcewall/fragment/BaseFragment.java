package net.nashlegend.sourcewall.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by NashLegend on 2014/9/18 0018
 */
public abstract class BaseFragment extends Fragment {
    public View layoutView;
    private final ArrayList<AsyncTask> stackedTasks = new ArrayList<>();

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
    public void onDetach() {
        //onDestroy后执行
        stopAllTasks();
        super.onDetach();
    }


}
