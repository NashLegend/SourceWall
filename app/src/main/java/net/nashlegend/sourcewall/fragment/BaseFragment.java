package net.nashlegend.sourcewall.fragment;

import android.content.Intent;
import android.support.v4.app.Fragment;

import net.nashlegend.sourcewall.util.ToastUtil;
import net.nashlegend.sourcewall.util.UiUtil;

import butterknife.Unbinder;

/**
 * Created by NashLegend on 2014/9/18 0018
 */
public abstract class BaseFragment extends Fragment {
    private boolean isActive = false;
    public Unbinder unbinder;

    public boolean isActive() {
        return isActive;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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

    public boolean reTap() {
        return false;
    }

    public void startOneActivity(Intent intent) {
        if (UiUtil.shouldThrottle()) {
            return;
        }
        super.startActivity(intent);
    }

    public void startActivity(Class clazz) {
        if (getContext() == null) {
            return;
        }
        startOneActivity(new Intent(getContext(), clazz));
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

}
