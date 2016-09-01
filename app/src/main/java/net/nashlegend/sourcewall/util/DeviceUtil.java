package net.nashlegend.sourcewall.util;

import android.app.Activity;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;

import net.nashlegend.sourcewall.App;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.net.ConnectivityManager.TYPE_MOBILE;
import static android.net.ConnectivityManager.TYPE_WIFI;

/**
 * Created by NashLegend on 2015/9/23 0023.
 * 打开设备某设置，获取设备信息
 */
public class DeviceUtil {

    /**
     * 网络是否可用，只有当wifi或者移动网络打开时才算，其他的不算
     *
     * @return
     */
    public static boolean isNetworkOK() {
        return isWifiConnected() || isMobileNetworkConnected();
    }

    public static boolean isWifiConnected() {
        return isNetworkConnectedByType(TYPE_WIFI);
    }

    public static boolean isMobileNetworkConnected() {
        return isNetworkConnectedByType(TYPE_MOBILE);
    }

    public static boolean isNetworkConnectedByType(int type) {
        ConnectivityManager manager = (ConnectivityManager) App.getApp().getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null && info.isConnected() && info.getType() == type;
    }

    public static void openNetwork(Activity activity) {
        openSetting(activity);
    }

    /**
     * 打开设置
     */
    public static void openSetting(Activity activity) {
        try {
            if (UiUtil.shouldThrottle()) {
                return;
            }
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            activity.startActivity(intent);
        } catch (Exception e) {
            ErrorUtils.onException(e);
        }
    }
}
